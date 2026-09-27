package com.example.ui.screens.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SakuCard
import com.example.ui.components.SakuTopBar
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun ExportBackupScreen(
    viewModel: ExportBackupViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                SakuTopBar(
                    title = "Ekspor & Cadangan Data",
                    canNavigateBack = true,
                    onNavigateBack = onNavigateBack
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Security note card
                    SakuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SakuLightGreen,
                        borderColor = SakuDarkGreen.copy(alpha = 0.2f),
                        cornerRadius = 18.dp,
                        elevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SakuDarkGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "100% Kepemilikan Data Pribadi",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Text(
                                    text = "Semua catatan keuangan disimpan di database lokal ponsel Anda. Tidak ada data yang diunggah ke pihak ketiga tanpa izin.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SakuDarkGreen)
                                )
                            }
                        }
                    }

                    if (uiState.statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.isError) SakuExpenseRed.copy(alpha = 0.15f) else SakuIncomeGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.statusMessage ?: "",
                                color = if (uiState.isError) SakuExpenseRed else SakuIncomeGreen,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Export to CSV Section
                    SakuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SakuCreamSurface,
                        cornerRadius = 20.dp,
                        elevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SakuLightGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = SakuDarkGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Ekspor ke Spreadsheet (CSV)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen))
                                    Text("Format tabel universal untuk Excel, Google Sheets, & Numbers", style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.exportToCsv() },
                                colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("export_csv_button"),
                                enabled = !uiState.isExporting
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buat & Unduh Berkas CSV", fontWeight = FontWeight.SemiBold)
                            }

                            if (uiState.exportedCsv != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Pratinjau CSV Transaksi:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextMuted)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SakuCreamBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                                    modifier = Modifier.fillMaxWidth().height(120.dp)
                                ) {
                                    Text(
                                        text = uiState.exportedCsv ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = SakuTextPrimary
                                        ),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Export to JSON Backup Section
                    SakuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SakuCreamSurface,
                        cornerRadius = 20.dp,
                        elevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SakuLightGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = SakuDarkGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Cadangan Lengkap (JSON)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen))
                                    Text("Mencakup semua data kantong, transaksi, dan preferensi", style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.exportToJson() },
                                colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("export_json_button"),
                                enabled = !uiState.isExporting
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buat Berkas Cadangan JSON", fontWeight = FontWeight.SemiBold)
                            }

                            if (uiState.exportedJson != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Pratinjau JSON Cadangan:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextMuted)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SakuCreamBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                                    modifier = Modifier.fillMaxWidth().height(120.dp)
                                ) {
                                    Text(
                                        text = uiState.exportedJson ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = SakuTextPrimary
                                        ),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Restore Section
                    SakuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SakuCreamSurface,
                        cornerRadius = 20.dp,
                        elevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SakuLightGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = SakuDarkGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pulihkan Data (Restore)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen))
                                    Text("Kembalikan data keuangan dari teks JSON cadangan", style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = uiState.restoreInput,
                                onValueChange = { viewModel.onRestoreInputChange(it) },
                                label = { Text("Tempel JSON Cadangan") },
                                placeholder = { Text("{\"appName\": \"Saku\", ...}") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("restore_json_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.restoreFromJson() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("restore_submit_button"),
                                enabled = !uiState.isExporting
                            ) {
                                Text("Mulai Pemulihan Data", color = SakuDarkGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
