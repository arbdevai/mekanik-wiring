# Project State & Session Memory

## Status Saat Ini
- **Fase Aktif:** Selesai Penuh (v1.0.0 Resmi Dirilis ke GitHub Releases)
- **Versi Terakhir:** v1.0.0 (versionCode: 10000)
- **Status CI/CD:** Hijau (Sukses) - Run ID 43001292144
- **Tautan Rilis Publik:** https://github.com/arbdevai/mekanik-wiring/releases/tag/v1.0.0

## Fitur yang Selesai Tuntas (Vertical Slices)
- [x] Inisiasi proyek Android Jetpack Compose murni (Single-Activity, Clean Architecture).
- [x] Engine C++ NDK Manhattan Wire Router (`app/src/main/cpp/`) untuk perutean kabel siku-siku 90 derajat bebas tabrakan.
- [x] Basis data Room SQLite (`WiringDatabase`, `DiagramDao`, `DiagramProjectEntity`).
- [x] Standar warna kabel pabrikan Indonesia (`ManufacturerColorProfile`): Honda, Yamaha, Suzuki, Kawasaki, dan Aftermarket dengan pola warna ganda/strip.
- [x] Preset kelistrikan motor nyata siap pakai (`PresetWiringFactory`):
  - Pengapian DC CDI Shogun 110 Kebo (SMI / 30D unlimiter).
  - Pengapian AC Honda Astrea Grand / Supra X 100 5-Pin.
  - Pengisian & Penerangan Fullwave Kiprok Honda Tiger / Yamaha NMAX.
- [x] Engine Canvas Jetpack Compose interaktif (`SchematicCanvas`):
  - Zoom (0.35x hingga 3.2x), pan dua dimensi, dan snapping grid teknis.
  - Hit testing terminal pin dengan snap target penarikan kabel.
  - Live drafting kabel baru saat disentuh dan diseret antar komponen.
  - Pemilihan kabel, pelepasan kabel, dan penyorotan net kontinuitas (*trace line*).
  - Perpindahan posisi komponen (*drag to move*).
- [x] Pemeriksaan kontinuitas otomatis (`CircuitDiagnosticReport`):
  - Validasi kelengkapan jalur pengapian (Massa, Pulser, 12V/Spul Api, Koil, Busi).
  - Validasi kelengkapan sistem pengisian (Spul AC, Kiprok, Aki).
- [x] Ekspor dan Impor skema diagram berbasis JSON (`ExportImportDialog`).
- [x] Modul In-App Updater mandiri (`UpdateManager.kt` & `UpdateDialog.kt`):
  - Unduhan streaming OkHttp ke berkas `.part`.
  - Verifikasi integritas 4 lapis (`isApkValid` via `getPackageArchiveInfo`).
  - Pemilihan otomatis arsitektur CPU (`arm64-v8a` vs `armeabi-v7a`).
- [x] Antarmuka True Dark OLED (`#000000` murni, `#121212` surface, bebas AI slop dan bebas warna kebiruan).
- [x] GitHub Actions CI/CD Pipeline lengkap dengan auto-keystore signing dan auto-release.

## Catatan Bug Terselesaikan & Workaround
- *Bug #01:* KSP version mismatch `2.2.20-1.0.29` tidak ditemukan di Maven.
  *Solusi:* Menggunakan KSP `2.2.20-2.0.4` yang kompatibel dengan Kotlin 2.2.20.
- *Bug #02:* Konflik konfigurasi ABI antara `ndk.abiFilters` dan `splits.abi`.
  *Solusi:* Menghapus blok `ndk { abiFilters }` karena `splits.abi` sudah mengontrol paket secara otomatis.
- *Bug #03:* Unresolved reference `ActiveSheet` pada `WiringScreen.kt`.
  *Solusi:* Menghapus import tak terpakai dari `ui.components`.

## Paket Binary APK Rilis v1.0.0
- **ARM64 (Android Modern 64-bit):** `app-arm64-v8a-release.apk` (13.01 MB)
- **ARM32 (Android Legacy 32-bit):** `app-armeabi-v7a-release.apk` (12.99 MB)
