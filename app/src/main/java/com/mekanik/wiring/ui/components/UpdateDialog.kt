package com.mekanik.wiring.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mekanik.wiring.data.updater.UpdateState
import com.mekanik.wiring.ui.theme.EarthGreen
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.OledBlack
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onDownload: (downloadUrl: String, assetBytes: Long) -> Unit,
    onInstall: (apkFile: java.io.File) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeutralSurface,
            border = BorderStroke(1.dp, NeutralBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                when (updateState) {
                    is UpdateState.Available -> {
                        Text(
                            text = "Pembaruan Tersedia (${updateState.versionTag})",
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateState.changelog,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Nanti")
                            }
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Button(
                                onClick = { onDownload(updateState.downloadUrl, updateState.assetBytes) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EarthGreen,
                                    contentColor = OledBlack
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Unduh Sekarang")
                            }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Text(
                            text = "Mengunduh Pembaruan...",
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { updateState.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = EarthGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${updateState.progressPercent}% (${updateState.bytesDownloaded / 1024} KB / ${updateState.totalBytes / 1024} KB)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    is UpdateState.ReadyToInstall -> {
                        Text(
                            text = "Berkas Siap Dipasang",
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Integritas berkas telah terverifikasi. Tekan pasang untuk memperbarui aplikasi.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { onInstall(updateState.apkFile) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EarthGreen,
                                contentColor = OledBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pasang Pembaruan")
                        }
                    }
                    is UpdateState.Error -> {
                        Text(
                            text = "Pemeriksaan Pembaruan",
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateState.message,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeutralBorder,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tutup")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
