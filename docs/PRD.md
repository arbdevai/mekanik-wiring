# Product Requirement Document (PRD)

## 1. Identitas Aplikasi
- **Nama Aplikasi:** Mekanik Wiring (Skema Kelistrikan Motor)
- **Package Name:** com.mekanik.wiring
- **Min SDK / Target SDK:** 26 / 35 (Android 15 Ready)
- **Arsitektur:** Single-Activity, Clean Architecture (Data -> Domain -> UI), Jetpack Compose Material 3, NDK C++ Engine.

## 2. Kebutuhan Teknis & Native
- **UI & Rendering:** Jetpack Compose Canvas dengan dukungan Hardware Acceleration, custom transformation matrix (Pinch-to-zoom, Pan, Grid snap, Hit testing terminal/pin).
- **Native Engine (C/C++ NDK):** Engine C++ untuk komputasi grafis dan *auto-routing* kabel ortogonal (Manhattan routing / A* pathfinding bebas tabrakan) agar saat puluhan kabel ditarik antar komponen, jalurnya rapi dan tidak saling tumpang tindih sembarangan.
- **Storage & Persistensi:**
  - Room Database (SQLite) untuk katalog komponen (CDI, Kiprok, Spul, Koil, Saklar, Aki, dll.), pinout definitions, preset skema bawaan, dan proyek tersimpan.
  - File System export/import format `.wire` / `.json` untuk pertukaran skema antar mekanik/bengkel.
- **Tema Visual:** True Dark OLED (`#000000` murni) dengan palet warna kabel berstandar pabrikan (Honda, Yamaha, Suzuki, Kawasaki) dan kontras tinggi untuk visibilitas di lingkungan bengkel.

## 3. Daftar Fitur Utama (Vertical Slices)
- [ ] **Fase 1: Inisiasi Proyek & Fondasi Arsitektur**
  - Setup Gradle Version Catalog (Kotlin 2.x, AGP 8.x, Compose BOM, Room, KSP).
  - Setup C++ NDK CMake toolchain untuk auto-routing kabel.
  - Setup True Dark OLED Material 3 theme tanpa warna kebiruan (#000000 murni).
- [ ] **Fase 2: Database Komponen & Pinout (Room DB)**
  - Entity & DAO untuk Komponen (CDI, Kiprok, Spul, Koil, Aki, Kunci Kontak, Saklar, Flasher, Bendik).
  - Terminal/Pin data model (Polaritas, Nama Sinyal: 12V DC, Massa/Ground, Pulser +, Pulser -, Koil Out, Spul Pengisian, Spul Lampu).
  - Manufacturer Color Profiles (Honda, Yamaha, Suzuki, Kawasaki, Aftermarket).
  - Seeding data awal komponen populer (CDI Shogun Kebo, Mio 5TL, Tiger, Grand, Kiprok NMAX/Tiger/Grand).
- [ ] **Fase 3: C++ NDK Wire Router Engine**
  - Implementasi Manhattan / Orthogonal routing A* di C++ untuk menghitung rute belokan kabel menghindari bodi komponen.
  - JNI Bridge antara Kotlin Coroutine dan C++ Native Router.
- [ ] **Fase 4: Interactive Schematic Canvas (Jetpack Compose)**
  - Infinite Canvas dengan dukungan gesture: Zoom, Pan, dan Snap-to-Grid.
  - Render komponen bodi dengan pin terminal yang interaktif (hit-testing sentuhan jari presisi).
  - Koneksi kabel pin-to-pin interaktif dengan rendering dua warna (solid & striped/bergaris).
  - Mode Tracing Jalur: Sentuh salah satu kabel/pin, seluruh rangkaian yang terhubung otomatis menyala terang (*highlight*).
- [ ] **Fase 5: Preset Wiring & Modul Siap Pakai**
  - Preset 1: Pengapian AC (Spul -> CDI -> Koil -> Busi).
  - Preset 2: Pengapian DC (Aki/Kontak -> CDI -> Koil -> Busi).
  - Preset 3: Sistem Pengisian & Penerangan Fullwave (Kiprok Tiger / NMAX).
  - Preset 4: Sistem Starter Elektrik (Tombol -> Bendik -> Dinamo).
- [ ] **Fase 6: Export & Manajemen Proyek**
  - Simpan dan muat proyek wiring dari Room DB.
  - Ekspor skema ke JSON/`.wire` untuk dibagikan.
  - Render skema ke Bitmap resolusi tinggi (PNG/PDF) untuk dicetak di bengkel.

## 4. Batasan & Desain Sistem
- Bebas AI slop (Nol pulsing green dot, nol marquee, nol glow berlebihan).
- Nol emoji di seluruh UI, microcopy, dan log.
- Wajib Vector Icons resmi Material 3 (`androidx.compose.material:material-icons-extended` / XML Vector).
- Shimmer skeleton loading untuk semua status tunggu.
- Strict OLED Black (`#000000`) pada latar belakang kanvas dan surface dialog.
