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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekanik.wiring.domain.model.ComponentType
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.OledBlack
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentPaletteSheet(
    onDismiss: () -> Unit,
    onComponentSelected: (ComponentType) -> Unit
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
                text = "Pilih Komponen Kelistrikan",
                color = TextPrimary,
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
            Text(
                text = "Ketuk untuk menambahkan komponen ke area kanvas kerja.",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ComponentType.values()) { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeutralSurfaceVariant, RoundedCornerShape(8.dp))
                            .clickable { onComponentSelected(type) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = type.displayName,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = getComponentDescription(type),
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

private fun getComponentDescription(type: ComponentType): String {
    return when (type) {
        ComponentType.CDI -> "Unit pengatur waktu dan pelepasan arus ke koil pengapian"
        ComponentType.KIPROK -> "Penyearah arus AC ke DC sekaligus pembatas voltase pengisian"
        ComponentType.SPUL -> "Pembangkit arus listrik magnet dan sensor titik pulser"
        ComponentType.KOIL -> "Transformator peninggi tegangan untuk percikan api busi"
        ComponentType.AKI -> "Sumber arus listrik DC 12 Volt penyimpan daya"
        ComponentType.KONTAK -> "Saklar utama pemutus dan penghubung arus kelistrikan"
        ComponentType.SAKLAR -> "Saklar pengendali starter, engine stop, atau lampu"
        ComponentType.BENDIK -> "Relay arus tinggi penghubung aki langsung ke dinamo starter"
        ComponentType.FLASHER -> "Relay pembuat kedipan lampu sein"
        ComponentType.FUSE -> "Sekring pemutus pengaman arus berlebih / korsleting"
        ComponentType.BUSI -> "Titik akhir percikan api pembakaran di ruang mesin"
    }
}
