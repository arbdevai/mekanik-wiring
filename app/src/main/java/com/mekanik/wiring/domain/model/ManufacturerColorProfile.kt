package com.mekanik.wiring.domain.model

data class WireColor(
    val primaryHex: Long,
    val stripeHex: Long? = null,
    val indonesianName: String
)

enum class Manufacturer(val label: String) {
    HONDA("Honda"),
    YAMAHA("Yamaha"),
    SUZUKI("Suzuki"),
    KAWASAKI("Kawasaki"),
    AFTERMARKET("Aftermarket / Kustom")
}

object ManufacturerColorProfile {
    fun getStandardColor(manufacturer: Manufacturer, role: PinRole): WireColor {
        return when (manufacturer) {
            Manufacturer.HONDA -> when (role) {
                PinRole.GND -> WireColor(0xFF2E7D32, null, "Hijau (Massa)")
                PinRole.BATTERY_12V -> WireColor(0xFFD32F2F, null, "Merah (Aki +12V)")
                PinRole.SWITCHED_12V -> WireColor(0xFF212121, null, "Hitam (12V Kontak)")
                PinRole.COIL_OUT -> WireColor(0xFF212121, 0xFFFBC02D, "Hitam-Kuning (CDI ke Koil)")
                PinRole.PULSER -> WireColor(0xFF1976D2, 0xFFFBC02D, "Biru-Kuning (Pulser +)")
                PinRole.PULSER_MINUS -> WireColor(0xFF2E7D32, null, "Hijau (Massa Pulser)")
                PinRole.AC_IGNITION_SOURCE -> WireColor(0xFF212121, 0xFFD32F2F, "Hitam-Merah (Spul Api AC)")
                PinRole.CHARGE_AC -> WireColor(0xFFEEEEEE, null, "Putih (Spul Pengisian)")
                PinRole.LIGHT_AC -> WireColor(0xFFFBC02D, null, "Kuning (Spul Lampu)")
                PinRole.ENGINE_STOP -> WireColor(0xFF212121, 0xFFEEEEEE, "Hitam-Putih (Engine Stop)")
                PinRole.STARTER_TRIGGER -> WireColor(0xFFFBC02D, 0xFFD32F2F, "Kuning-Merah (Starter)")
                PinRole.VOLTAGE_MONITOR -> WireColor(0xFF212121, null, "Hitam (Monitor Kontak)")
                PinRole.STARTER_HIGH_AMP -> WireColor(0xFFD32F2F, null, "Merah Tebal")
                PinRole.SPARK_OUT -> WireColor(0xFF1E88E5, null, "Kabel Busi")
            }
            Manufacturer.YAMAHA -> when (role) {
                PinRole.GND -> WireColor(0xFF212121, null, "Hitam (Massa)")
                PinRole.BATTERY_12V -> WireColor(0xFFD32F2F, null, "Merah (Aki +12V)")
                PinRole.SWITCHED_12V -> WireColor(0xFF5D4037, null, "Cokelat (12V Kontak)")
                PinRole.COIL_OUT -> WireColor(0xFFF57C00, null, "Orange (CDI ke Koil)")
                PinRole.PULSER -> WireColor(0xFFEEEEEE, 0xFFD32F2F, "Putih-Merah (Pulser +)")
                PinRole.PULSER_MINUS -> WireColor(0xFF212121, null, "Hitam (Massa Pulser)")
                PinRole.AC_IGNITION_SOURCE -> WireColor(0xFF212121, 0xFFD32F2F, "Hitam-Merah (Spul Api AC)")
                PinRole.CHARGE_AC -> WireColor(0xFFEEEEEE, null, "Putih (Spul Pengisian)")
                PinRole.LIGHT_AC -> WireColor(0xFFFBC02D, 0xFFD32F2F, "Kuning-Merah (Spul Lampu)")
                PinRole.ENGINE_STOP -> WireColor(0xFF212121, 0xFFEEEEEE, "Hitam-Putih (Engine Stop)")
                PinRole.STARTER_TRIGGER -> WireColor(0xFF1976D2, 0xFFEEEEEE, "Biru-Putih (Starter)")
                PinRole.VOLTAGE_MONITOR -> WireColor(0xFF5D4037, null, "Cokelat (Monitor Kontak)")
                PinRole.STARTER_HIGH_AMP -> WireColor(0xFFD32F2F, null, "Merah Tebal")
                PinRole.SPARK_OUT -> WireColor(0xFF1E88E5, null, "Kabel Busi")
            }
            Manufacturer.SUZUKI -> when (role) {
                PinRole.GND -> WireColor(0xFF212121, 0xFFEEEEEE, "Hitam-Putih (Massa)")
                PinRole.BATTERY_12V -> WireColor(0xFFD32F2F, null, "Merah (Aki +12V)")
                PinRole.SWITCHED_12V -> WireColor(0xFFF57C00, null, "Orange (12V Kontak)")
                PinRole.COIL_OUT -> WireColor(0xFFEEEEEE, 0xFF1976D2, "Putih-Biru (CDI ke Koil)")
                PinRole.PULSER -> WireColor(0xFF1976D2, 0xFFFBC02D, "Biru-Kuning (Pulser +)")
                PinRole.PULSER_MINUS -> WireColor(0xFF2E7D32, null, "Hijau (Massa Pulser)")
                PinRole.AC_IGNITION_SOURCE -> WireColor(0xFF212121, 0xFFD32F2F, "Hitam-Merah (Spul Api AC)")
                PinRole.CHARGE_AC -> WireColor(0xFFEEEEEE, 0xFF1976D2, "Putih-Biru (Spul Pengisian)")
                PinRole.LIGHT_AC -> WireColor(0xFFFBC02D, 0xFFEEEEEE, "Kuning-Putih (Spul Lampu)")
                PinRole.ENGINE_STOP -> WireColor(0xFF212121, 0xFFF57C00, "Hitam-Orange (Engine Stop)")
                PinRole.STARTER_TRIGGER -> WireColor(0xFFFBC02D, 0xFF2E7D32, "Kuning-Hijau (Starter)")
                PinRole.VOLTAGE_MONITOR -> WireColor(0xFFF57C00, null, "Orange (Monitor Kontak)")
                PinRole.STARTER_HIGH_AMP -> WireColor(0xFFD32F2F, null, "Merah Tebal")
                PinRole.SPARK_OUT -> WireColor(0xFF1E88E5, null, "Kabel Busi")
            }
            Manufacturer.KAWASAKI -> when (role) {
                PinRole.GND -> WireColor(0xFF212121, 0xFFFBC02D, "Hitam-Kuning (Massa)")
                PinRole.BATTERY_12V -> WireColor(0xFFEEEEEE, null, "Putih (Aki Langsung)")
                PinRole.SWITCHED_12V -> WireColor(0xFF5D4037, null, "Cokelat (12V Kontak)")
                PinRole.COIL_OUT -> WireColor(0xFF212121, null, "Hitam (CDI ke Koil)")
                PinRole.PULSER -> WireColor(0xFFD32F2F, null, "Merah (Pulser +)")
                PinRole.PULSER_MINUS -> WireColor(0xFF212121, 0xFFFBC02D, "Hitam-Kuning (Massa Pulser)")
                PinRole.AC_IGNITION_SOURCE -> WireColor(0xFFD32F2F, 0xFF212121, "Merah-Hitam (Spul Api AC)")
                PinRole.CHARGE_AC -> WireColor(0xFF1976D2, null, "Biru (Spul Pengisian)")
                PinRole.LIGHT_AC -> WireColor(0xFFFBC02D, null, "Kuning (Spul Lampu)")
                PinRole.ENGINE_STOP -> WireColor(0xFF212121, 0xFFEEEEEE, "Hitam-Putih (Engine Stop)")
                PinRole.STARTER_TRIGGER -> WireColor(0xFF212121, 0xFFD32F2F, "Hitam-Merah (Starter)")
                PinRole.VOLTAGE_MONITOR -> WireColor(0xFF5D4037, null, "Cokelat (Monitor Kontak)")
                PinRole.STARTER_HIGH_AMP -> WireColor(0xFFEEEEEE, null, "Putih Tebal")
                PinRole.SPARK_OUT -> WireColor(0xFF1E88E5, null, "Kabel Busi")
            }
            Manufacturer.AFTERMARKET -> when (role) {
                PinRole.GND -> WireColor(0xFF2E7D32, null, "Hijau (Massa)")
                PinRole.BATTERY_12V -> WireColor(0xFFD32F2F, null, "Merah (Aki +12V)")
                PinRole.SWITCHED_12V -> WireColor(0xFF212121, null, "Hitam (12V Kontak)")
                PinRole.COIL_OUT -> WireColor(0xFFF57C00, null, "Orange (Koil)")
                PinRole.PULSER -> WireColor(0xFF1976D2, null, "Biru (Pulser)")
                PinRole.PULSER_MINUS -> WireColor(0xFF2E7D32, null, "Hijau (Massa Pulser)")
                PinRole.AC_IGNITION_SOURCE -> WireColor(0xFF212121, 0xFFD32F2F, "Hitam-Merah (Spul AC)")
                PinRole.CHARGE_AC -> WireColor(0xFFEEEEEE, null, "Putih (Pengisian)")
                PinRole.LIGHT_AC -> WireColor(0xFFFBC02D, null, "Kuning (Lampu)")
                PinRole.ENGINE_STOP -> WireColor(0xFF212121, 0xFFEEEEEE, "Hitam-Putih (Stop)")
                PinRole.STARTER_TRIGGER -> WireColor(0xFFFBC02D, 0xFFD32F2F, "Kuning-Merah (Starter)")
                PinRole.VOLTAGE_MONITOR -> WireColor(0xFF212121, null, "Hitam (Sensor)")
                PinRole.STARTER_HIGH_AMP -> WireColor(0xFFD32F2F, null, "Merah Tebal")
                PinRole.SPARK_OUT -> WireColor(0xFF1E88E5, null, "Kop Busi")
            }
        }
    }
}
