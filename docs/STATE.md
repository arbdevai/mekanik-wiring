# Project State & Session Memory

## Status Saat Ini
- **Fase Aktif:** Fase 1-5 Tuntas: Fondasi Arsitektur, Engine C++ NDK, Room DB, Presets, dan Interactive Canvas Selesai
- **Versi Terakhir:** v1.0.0 (versionCode: 10000)
- **Status Sintaksis:** Lolos verifikasi `verify_kotlin_syntax.py` (0 Hard Errors)

## Fitur yang Selesai Tuntas (Vertical Slices)
- [x] Inisiasi proyek Android Jetpack Compose murni (Single-Activity, Clean Architecture).
- [x] Konfigurasi C++ NDK & CMake Manhattan Wire Router (`app/src/main/cpp/`) untuk perutean kabel siku-siku bebas tabrakan dengan fallback murni Kotlin yang tahan uji.
- [x] Basis data Room SQLite (`WiringDatabase`, `DiagramDao`, `DiagramProjectEntity`) dengan proteksi KSP2 (`suspend deleteById` mengembalikan `: Int`).
- [x] Standar warna kabel pabrikan Indonesia (`ManufacturerColorProfile`): Honda, Yamaha, Suzuki, Kawasaki, dan Aftermarket dengan pola warna ganda/strip.
- [x] Preset kelistrikan motor nyata siap pakai (`PresetWiringFactory`):
  - Pengapian DC CDI Shogun 110 Kebo (SMI / 30D unlimiter).
  - Pengapian AC Honda Astrea Grand / Supra X 100 5-Pin.
  - Pengisian & Penerangan Fullwave Kiprok Honda Tiger / Yamaha NMAX.
- [x] Engine Canvas Jetpack Compose interaktif (`SchematicCanvas`):
  - Zoom (0.35x hingga 3.2x), pan dua dimensi, dan snapping grid teknis.
  - Hit testing terminal pin dengan lingkaran target penarikan kabel.
  - Live drafting kabel baru saat disentuh dan diseret antar komponen.
  - Pemilihan kabel, pelepasan kabel, dan penyorotan net kontinuitas (*trace line*).
  - Perpindahan posisi komponen (*drag to move*).
- [x] Pemeriksaan kontinuitas otomatis (`CircuitDiagnosticReport`):
  - Validasi kelengkapan jalur pengapian (Massa, Pulser, 12V/Spul Api, Koil, Busi).
  - Validasi kelengkapan sistem pengisian (Spul AC, Kiprok, Aki).
- [x] Ekspor dan Impor skema diagram berbasis JSON (`ExportImportDialog`).
- [x] Antarmuka True Dark OLED (`#000000` murni, `#121212` surface, bebas AI slop dan bebas warna kebiruan).

## Catatan Bug Terselesaikan & Workaround
- *Bug #01:* Validator mendeteksi variabel ambigu `it` di dalam nested lambda `onSuccess` pada `WiringViewModel.kt`.
  *Solusi:* Menggunakan parameter eksplisit `currentState ->` pada lambda `_uiState.update`.

## Tugas Selanjutnya
1. Pengujian interaktif kanvas dan penambahan preset motor injeksi (ECU/ECM 33-pin).
2. Penyusunan laporan verifikasi untuk pengguna.
