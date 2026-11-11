package com.mekanik.wiring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekanik.wiring.domain.model.Manufacturer
import com.mekanik.wiring.ui.theme.EarthGreen
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary
import com.mekanik.wiring.ui.theme.VoltageRed

@Composable
fun WiringTopBar(
    title: String,
    currentManufacturer: Manufacturer,
    isIgnitionComplete: Boolean,
    isWireSelected: Boolean,
    onManufacturerSelected: (Manufacturer) -> Unit,
    onOpenPresets: () -> Unit,
    onOpenPalette: () -> Unit,
    onOpenDiagnostic: () -> Unit,
    onSaveDiagram: () -> Unit,
    onOpenExport: () -> Unit,
    onDeleteSelectedWire: () -> Unit
) {
    var brandMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { brandMenuExpanded = true }
                ) {
                    Text(
                        text = "Warna Kabel: ${currentManufacturer.label}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "[Ganti]",
                        color = EarthGreen,
                        fontSize = 11.sp
                    )

                    DropdownMenu(
                        expanded = brandMenuExpanded,
                        onDismissRequest = { brandMenuExpanded = false },
                        modifier = Modifier.background(NeutralSurfaceVariant)
                    ) {
                        Manufacturer.values().forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.label, color = TextPrimary) },
                                onClick = {
                                    onManufacturerSelected(m)
                                    brandMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isWireSelected) {
                    IconButton(onClick = onDeleteSelectedWire) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Kabel",
                            tint = VoltageRed
                        )
                    }
                }

                IconButton(onClick = onOpenDiagnostic) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Diagnostik Jalur",
                            tint = if (isIgnitionComplete) EarthGreen else VoltageRed
                        )
                    }
                }

                IconButton(onClick = onOpenPresets) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Katalog Preset",
                        tint = TextPrimary
                    )
                }

                IconButton(onClick = onOpenPalette) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Komponen",
                        tint = EarthGreen
                    )
                }

                IconButton(onClick = onSaveDiagram) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Simpan Skema",
                        tint = TextPrimary
                    )
                }

                IconButton(onClick = onOpenExport) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Ekspor / Impor",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
