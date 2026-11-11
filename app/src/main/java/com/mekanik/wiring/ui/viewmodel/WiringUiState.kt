package com.mekanik.wiring.ui.viewmodel

import com.mekanik.wiring.domain.model.CircuitDiagnosticReport
import com.mekanik.wiring.domain.model.Manufacturer
import com.mekanik.wiring.domain.model.WiringDiagram

enum class ActiveSheet {
    NONE,
    COMPONENT_PALETTE,
    PRESET_SELECTOR,
    DIAGNOSTIC_REPORT,
    EXPORT_IMPORT
}

data class WiringUiState(
    val currentDiagram: WiringDiagram,
    val savedDiagrams: List<WiringDiagram> = emptyList(),
    val selectedWireId: String? = null,
    val highlightedNetPinIds: Set<String> = emptySet(),
    val activeManufacturer: Manufacturer = Manufacturer.HONDA,
    val diagnosticReport: CircuitDiagnosticReport? = null,
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)
