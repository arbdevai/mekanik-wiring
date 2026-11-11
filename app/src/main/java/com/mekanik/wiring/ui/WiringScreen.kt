package com.mekanik.wiring.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mekanik.wiring.data.updater.UpdateState
import com.mekanik.wiring.ui.canvas.SchematicCanvas
import com.mekanik.wiring.ui.components.ComponentPaletteSheet
import com.mekanik.wiring.ui.components.DiagnosticSheet
import com.mekanik.wiring.ui.components.ExportImportDialog
import com.mekanik.wiring.ui.components.PresetSelectorSheet
import com.mekanik.wiring.ui.components.UpdateDialog
import com.mekanik.wiring.ui.components.WiringTopBar
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.OledBlack
import com.mekanik.wiring.ui.theme.TextSecondary
import com.mekanik.wiring.ui.viewmodel.ActiveSheet as VmActiveSheet
import com.mekanik.wiring.ui.viewmodel.WiringViewModel

@Composable
fun WiringScreen(
    viewModel: WiringViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Available ||
            updateState is UpdateState.Downloading ||
            updateState is UpdateState.ReadyToInstall ||
            updateState is UpdateState.Error
        ) {
            showUpdateDialog = true
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .imePadding(),
        topBar = {
            WiringTopBar(
                title = state.currentDiagram.title,
                currentManufacturer = state.activeManufacturer,
                isIgnitionComplete = state.diagnosticReport?.isIgnitionComplete ?: false,
                isWireSelected = state.selectedWireId != null,
                onManufacturerSelected = { viewModel.setManufacturer(it) },
                onOpenPresets = { viewModel.openSheet(VmActiveSheet.PRESET_SELECTOR) },
                onOpenPalette = { viewModel.openSheet(VmActiveSheet.COMPONENT_PALETTE) },
                onOpenDiagnostic = { viewModel.openSheet(VmActiveSheet.DIAGNOSTIC_REPORT) },
                onSaveDiagram = { viewModel.saveCurrentDiagram() },
                onOpenExport = { viewModel.openSheet(VmActiveSheet.EXPORT_IMPORT) },
                onCheckUpdate = { viewModel.checkForUpdate() },
                onDeleteSelectedWire = { viewModel.deleteSelectedWire() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OledBlack)
        ) {
            // Hint instruction banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Tarik pin terminal ke pin lain untuk menyambung kabel. Ketuk kabel untuk memilih/menghapus.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                SchematicCanvas(
                    modifier = Modifier.fillMaxSize(),
                    components = state.currentDiagram.components,
                    cables = state.currentDiagram.cables,
                    selectedWireId = state.selectedWireId,
                    highlightedNetPinIds = state.highlightedNetPinIds,
                    onComponentMove = { id, x, y -> viewModel.moveComponent(id, x, y) },
                    onWireCreated = { fromC, fromP, toC, toP -> viewModel.addWire(fromC, fromP, toC, toP) },
                    onWireSelected = { wireId -> viewModel.selectWire(wireId) },
                    onPinClicked = { compId, pinId -> viewModel.tracePinNet(compId, pinId) }
                )
            }
        }
    }

    // Modal Sheets and Dialogs
    when (state.activeSheet) {
        VmActiveSheet.COMPONENT_PALETTE -> {
            ComponentPaletteSheet(
                onDismiss = { viewModel.closeSheet() },
                onComponentSelected = { type -> viewModel.addComponent(type) }
            )
        }
        VmActiveSheet.PRESET_SELECTOR -> {
            PresetSelectorSheet(
                savedDiagrams = state.savedDiagrams,
                onDismiss = { viewModel.closeSheet() },
                onPresetSelected = { preset -> viewModel.loadPreset(preset) }
            )
        }
        VmActiveSheet.DIAGNOSTIC_REPORT -> {
            DiagnosticSheet(
                report = state.diagnosticReport,
                onDismiss = { viewModel.closeSheet() }
            )
        }
        VmActiveSheet.EXPORT_IMPORT -> {
            ExportImportDialog(
                exportJsonString = viewModel.exportJson(),
                onDismiss = { viewModel.closeSheet() },
                onImport = { json -> viewModel.importJson(json) }
            )
        }
        VmActiveSheet.NONE -> {}
    }

    if (showUpdateDialog && updateState !is UpdateState.Idle) {
        UpdateDialog(
            updateState = updateState,
            onDismiss = { showUpdateDialog = false },
            onDownload = { url, bytes -> viewModel.downloadUpdate(url, bytes) },
            onInstall = { file -> viewModel.installUpdate(file) }
        )
    }
}
