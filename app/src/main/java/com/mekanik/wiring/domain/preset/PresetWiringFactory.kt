package com.mekanik.wiring.domain.preset

import com.mekanik.wiring.domain.model.ComponentPin
import com.mekanik.wiring.domain.model.ComponentType
import com.mekanik.wiring.domain.model.Manufacturer
import com.mekanik.wiring.domain.model.ManufacturerColorProfile
import com.mekanik.wiring.domain.model.MotorComponent
import com.mekanik.wiring.domain.model.PinRole
import com.mekanik.wiring.domain.model.PinSide
import com.mekanik.wiring.domain.model.WiringCable
import com.mekanik.wiring.domain.model.WiringDiagram
import java.util.UUID

object PresetWiringFactory {

    fun createShogunKeboDCIgnition(): WiringDiagram {
        val akiId = "aki-1"
        val fuseId = "fuse-1"
        val kontakId = "kontak-1"
        val spulId = "spul-1"
        val cdiId = "cdi-shogun-1"
        val koilId = "koil-1"
        val busiId = "busi-1"

        val aki = MotorComponent(
            id = akiId,
            name = "Aki Basah/Kering",
            codeModel = "GTZ5S / 12V 3.5Ah",
            type = ComponentType.AKI,
            x = 80f,
            y = 120f,
            width = 170f,
            height = 110f,
            pins = listOf(
                ComponentPin("aki-pos", "+12V Aki", PinRole.BATTERY_12V, PinSide.RIGHT, 0.35f),
                ComponentPin("aki-neg", "Massa Aki (-)", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val fuse = MotorComponent(
            id = fuseId,
            name = "Sekring Pengaman",
            codeModel = "Fuse Blade 10A Merah",
            type = ComponentType.FUSE,
            x = 320f,
            y = 120f,
            width = 160f,
            height = 90f,
            pins = listOf(
                ComponentPin("fuse-in", "In (+12V Aki)", PinRole.BATTERY_12V, PinSide.LEFT, 0.5f),
                ComponentPin("fuse-out", "Out (ke Kontak)", PinRole.BATTERY_12V, PinSide.RIGHT, 0.5f)
            )
        )

        val kontak = MotorComponent(
            id = kontakId,
            name = "Kunci Kontak",
            codeModel = "Kontak 2 Kabel Universal",
            type = ComponentType.KONTAK,
            x = 540f,
            y = 120f,
            width = 170f,
            height = 100f,
            pins = listOf(
                ComponentPin("kontak-in", "Input 12V", PinRole.BATTERY_12V, PinSide.LEFT, 0.5f),
                ComponentPin("kontak-out", "Out 12V Kontak", PinRole.SWITCHED_12V, PinSide.RIGHT, 0.5f)
            )
        )

        val spul = MotorComponent(
            id = spulId,
            name = "Stator & Pulser",
            codeModel = "Spul Magnet Suzuki",
            type = ComponentType.SPUL,
            x = 80f,
            y = 350f,
            width = 180f,
            height = 120f,
            pins = listOf(
                ComponentPin("spul-pulser", "Sinyal Pulser (+)", PinRole.PULSER, PinSide.RIGHT, 0.35f),
                ComponentPin("spul-gnd", "Massa Pulser (-)", PinRole.PULSER_MINUS, PinSide.RIGHT, 0.70f)
            )
        )

        val cdi = MotorComponent(
            id = cdiId,
            name = "CDI Shogun Kebo",
            codeModel = "SMI / 30D (DC Unlimiter)",
            type = ComponentType.CDI,
            x = 420f,
            y = 350f,
            width = 200f,
            height = 130f,
            pins = listOf(
                ComponentPin("cdi-pin1", "Pin 1: Pulser (+)", PinRole.PULSER, PinSide.LEFT, 0.35f),
                ComponentPin("cdi-pin4", "Pin 4: +12V Kontak", PinRole.SWITCHED_12V, PinSide.TOP, 0.5f),
                ComponentPin("cdi-pin3", "Pin 3: Massa (-)", PinRole.GND, PinSide.BOTTOM, 0.5f),
                ComponentPin("cdi-pin2", "Pin 2: Output Koil", PinRole.COIL_OUT, PinSide.RIGHT, 0.5f)
            )
        )

        val koil = MotorComponent(
            id = koilId,
            name = "Koil Pengapian",
            codeModel = "Koil Kawahara / YZ 125",
            type = ComponentType.KOIL,
            x = 730f,
            y = 350f,
            width = 170f,
            height = 110f,
            pins = listOf(
                ComponentPin("koil-in", "Input dari CDI", PinRole.COIL_OUT, PinSide.LEFT, 0.4f),
                ComponentPin("koil-gnd", "Massa Koil", PinRole.GND, PinSide.BOTTOM, 0.5f),
                ComponentPin("koil-out", "Kop Busi High-Volt", PinRole.SPARK_OUT, PinSide.RIGHT, 0.5f)
            )
        )

        val busi = MotorComponent(
            id = busiId,
            name = "Busi Motor",
            codeModel = "NGK CPR9EA-9",
            type = ComponentType.BUSI,
            x = 980f,
            y = 350f,
            width = 150f,
            height = 90f,
            pins = listOf(
                ComponentPin("busi-terminal", "Terminal Busi", PinRole.SPARK_OUT, PinSide.LEFT, 0.5f),
                ComponentPin("busi-gnd", "Massa Drat Busi", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val cables = mutableListOf<WiringCable>()

        fun addCable(fromC: String, fromP: String, toC: String, toP: String, role: PinRole) {
            val color = ManufacturerColorProfile.getStandardColor(Manufacturer.SUZUKI, role)
            cables.add(
                WiringCable(
                    id = UUID.randomUUID().toString(),
                    fromComponentId = fromC,
                    fromPinId = fromP,
                    toComponentId = toC,
                    toPinId = toP,
                    wireColorHex = color.primaryHex,
                    stripeColorHex = color.stripeHex,
                    label = color.indonesianName
                )
            )
        }

        addCable(akiId, "aki-pos", fuseId, "fuse-in", PinRole.BATTERY_12V)
        addCable(fuseId, "fuse-out", kontakId, "kontak-in", PinRole.BATTERY_12V)
        addCable(kontakId, "kontak-out", cdiId, "cdi-pin4", PinRole.SWITCHED_12V)
        addCable(spulId, "spul-pulser", cdiId, "cdi-pin1", PinRole.PULSER)
        addCable(cdiId, "cdi-pin2", koilId, "koil-in", PinRole.COIL_OUT)
        addCable(koilId, "koil-out", busiId, "busi-terminal", PinRole.SPARK_OUT)
        addCable(cdiId, "cdi-pin3", akiId, "aki-neg", PinRole.GND)

        return WiringDiagram(
            id = "preset-shogun-dc",
            title = "Pengapian DC CDI Shogun 110 (Kebo 30D)",
            description = "Skema pengapian DC murni tanpa spul jalan. Arus CDI disuplai dari Aki 12V via Kunci Kontak.",
            manufacturer = Manufacturer.SUZUKI,
            components = listOf(aki, fuse, kontak, spul, cdi, koil, busi),
            cables = cables
        )
    }

    fun createGrandACIgnition(): WiringDiagram {
        val spulId = "spul-grand-1"
        val cdiId = "cdi-grand-1"
        val kontakId = "kontak-grand-1"
        val koilId = "koil-grand-1"
        val busiId = "busi-grand-1"

        val spul = MotorComponent(
            id = spulId,
            name = "Spul Pengapian & Pulser",
            codeModel = "Spul Astrea Grand / Prima",
            type = ComponentType.SPUL,
            x = 80f,
            y = 200f,
            width = 190f,
            height = 130f,
            pins = listOf(
                ComponentPin("spul-ac-api", "Spul Api AC", PinRole.AC_IGNITION_SOURCE, PinSide.RIGHT, 0.3f),
                ComponentPin("spul-pulser", "Pulser (+)", PinRole.PULSER, PinSide.RIGHT, 0.7f),
                ComponentPin("spul-massa", "Massa Rangka", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val cdi = MotorComponent(
            id = cdiId,
            name = "CDI Astrea Grand",
            codeModel = "Shindengen 5-Pin AC",
            type = ComponentType.CDI,
            x = 410f,
            y = 200f,
            width = 200f,
            height = 140f,
            pins = listOf(
                ComponentPin("cdi-spul", "Pin 1: Spul Api AC", PinRole.AC_IGNITION_SOURCE, PinSide.LEFT, 0.3f),
                ComponentPin("cdi-pulser", "Pin 2: Pulser (+)", PinRole.PULSER, PinSide.LEFT, 0.7f),
                ComponentPin("cdi-stop", "Pin 3: Engine Stop", PinRole.ENGINE_STOP, PinSide.TOP, 0.5f),
                ComponentPin("cdi-massa", "Pin 4: Massa Rangka", PinRole.GND, PinSide.BOTTOM, 0.5f),
                ComponentPin("cdi-koil", "Pin 5: Out ke Koil", PinRole.COIL_OUT, PinSide.RIGHT, 0.5f)
            )
        )

        val kontak = MotorComponent(
            id = kontakId,
            name = "Kunci Kontak 4 Kabel",
            codeModel = "Kontak Honda Grand AC",
            type = ComponentType.KONTAK,
            x = 410f,
            y = 40f,
            width = 200f,
            height = 90f,
            pins = listOf(
                ComponentPin("kontak-stop", "Kabel Hitam/Putih", PinRole.ENGINE_STOP, PinSide.BOTTOM, 0.4f),
                ComponentPin("kontak-massa", "Kabel Hijau (Massa)", PinRole.GND, PinSide.BOTTOM, 0.7f)
            )
        )

        val koil = MotorComponent(
            id = koilId,
            name = "Koil Pengapian",
            codeModel = "Koil Honda MP / Grand",
            type = ComponentType.KOIL,
            x = 720f,
            y = 200f,
            width = 170f,
            height = 110f,
            pins = listOf(
                ComponentPin("koil-in", "Input dari CDI", PinRole.COIL_OUT, PinSide.LEFT, 0.4f),
                ComponentPin("koil-gnd", "Massa Koil", PinRole.GND, PinSide.BOTTOM, 0.5f),
                ComponentPin("koil-out", "Kop Busi", PinRole.SPARK_OUT, PinSide.RIGHT, 0.5f)
            )
        )

        val busi = MotorComponent(
            id = busiId,
            name = "Busi Motor",
            codeModel = "NGK C7HSA",
            type = ComponentType.BUSI,
            x = 980f,
            y = 200f,
            width = 150f,
            height = 90f,
            pins = listOf(
                ComponentPin("busi-terminal", "Terminal Busi", PinRole.SPARK_OUT, PinSide.LEFT, 0.5f),
                ComponentPin("busi-gnd", "Massa Blok Mesin", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val cables = mutableListOf<WiringCable>()

        fun addCable(fromC: String, fromP: String, toC: String, toP: String, role: PinRole) {
            val color = ManufacturerColorProfile.getStandardColor(Manufacturer.HONDA, role)
            cables.add(
                WiringCable(
                    id = UUID.randomUUID().toString(),
                    fromComponentId = fromC,
                    fromPinId = fromP,
                    toComponentId = toC,
                    toPinId = toP,
                    wireColorHex = color.primaryHex,
                    stripeColorHex = color.stripeHex,
                    label = color.indonesianName
                )
            )
        }

        addCable(spulId, "spul-ac-api", cdiId, "cdi-spul", PinRole.AC_IGNITION_SOURCE)
        addCable(spulId, "spul-pulser", cdiId, "cdi-pulser", PinRole.PULSER)
        addCable(cdiId, "cdi-koil", koilId, "koil-in", PinRole.COIL_OUT)
        addCable(koilId, "koil-out", busiId, "busi-terminal", PinRole.SPARK_OUT)
        addCable(cdiId, "cdi-stop", kontakId, "kontak-stop", PinRole.ENGINE_STOP)
        addCable(spulId, "spul-massa", cdiId, "cdi-massa", PinRole.GND)

        return WiringDiagram(
            id = "preset-grand-ac",
            title = "Pengapian AC Honda Grand / Astrea / Supra",
            description = "Skema pengapian AC murni dari spul jalan tanpa memerlukan aki. Mematikan mesin dengan menghubungkan Engine Stop ke Massa.",
            manufacturer = Manufacturer.HONDA,
            components = listOf(spul, cdi, kontak, koil, busi),
            cables = cables
        )
    }

    fun createFullwaveChargingSystem(): WiringDiagram {
        val spulId = "spul-fullwave-1"
        val kiprokId = "kiprok-tiger-1"
        val fuseId = "fuse-fullwave-1"
        val akiId = "aki-fullwave-1"
        val kontakId = "kontak-fullwave-1"

        val spul = MotorComponent(
            id = spulId,
            name = "Stator 1-Fasa Fullwave",
            codeModel = "Spul Gulungan Floating Ground",
            type = ComponentType.SPUL,
            x = 80f,
            y = 200f,
            width = 190f,
            height = 120f,
            pins = listOf(
                ComponentPin("spul-ac1", "Fasa AC 1 (Kuning)", PinRole.CHARGE_AC, PinSide.RIGHT, 0.35f),
                ComponentPin("spul-ac2", "Fasa AC 2 (Putih)", PinRole.CHARGE_AC, PinSide.RIGHT, 0.70f)
            )
        )

        val kiprok = MotorComponent(
            id = kiprokId,
            name = "Kiprok Fullwave Tiger",
            codeModel = "Shindengen SH634A-12",
            type = ComponentType.KIPROK,
            x = 420f,
            y = 200f,
            width = 200f,
            height = 150f,
            pins = listOf(
                ComponentPin("kiprok-in1", "Input AC 1 (Kuning)", PinRole.CHARGE_AC, PinSide.LEFT, 0.35f),
                ComponentPin("kiprok-in2", "Input AC 2 (Putih)", PinRole.CHARGE_AC, PinSide.LEFT, 0.70f),
                ComponentPin("kiprok-out", "Out +12V DC Pengisian", PinRole.BATTERY_12V, PinSide.RIGHT, 0.35f),
                ComponentPin("kiprok-sense", "Kabel Sensor Kontak", PinRole.VOLTAGE_MONITOR, PinSide.TOP, 0.5f),
                ComponentPin("kiprok-massa", "Massa Kiprok", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val fuse = MotorComponent(
            id = fuseId,
            name = "Sekring Pengisian",
            codeModel = "Fuse Blade 15A Biru",
            type = ComponentType.FUSE,
            x = 710f,
            y = 200f,
            width = 160f,
            height = 90f,
            pins = listOf(
                ComponentPin("fuse-in", "In dari Kiprok", PinRole.BATTERY_12V, PinSide.LEFT, 0.5f),
                ComponentPin("fuse-out", "Out ke Aki", PinRole.BATTERY_12V, PinSide.RIGHT, 0.5f)
            )
        )

        val aki = MotorComponent(
            id = akiId,
            name = "Aki Motor",
            codeModel = "GTZ7S / 12V 6Ah",
            type = ComponentType.AKI,
            x = 960f,
            y = 200f,
            width = 170f,
            height = 110f,
            pins = listOf(
                ComponentPin("aki-pos", "+12V Aki", PinRole.BATTERY_12V, PinSide.LEFT, 0.35f),
                ComponentPin("aki-neg", "Massa Aki (-)", PinRole.GND, PinSide.BOTTOM, 0.5f)
            )
        )

        val kontak = MotorComponent(
            id = kontakId,
            name = "Kunci Kontak",
            codeModel = "Kontak Pengaktif Sensor",
            type = ComponentType.KONTAK,
            x = 420f,
            y = 40f,
            width = 190f,
            height = 90f,
            pins = listOf(
                ComponentPin("kontak-in", "Input 12V Aki", PinRole.BATTERY_12V, PinSide.RIGHT, 0.5f),
                ComponentPin("kontak-out", "Out 12V ke Sensor", PinRole.VOLTAGE_MONITOR, PinSide.BOTTOM, 0.5f)
            )
        )

        val cables = mutableListOf<WiringCable>()

        fun addCable(fromC: String, fromP: String, toC: String, toP: String, role: PinRole) {
            val color = ManufacturerColorProfile.getStandardColor(Manufacturer.HONDA, role)
            cables.add(
                WiringCable(
                    id = UUID.randomUUID().toString(),
                    fromComponentId = fromC,
                    fromPinId = fromP,
                    toComponentId = toC,
                    toPinId = toP,
                    wireColorHex = color.primaryHex,
                    stripeColorHex = color.stripeHex,
                    label = color.indonesianName
                )
            )
        }

        addCable(spulId, "spul-ac1", kiprokId, "kiprok-in1", PinRole.CHARGE_AC)
        addCable(spulId, "spul-ac2", kiprokId, "kiprok-in2", PinRole.CHARGE_AC)
        addCable(kiprokId, "kiprok-out", fuseId, "fuse-in", PinRole.BATTERY_12V)
        addCable(fuseId, "fuse-out", akiId, "aki-pos", PinRole.BATTERY_12V)
        addCable(kontakId, "kontak-out", kiprokId, "kiprok-sense", PinRole.VOLTAGE_MONITOR)
        addCable(kiprokId, "kiprok-massa", akiId, "aki-neg", PinRole.GND)

        return WiringDiagram(
            id = "preset-fullwave-tiger",
            title = "Pengisian & Penerangan Fullwave (Kiprok Tiger)",
            description = "Sistem pengisian fullwave 1-fasa gulungan spul floating. Output DC disalurkan ke Aki dengan sensor voltase pasca-kontak.",
            manufacturer = Manufacturer.HONDA,
            components = listOf(spul, kiprok, fuse, aki, kontak),
            cables = cables
        )
    }

    fun getAllPresets(): List<WiringDiagram> {
        return listOf(
            createShogunKeboDCIgnition(),
            createGrandACIgnition(),
            createFullwaveChargingSystem()
        )
    }
}
