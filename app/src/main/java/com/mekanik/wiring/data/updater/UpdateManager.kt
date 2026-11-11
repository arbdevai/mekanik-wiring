package com.mekanik.wiring.data.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.mekanik.wiring.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class Available(
        val versionTag: String,
        val changelog: String,
        val downloadUrl: String,
        val assetBytes: Long
    ) : UpdateState
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateState
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

class UpdateManager(
    private val context: Context,
    private val repoOwner: String = "arbdevai",
    private val repoName: String = "mekanik-wiring"
) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(): UpdateState = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            cleanOldApkFiles()

            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val state = UpdateState.Error("Gagal menghubungi server rilis (${response.code})")
                _updateState.value = state
                return@withContext state
            }

            val bodyString = response.body?.string() ?: throw Exception("Respons server kosong")
            val releaseObj = json.parseToJsonElement(bodyString).jsonObject

            val tagName = releaseObj["tag_name"]?.jsonPrimitive?.content ?: ""
            val changelog = releaseObj["body"]?.jsonPrimitive?.content ?: "Pembaruan rilis stabil."
            val assetsArray = releaseObj["assets"]?.jsonArray ?: throw Exception("Tidak ada berkas APK pada rilis")

            // Compare version
            val cleanTag = tagName.removePrefix("v").trim()
            if (!isRemoteVersionNewer(cleanTag, BuildConfig.VERSION_NAME)) {
                val state = UpdateState.Idle
                _updateState.value = state
                return@withContext state
            }

            // Find matching ABI asset
            val primaryAbi = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "arm64-v8a"
            var targetAsset = assetsArray.find {
                val name = it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                name.contains(primaryAbi, ignoreCase = true) && name.endsWith(".apk")
            }

            if (targetAsset == null) {
                targetAsset = assetsArray.find {
                    val name = it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                    name.endsWith(".apk")
                }
            }

            if (targetAsset == null) {
                val state = UpdateState.Error("Tidak ditemukan berkas APK yang kompatibel dengan perangkat")
                _updateState.value = state
                return@withContext state
            }

            val downloadUrl = targetAsset.jsonObject["browser_download_url"]?.jsonPrimitive?.content ?: ""
            val assetSize = targetAsset.jsonObject["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

            val state = UpdateState.Available(
                versionTag = tagName,
                changelog = changelog,
                downloadUrl = downloadUrl,
                assetBytes = assetSize
            )
            _updateState.value = state
            state
        } catch (e: Exception) {
            val state = UpdateState.Error("Gagal memeriksa pembaruan: ${e.message}")
            _updateState.value = state
            state
        }
    }

    suspend fun downloadAndVerify(downloadUrl: String, expectedBytes: Long) = withContext(Dispatchers.IO) {
        val downloadDir = context.getExternalFilesDir(null) ?: context.filesDir
        val partFile = File(downloadDir, "update.part")
        val finalApk = File(downloadDir, "update_${System.currentTimeMillis()}.apk")

        if (partFile.exists()) partFile.delete()

        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body ?: throw Exception("Data unduhan kosong")
            val totalBytes = if (expectedBytes > 0) expectedBytes else body.contentLength()

            body.byteStream().use { input ->
                FileOutputStream(partFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesCopied = 0L
                    var read: Int

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val percent = if (totalBytes > 0) ((bytesCopied * 100) / totalBytes).toInt() else 0
                        _updateState.value = UpdateState.Downloading(percent, bytesCopied, totalBytes)
                    }
                    output.flush()
                }
            }

            // Rename .part to .apk
            if (!partFile.renameTo(finalApk)) {
                throw Exception("Gagal menyelesaikan penulisan berkas APK")
            }

            // 4-Layer Integrity Verification
            if (!isApkValid(context, finalApk, expectedBytes)) {
                finalApk.delete()
                throw Exception("Integritas paket APK tidak valid atau rusak")
            }

            _updateState.value = UpdateState.ReadyToInstall(finalApk)
        } catch (e: Exception) {
            if (partFile.exists()) partFile.delete()
            if (finalApk.exists()) finalApk.delete()
            _updateState.value = UpdateState.Error("Gagal mengunduh berkas pembaruan: ${e.message}")
        }
    }

    fun triggerInstall(apkFile: File) {
        if (!apkFile.exists() || apkFile.length() < 10_000_000) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            return
        }

        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }

    private fun isApkValid(context: Context, apkFile: File, expectedBytes: Long = 0L): Boolean {
        if (!apkFile.exists() || !apkFile.isFile || apkFile.length() < 10_000_000) return false
        if (expectedBytes > 0L && Math.abs(apkFile.length() - expectedBytes) > 2048L) return false

        return try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_ACTIVITIES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES or PackageManager.GET_ACTIVITIES
            }
            val pkg = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags) ?: return false
            if (pkg.packageName != context.packageName) return false
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun cleanOldApkFiles() {
        val downloadDir = context.getExternalFilesDir(null) ?: context.filesDir
        downloadDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".part") || file.name.endsWith(".apk")) {
                file.delete()
            }
        }
    }

    private fun isRemoteVersionNewer(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
