package com.mekanik.wiring.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekanik.wiring.data.repository.WiringRepository
import com.mekanik.wiring.data.updater.UpdateManager
import com.mekanik.wiring.data.updater.UpdateState
import com.mekanik.wiring.domain.model.ComponentPin
import com.mekanik.wiring.domain.model.ComponentType
import com.mekanik.wiring.domain.model.Manufacturer
import com.mekanik.wiring.domain.model.ManufacturerColorProfile
import com.mekanik.wiring.domain.model.MotorComponent
import com.mekanik.wiring.domain.model.PinRole
import com.mekanik.wiring.domain.model.PinSide
import com.mekanik.wiring.domain.model.WiringCable
import com.mekanik.wiring.domain.model.WiringDiagram
import com.mekanik.wiring.domain.preset.PresetWiringFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class WiringViewModel(
    private val repository: WiringRepository,
    private val updateManager: UpdateManager? = null
) : ViewModel() {

    val updateState: StateFlow<UpdateState> = updateManager?.updateState
        ?: MutableStateFlow(UpdateState.Idle).asStateFlow()

    private val initialDiagram = PresetWiringFactory.createShogunKeboDCIgnition()

    private val _uiState = MutableStateFlow(
        WiringUiState(
            currentDiagram = initialDiagram,
            activeManufacturer = initialDiagram.manufacturer,
            diagnosticReport = repository.runCircuitDiagnostic(initialDiagram)
        )
    )
    val uiState: StateFlow<WiringUiState> = _uiState.asStateFlow()

    init {
        observeSavedDiagrams()
    }

    private fun observeSavedDiagrams() {
        viewModelScope.launch {
            repository.getAllSavedDiagrams().collect { saved ->
                _uiState.update { it.copy(savedDiagrams = saved) }
            }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            updateManager?.checkForUpdate()
        }
    }

    fun downloadUpdate(downloadUrl: String, expectedBytes: Long) {
        viewModelScope.launch {
            updateManager?.downloadAndVerify(downloadUrl, expectedBytes)
        }
    }

    fun installUpdate(apkFile: java.io.File) {
        updateManager?.triggerInstall(apkFile)
    }

    fun dismissUpdate() {
        // Handled via state or reset if needed
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun setManufacturer(m: Manufacturer) {
        _uiState.update { current ->
            val updatedCables = current.currentDiagram.cables.map { cable ->
                val fromComp = current.currentDiagram.components.find { it.id == cable.fromComponentId }
                val fromPin = fromComp?.pins?.find { it.id == cable.fromPinId }
                val role = fromPin?.role ?: PinRole.GND
                val color = ManufacturerColorProfile.getStandardColor(m, role)
                cable.copy(
                    wireColorHex = color.primaryHex,
                    stripeColorHex = color.stripeHex,
                    label = color.indonesianName
                )
            }
            val newDiagram = current.currentDiagram.copy(
                manufacturer = m,
                cables = updatedCables,
                updatedAt = System.currentTimeMillis()
            )
            current.copy(
                activeManufacturer = m,
                currentDiagram = newDiagram,
                snackbarMessage = "Profil warna diubah ke ${m.label}"
            )
        }
    }

    fun loadPreset(preset: WiringDiagram) {
        _uiState.update {
            it.copy(
                currentDiagram = preset,
                activeManufacturer = preset.manufacturer,
                selectedWireId = null,
                highlightedNetPinIds = emptySet(),
                diagnosticReport = repository.runCircuitDiagnostic(preset),
                activeSheet = ActiveSheet.NONE,
                snackbarMessage = "Preset '${preset.title}' dimuat"
            )
        }
    }

    fun moveComponent(componentId: String, newX: Float, newY: Float) {
        _uiState.update { state ->
            val updated = state.currentDiagram.components.map {
                if (it.id == componentId) it.copy(x = newX, y = newY) else it
            }
            state.copy(
                currentDiagram = state.currentDiagram.copy(
                    components = updated,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addWire(fromC: String, fromP: String, toC: String, toP: String) {
        val currentDiagram = _uiState.value.currentDiagram
        val exists = currentDiagram.cables.any {
            (it.fromComponentId == fromC && it.fromPinId == fromP && it.toComponentId == toC && it.toPinId == toP) ||
            (it.fromComponentId == toC && it.fromPinId == toP && it.toComponentId == fromC && it.toPinId == fromP)
        }
        if (exists) {
            _uiState.update { it.copy(snackbarMessage = "Kabel tersebut sudah terhubung!") }
            return
        }

        val fromComp = currentDiagram.components.find { it.id == fromC }
        val pin = fromComp?.pins?.find { it.id == fromP }
        val role = pin?.role ?: PinRole.GND
        val wireColor = ManufacturerColorProfile.getStandardColor(_uiState.value.activeManufacturer, role)

        val newCable = WiringCable(
            id = UUID.randomUUID().toString(),
            fromComponentId = fromC,
            fromPinId = fromP,
            toComponentId = toC,
            toPinId = toP,
            wireColorHex = wireColor.primaryHex,
            stripeColorHex = wireColor.stripeHex,
            label = wireColor.indonesianName
        )

        val updatedCables = currentDiagram.cables + newCable
        val updatedDiagram = currentDiagram.copy(
            cables = updatedCables,
            updatedAt = System.currentTimeMillis()
        )

        val diagnostic = repository.runCircuitDiagnostic(updatedDiagram)

        _uiState.update {
            it.copy(
                currentDiagram = updatedDiagram,
                diagnosticReport = diagnostic,
                snackbarMessage = "Kabel baru tersambung: ${wireColor.indonesianName}"
            )
        }
    }

    fun selectWire(wireId: String?) {
        _uiState.update { state ->
            if (wireId == null) {
                state.copy(selectedWireId = null, highlightedNetPinIds = emptySet())
            } else {
                val wire = state.currentDiagram.cables.find { it.id == wireId }
                val pins = if (wire != null) {
                    setOf("${wire.fromComponentId}:${wire.fromPinId}", "${wire.toComponentId}:${wire.toPinId}")
                } else emptySet()
                state.copy(selectedWireId = wireId, highlightedNetPinIds = pins)
            }
        }
    }

    fun deleteSelectedWire() {
        val selected = _uiState.value.selectedWireId ?: return
        val updatedCables = _uiState.value.currentDiagram.cables.filterNot { it.id == selected }
        val updatedDiagram = _uiState.value.currentDiagram.copy(
            cables = updatedCables,
            updatedAt = System.currentTimeMillis()
        )
        val diagnostic = repository.runCircuitDiagnostic(updatedDiagram)

        _uiState.update {
            it.copy(
                currentDiagram = updatedDiagram,
                selectedWireId = null,
                highlightedNetPinIds = emptySet(),
                diagnosticReport = diagnostic,
                snackbarMessage = "Kabel dilepas"
            )
        }
    }

    fun tracePinNet(componentId: String, pinId: String) {
        val key = "$componentId:$pinId"
        val state = _uiState.value
        if (state.highlightedNetPinIds.contains(key)) {
            _uiState.update { it.copy(highlightedNetPinIds = emptySet(), selectedWireId = null) }
            return
        }

        val netPins = mutableSetOf(key)
        var addedMore = true
        while (addedMore) {
            addedMore = false
            for (cable in state.currentDiagram.cables) {
                val p1 = "${cable.fromComponentId}:${cable.fromPinId}"
                val p2 = "${cable.toComponentId}:${cable.toPinId}"
                if (netPins.contains(p1) && !netPins.contains(p2)) {
                    netPins.add(p2)
                    addedMore = true
                } else if (netPins.contains(p2) && !netPins.contains(p1)) {
                    netPins.add(p1)
                    addedMore = true
                }
            }
        }

        _uiState.update {
            it.copy(
                highlightedNetPinIds = netPins,
                selectedWireId = null,
                snackbarMessage = "Menyorot ${netPins.size} titik terminal dalam satu jalur"
            )
        }
    }

    fun addComponent(type: ComponentType) {
        val compId = "comp-${UUID.randomUUID().toString().take(6)}"
        val comp = createDefaultComponent(type, compId, 200f, 200f)

        val updatedComponents = _uiState.value.currentDiagram.components + comp
        val updatedDiagram = _uiState.value.currentDiagram.copy(
            components = updatedComponents,
            updatedAt = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                currentDiagram = updatedDiagram,
                activeSheet = ActiveSheet.NONE,
                snackbarMessage = "Komponen ${type.displayName} ditambahkan ke kanvas"
            )
        }
    }

    fun removeComponent(componentId: String) {
        val current = _uiState.value.currentDiagram
        val updatedComponents = current.components.filterNot { it.id == componentId }
        val updatedCables = current.cables.filterNot {
            it.fromComponentId == componentId || it.toComponentId == componentId
        }
        val updatedDiagram = current.copy(
            components = updatedComponents,
            cables = updatedCables,
            updatedAt = System.currentTimeMillis()
        )
        val diagnostic = repository.runCircuitDiagnostic(updatedDiagram)

        _uiState.update {
            it.copy(
                currentDiagram = updatedDiagram,
                diagnosticReport = diagnostic,
                snackbarMessage = "Komponen dihapus dari kanvas"
            )
        }
    }

    fun saveCurrentDiagram() {
        viewModelScope.launch {
            val result = repository.saveDiagram(_uiState.value.currentDiagram)
            if (result.isSuccess) {
                _uiState.update { it.copy(snackbarMessage = "Skema berhasil disimpan ke penyimpanan lokal") }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Gagal menyimpan skema") }
            }
        }
    }

    fun exportJson(): String {
        return repository.exportDiagramJson(_uiState.value.currentDiagram)
    }

    fun importJson(jsonStr: String) {
        val result = repository.importDiagramJson(jsonStr)
        result.onSuccess { imported ->
            _uiState.update { currentState ->
                currentState.copy(
                    currentDiagram = imported,
                    activeManufacturer = imported.manufacturer,
                    selectedWireId = null,
                    highlightedNetPinIds = emptySet(),
                    diagnosticReport = repository.runCircuitDiagnostic(imported),
                    activeSheet = ActiveSheet.NONE,
                    snackbarMessage = "Skema '${imported.title}' berhasil diimpor"
                )
            }
        }.onFailure {
            _uiState.update { currentState ->
                currentState.copy(snackbarMessage = "Gagal mengimpor: format JSON tidak valid")
            }
        }
    }

    private fun createDefaultComponent(type: ComponentType, id: String, x: Float, y: Float): MotorComponent {
        return when (type) {
            ComponentType.CDI -> MotorComponent(
                id = id,
                name = "CDI Kustom",
                codeModel = "Universal Racing DC",
                type = ComponentType.CDI,
                x = x,
                y = y,
                width = 190f,
                height = 120f,
                pins = listOf(
                    ComponentPin("$id-p1", "Pulser (+)", PinRole.PULSER, PinSide.LEFT, 0.4f),
                    ComponentPin("$id-p2", "12V Kontak", PinRole.SWITCHED_12V, PinSide.TOP, 0.5f),
                    ComponentPin("$id-p3", "Massa", PinRole.GND, PinSide.BOTTOM, 0.5f),
                    ComponentPin("$id-p4", "Koil Out", PinRole.COIL_OUT, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.KIPROK -> MotorComponent(
                id = id,
                name = "Kiprok Regulator",
                codeModel = "Fullwave / Halfwave",
                type = ComponentType.KIPROK,
                x = x,
                y = y,
                width = 190f,
                height = 130f,
                pins = listOf(
                    ComponentPin("$id-k1", "Spul In 1", PinRole.CHARGE_AC, PinSide.LEFT, 0.35f),
                    ComponentPin("$id-k2", "Spul In 2", PinRole.CHARGE_AC, PinSide.LEFT, 0.70f),
                    ComponentPin("$id-k3", "+12V Aki", PinRole.BATTERY_12V, PinSide.RIGHT, 0.5f),
                    ComponentPin("$id-k4", "Massa", PinRole.GND, PinSide.BOTTOM, 0.5f)
                )
            )
            ComponentType.SPUL -> MotorComponent(
                id = id,
                name = "Stator & Spul",
                codeModel = "Spul Pengisian/Pulser",
                type = ComponentType.SPUL,
                x = x,
                y = y,
                width = 180f,
                height = 120f,
                pins = listOf(
                    ComponentPin("$id-s1", "Pulser (+)", PinRole.PULSER, PinSide.RIGHT, 0.35f),
                    ComponentPin("$id-s2", "Spul Api AC", PinRole.AC_IGNITION_SOURCE, PinSide.RIGHT, 0.70f),
                    ComponentPin("$id-s3", "Massa", PinRole.GND, PinSide.BOTTOM, 0.5f)
                )
            )
            ComponentType.KOIL -> MotorComponent(
                id = id,
                name = "Koil Pengapian",
                codeModel = "High Energy Ignition Coil",
                type = ComponentType.KOIL,
                x = x,
                y = y,
                width = 160f,
                height = 110f,
                pins = listOf(
                    ComponentPin("$id-c1", "Input CDI", PinRole.COIL_OUT, PinSide.LEFT, 0.4f),
                    ComponentPin("$id-c2", "Massa Koil", PinRole.GND, PinSide.BOTTOM, 0.5f),
                    ComponentPin("$id-c3", "Kop Busi", PinRole.SPARK_OUT, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.AKI -> MotorComponent(
                id = id,
                name = "Aki 12V",
                codeModel = "Aki Kering MF",
                type = ComponentType.AKI,
                x = x,
                y = y,
                width = 160f,
                height = 100f,
                pins = listOf(
                    ComponentPin("$id-b1", "+12V Terminal", PinRole.BATTERY_12V, PinSide.RIGHT, 0.4f),
                    ComponentPin("$id-b2", "Massa (-)", PinRole.GND, PinSide.BOTTOM, 0.5f)
                )
            )
            ComponentType.KONTAK -> MotorComponent(
                id = id,
                name = "Kunci Kontak",
                codeModel = "Main Switch ON/OFF",
                type = ComponentType.KONTAK,
                x = x,
                y = y,
                width = 170f,
                height = 90f,
                pins = listOf(
                    ComponentPin("$id-sw1", "12V In", PinRole.BATTERY_12V, PinSide.LEFT, 0.5f),
                    ComponentPin("$id-sw2", "12V Out", PinRole.SWITCHED_12V, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.SAKLAR -> MotorComponent(
                id = id,
                name = "Saklar Handle",
                codeModel = "Saklar Starter/Engine Stop",
                type = ComponentType.SAKLAR,
                x = x,
                y = y,
                width = 170f,
                height = 90f,
                pins = listOf(
                    ComponentPin("$id-sk1", "Input 12V", PinRole.SWITCHED_12V, PinSide.LEFT, 0.5f),
                    ComponentPin("$id-sk2", "Trigger Out", PinRole.STARTER_TRIGGER, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.BENDIK -> MotorComponent(
                id = id,
                name = "Bendik Starter",
                codeModel = "Starter Relay High-Amp",
                type = ComponentType.BENDIK,
                x = x,
                y = y,
                width = 180f,
                height = 110f,
                pins = listOf(
                    ComponentPin("$id-bn1", "+12V Aki Tebal", PinRole.STARTER_HIGH_AMP, PinSide.LEFT, 0.35f),
                    ComponentPin("$id-bn2", "Ke Dinamo", PinRole.STARTER_HIGH_AMP, PinSide.RIGHT, 0.35f),
                    ComponentPin("$id-bn3", "Trigger (+)", PinRole.STARTER_TRIGGER, PinSide.TOP, 0.5f),
                    ComponentPin("$id-bn4", "Massa (-)", PinRole.GND, PinSide.BOTTOM, 0.5f)
                )
            )
            ComponentType.FLASHER -> MotorComponent(
                id = id,
                name = "Flasher Sein",
                codeModel = "Flasher 12V 2-Pin",
                type = ComponentType.FLASHER,
                x = x,
                y = y,
                width = 150f,
                height = 90f,
                pins = listOf(
                    ComponentPin("$id-fl1", "Pin B (+12V)", PinRole.SWITCHED_12V, PinSide.LEFT, 0.5f),
                    ComponentPin("$id-fl2", "Pin L (Lampu)", PinRole.LIGHT_AC, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.FUSE -> MotorComponent(
                id = id,
                name = "Sekring Pengaman",
                codeModel = "Fuse Blade 15A",
                type = ComponentType.FUSE,
                x = x,
                y = y,
                width = 150f,
                height = 90f,
                pins = listOf(
                    ComponentPin("$id-fs1", "In (+12V)", PinRole.BATTERY_12V, PinSide.LEFT, 0.5f),
                    ComponentPin("$id-fs2", "Out (Beban)", PinRole.BATTERY_12V, PinSide.RIGHT, 0.5f)
                )
            )
            ComponentType.BUSI -> MotorComponent(
                id = id,
                name = "Busi Motor",
                codeModel = "Spark Plug",
                type = ComponentType.BUSI,
                x = x,
                y = y,
                width = 150f,
                height = 90f,
                pins = listOf(
                    ComponentPin("$id-sp1", "Kop Busi", PinRole.SPARK_OUT, PinSide.LEFT, 0.5f),
                    ComponentPin("$id-sp2", "Massa Blok", PinRole.GND, PinSide.BOTTOM, 0.5f)
                )
            )
        }
    }
}
