package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SakuCard
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToExport: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            item {
                SakuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SakuCreamSurface,
                    cornerRadius = 22.dp,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SakuDarkGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = user?.name ?: "Budi Santoso",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = user?.email ?: "budi.santoso@email.com",
                                style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SakuLightGreen
                            ) {
                                Text(
                                    text = "Offline-First • Enkripsi Lokal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuDarkGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Fitur Cerdas & Integrasi (API-Key & OCR/AI)
            item {
                Text(
                    text = "Konfigurasi Kunci API & AI",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
            }

            item {
                SakuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SakuCreamSurface,
                    cornerRadius = 18.dp,
                    elevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileMenuRow(
                            icon = Icons.Default.Key,
                            title = "Kunci API Gemini / AI Vision",
                            subtitle = if (uiState.isApiKeySaved) "Tersimpan & Terhubung" else "Belum Dikonfigurasi",
                            badgeText = if (uiState.isApiKeySaved) "Aktif" else "Atur",
                            badgeColor = if (uiState.isApiKeySaved) SakuIncomeGreen else SakuGoldAccent,
                            onClick = { viewModel.openApiKeyDialog() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Kunci API disimpan secara aman di perangkat lokal Anda untuk menjalankan fitur pemindaian struk berbasis AI.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SakuTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Section: Konversi Kurs Aset (Asset Currency Conversion)
            item {
                Text(
                    text = "Konversi Kurs Aset Multivaluta",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
            }

            item {
                SakuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SakuCreamSurface,
                    cornerRadius = 18.dp,
                    elevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Estimasi Nilai Total Saldo",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                                )
                                Text(
                                    text = uiState.convertedBalancePreview,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SakuLightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = SakuDarkGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Currencies Selector Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("IDR", "USD", "SGD", "EUR", "JPY").forEach { curr ->
                                val isSelected = uiState.selectedCurrency == curr
                                Surface(
                                    onClick = { viewModel.selectCurrency(curr) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SakuDarkGreen else SakuCreamBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) SakuDarkGreen else SakuCreamBorder
                                    ),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = curr,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else SakuTextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Data & Backup
            item {
                Text(
                    text = "Data & Penyimpanan",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
            }

            item {
                SakuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SakuCreamSurface,
                    cornerRadius = 18.dp,
                    elevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileMenuRow(
                            icon = Icons.Default.CloudDownload,
                            title = "Cadangkan & Ekspor Data",
                            subtitle = "Unduh format CSV & JSON atau pulihkan data",
                            onClick = onNavigateToExport
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ProfileMenuRow(
                            icon = Icons.Default.Refresh,
                            title = "Setel Ulang Data ke Bawaan",
                            subtitle = "Kembalikan saldo dan kantong simulasi default",
                            onClick = { viewModel.openResetDialog() }
                        )
                    }
                }
            }

            // Section: Logout
            item {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuExpenseRed.copy(alpha = 0.1f),
                        contentColor = SakuExpenseRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button")
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar dari Akun", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dialog Konfigurasi Kunci API
        if (uiState.isApiKeyDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeApiKeyDialog() },
                title = {
                    Text(
                        text = "Konfigurasi Kunci API Gemini",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Masukkan Gemini API Key dari Google AI Studio untuk mengaktifkan pemindaian struk otomatis dan OCR multimodal.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                        OutlinedTextField(
                            value = uiState.geminiApiKeyInput,
                            onValueChange = { viewModel.onApiKeyChange(it) },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveApiKey() },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_api_key_button")
                    ) {
                        Text("Simpan Kunci")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeApiKeyDialog() }) {
                        Text("Batal", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }

        // Dialog Reset Data
        if (uiState.isResetDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeResetDialog() },
                title = { Text("Setel Ulang Data?", fontWeight = FontWeight.Bold, color = SakuExpenseRed) },
                text = {
                    Text(
                        "Semua kantong dan transaksi saat ini akan disetel ulang ke contoh data awal Saku.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetData() },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuExpenseRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ya, Setel Ulang")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeResetDialog() }) {
                        Text("Batal", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = SakuDarkGreen,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SakuLightGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SakuDarkGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SakuTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
            )
        }

        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SakuTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
