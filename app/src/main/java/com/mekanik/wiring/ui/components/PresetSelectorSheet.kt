package com.mekanik.wiring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekanik.wiring.domain.model.WiringDiagram
import com.mekanik.wiring.domain.preset.PresetWiringFactory
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSelectorSheet(
    savedDiagrams: List<WiringDiagram>,
    onDismiss: () -> Unit,
    onPresetSelected: (WiringDiagram) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val builtInPresets = PresetWiringFactory.getAllPresets()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NeutralSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Katalog Skema & Preset",
                color = TextPrimary,
                fontSize = 18.sp
            )
            Text(
                text = "Pilih rangkaian bawaan teruji atau buka skema tersimpan.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            HorizontalDivider(color = NeutralBorder)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "RANGKAIAN STANDAR PABRIKAN / BALAP",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(builtInPresets) { preset ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeutralSurfaceVariant, RoundedCornerShape(8.dp))
                            .clickable { onPresetSelected(preset) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = preset.manufacturer.label,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (savedDiagrams.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SKEMA TERSIMPAN SAYA",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(savedDiagrams) { saved ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeutralSurfaceVariant, RoundedCornerShape(8.dp))
                                .clickable { onPresetSelected(saved) }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = saved.title,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${saved.components.size} Komponen, ${saved.cables.size} Sambungan Kabel",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
