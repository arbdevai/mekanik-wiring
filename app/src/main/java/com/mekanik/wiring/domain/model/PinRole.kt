package com.mekanik.wiring.domain.model

enum class PinRole(val displayName: String) {
    GND("Massa / Ground"),
    BATTERY_12V("12V Aki Langsung"),
    SWITCHED_12V("12V Pasca Kontak"),
    PULSER("Sinyal Pulser (+)"),
    PULSER_MINUS("Sinyal Pulser (-)"),
    COIL_OUT("Output ke Koil"),
    AC_IGNITION_SOURCE("Spul Pengapian AC"),
    CHARGE_AC("Spul Pengisian AC"),
    LIGHT_AC("Spul Penerangan AC"),
    VOLTAGE_MONITOR("Sensor Voltase Kiprok"),
    STARTER_TRIGGER("Trigger Bendik Starter"),
    STARTER_HIGH_AMP("Jalur Utama Dinamo Starter"),
    ENGINE_STOP("Engine Stop Switch"),
    SPARK_OUT("Kop Busi High Voltage")
}

enum class PinSide {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM
}

enum class ComponentType(val displayName: String) {
    CDI("CDI Unit"),
    KIPROK("Kiprok / Regulator"),
    SPUL("Spul & Pulser"),
    KOIL("Koil Pengapian"),
    AKI("Aki 12V"),
    KONTAK("Kunci Kontak"),
    SAKLAR("Saklar Handle"),
    BENDIK("Bendik Starter"),
    FLASHER("Flasher Sein"),
    FUSE("Sekring / Fuse"),
    BUSI("Busi")
}
