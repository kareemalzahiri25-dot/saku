package com.example.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import com.example.domain.model.FinancialSummary
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.Formatters
import com.example.ui.components.SakuCard
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

// ===== ProfileHeader =====
@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatarBitmap: android.graphics.Bitmap?,
    onNavigateToInformasiPribadi: () -> Unit
) {
    SakuCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToInformasiPribadi)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar dengan bitmap atau initial letter
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    // Display avatar bitmap if available
                    val painter = BitmapPainter(avatarBitmap.asImageBitmap())
                    Image(
                        painter = painter,
                        contentDescription = "Avatar Pengguna",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Fallback to initial letter if no bitmap
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SakuDarkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase() ?: "B",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuTextPrimary
                    )
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ke Informasi Pribadi",
                tint = SakuTextMuted,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        rotationZ = 180f
                    }
            )
        }
    }
}

// ===== SectionTitle =====
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = SakuDarkGreen
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

// ===== FinancialStatsCard =====
@Composable
fun FinancialStatsCard(
    summary: FinancialSummary,
    transactionCount: Int
) {
    SakuCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Asset",
                    value = Formatters.formatRupiah(summary.totalAssetBalance),
                    color = SakuDarkGreen
                )
                StatItem(
                    label = "Total Transaksi",
                    value = if (transactionCount > 0) transactionCount.toString() else "-",
                    color = SakuGoldAccent
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Pemasukan Bulan Ini",
                    value = Formatters.formatRupiah(summary.totalIncomeThisMonth),
                    color = SakuIncomeGreen
                )
                StatItem(
                    label = "Pengeluaran Bulan Ini",
                    value = Formatters.formatRupiah(summary.totalExpenseThisMonth),
                    color = SakuExpenseRed
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
        )
    }
}

// ===== AccountSection =====
@Composable
fun AccountSection(
    onWalletIconClick: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AccountMenuItem(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Dompet",
            subtitle = "Kelola mata uang dan wallet",
            actionIcon = Icons.Default.CurrencyExchange,
            onClick = onWalletIconClick
        )
        AccountMenuItem(
            icon = Icons.Default.Settings,
            title = "Backup Data",
            subtitle = "Ekspor dan cadangkan data",
            actionIcon = Icons.Default.ArrowForward,
            onClick = { onShowSnackbar("Backup Data") }
        )
        AccountMenuItem(
            icon = Icons.Default.Fingerprint,
            title = "Biometric",
            subtitle = "Aktifkan login sidik jari",
            actionIcon = null,
            onClick = { onShowSnackbar("Biometric") },
            trailing = {
                Switch(
                    checked = false,
                    onCheckedChange = { onShowSnackbar("Biometric") },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SakuDarkGreen,
                        checkedTrackColor = SakuLightGreen,
                        uncheckedThumbColor = SakuTextMuted,
                        uncheckedTrackColor = SakuCreamBorder
                    )
                )
            }
        )
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionIcon: ImageVector?,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SakuCreamSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SakuDarkGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = SakuTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SakuTextSecondary
                )
            )
        }
        if (actionIcon != null) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = SakuTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (trailing != null) {
            trailing()
        }
    }
}

// ===== AppSettingsSection =====
@Composable
fun AppSettingsSection(
    onPlaceholderClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsMenuItem(
            icon = Icons.Default.Palette,
            title = "Tema",
            subtitle = "Light/Dark mode",
            onClick = { onPlaceholderClick("Tema") }
        )
        SettingsMenuItem(
            icon = Icons.Default.Language,
            title = "Bahasa",
            subtitle = "Indonesia/English",
            onClick = { onPlaceholderClick("Bahasa") }
        )
        SettingsMenuItem(
            icon = Icons.Default.Notifications,
            title = "Notifikasi",
            subtitle = "Pengaturan notifikasi",
            onClick = { onPlaceholderClick("Notifikasi") }
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SakuCreamSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SakuDarkGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = SakuTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SakuTextSecondary
                )
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SakuTextMuted,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    rotationZ = 180f
                }
        )
    }
}

// ===== HelpSection =====
@Composable
fun HelpSection(
    onPlaceholderClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HelpMenuItem(
            icon = Icons.Default.Help,
            title = "Bantuan",
            onClick = { onPlaceholderClick("Bantuan") }
        )
        HelpMenuItem(
            icon = Icons.Default.Info,
            title = "Tentang",
            onClick = { onPlaceholderClick("Tentang") }
        )
        HelpMenuItem(
            icon = Icons.Default.ReceiptLong,
            title = "Cara Menggunakan",
            onClick = { onPlaceholderClick("Cara Menggunakan") }
        )
    }
}

@Composable
fun HelpMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SakuCreamSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SakuDarkGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = SakuTextPrimary
            )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SakuTextMuted,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    rotationZ = 180f
                }
        )
    }
}

// ===== LogoutButton =====
@Composable
fun LogoutButton(
    onLogout: () -> Unit
) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SakuExpenseRed,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Keluar Akun",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// ===== CurrencyOption (untuk dialog wallet) =====
@Composable
fun CurrencyOption(
    currencyName: String,
    currencySymbol: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SakuLightGreen else SakuCreamSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected) SakuDarkGreen else SakuTextMuted),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currencySymbol,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = currencyName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = if (selected) SakuDarkGreen else SakuTextPrimary
            )
        )
    }
}

// ===== BackupDialog =====
@Composable
fun BackupDialog(
    onExport: () -> Unit,
    onBackupLocal: () -> Unit,
    onBackupDrive: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadangkan Data",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pilih metode backup untuk data transaksi dan kantong Anda:",
                    style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                )
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuDarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Ekspor CSV/JSON")
                }
                OutlinedButton(
                    onClick = onBackupLocal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Backup Lokal")
                }
                OutlinedButton(
                    onClick = onBackupDrive,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Backup ke Google Drive")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = SakuTextSecondary)
            ) {
                Text("Tutup")
            }
        },
        containerColor = SakuCreamSurface
    )
}