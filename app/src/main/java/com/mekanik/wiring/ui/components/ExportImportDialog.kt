package com.mekanik.wiring.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mekanik.wiring.ui.theme.EarthGreen
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceVariant
import com.mekanik.wiring.ui.theme.OledBlack
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary

@Composable
fun ExportImportDialog(
    exportJsonString: String,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isImportMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeutralSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isImportMode) "Impor Skema JSON" else "Ekspor Skema",
                    color = TextPrimary,
                    fontSize = 17.sp
                )
                Text(
                    text = if (isImportMode) "Tempel kode JSON skema kabel untuk dimuat ke kanvas."
                    else "Salin kode skema untuk dibagikan atau dicadangkan.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (isImportMode) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("Tempel JSON di sini...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EarthGreen,
                            unfocusedBorderColor = NeutralBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = exportJsonString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeutralBorder,
                            unfocusedBorderColor = NeutralBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { isImportMode = !isImportMode },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (isImportMode) "Beralih ke Ekspor" else "Beralih ke Impor")
                    }

                    if (isImportMode) {
                        Button(
                            onClick = { onImport(inputText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EarthGreen,
                                contentColor = OledBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Terapkan")
                        }
                    } else {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Mekanik Wiring Diagram", exportJsonString)
                                clipboard.setPrimaryClip(clip)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EarthGreen,
                                contentColor = OledBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Salin Teks")
                        }
                    }
                }
            }
        }
    }
}
