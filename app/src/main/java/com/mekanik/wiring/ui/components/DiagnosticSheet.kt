package com.mekanik.wiring.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekanik.wiring.domain.model.CircuitDiagnosticReport
import com.mekanik.wiring.ui.theme.EarthGreen
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary
import com.mekanik.wiring.ui.theme.VoltageRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticSheet(
    report: CircuitDiagnosticReport?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                text = "Pemeriksaan Kontinuitas Sirkuit",
                color = TextPrimary,
                fontSize = 18.sp
            )
            Text(
                text = "Pemeriksaan otomatis untuk memastikan rangkaian siap menyalakan motor.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            HorizontalDivider(color = NeutralBorder)
            Spacer(modifier = Modifier.height(14.dp))

            if (report == null) {
                Text(
                    text = "Belum ada data skema untuk diperiksa.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeutralSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (report.isIgnitionComplete) EarthGreen else VoltageRed,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = report.summaryText,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (report.isIgnitionComplete) "Pengapian siap meletupkan busi" else "Perlu menyambungkan pin yang belum terhubung",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (report.missingConnections.isNotEmpty()) {
                    Text(
                        text = "CATATAN PIN YANG BELUM TERSAMBUNG",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(report.missingConnections) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NeutralSurfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${item.componentName} - ${item.pinName}",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.diagnosticMessage,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Semua pin utama telah terhubung dengan baik.",
                        color = EarthGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}
