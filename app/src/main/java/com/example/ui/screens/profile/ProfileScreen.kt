package com.example.ui.screens.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.example.ui.screens.profile.AvatarBitmapUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToExport: () -> Unit,
    onNavigateToInformasiPribadi: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val transactionCount by viewModel.transactionCount.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val avatarBitmap by viewModel.avatarBitmap.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Load avatar when ProfileScreen resumes (e.g., after returning from InformasiPribadi)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAvatarBitmap(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Extract user data with fallback
    val userName = user?.name ?: "Budi Santoso"
    val userEmail = user?.email ?: "budi.santoso@email.com"
    
    // Snackbar setup
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog states
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    // Snackbar helper
    fun showPlaceholderSnackbar(featureName: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Fitur $featureName sedang dikembangkan",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Main Surface with SnackbarHost
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SakuCreamBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== 1. HEADER =====
                item {
                    ProfileHeader(
                        name = userName,
                        email = userEmail,
                        avatarBitmap = avatarBitmap,
                        onNavigateToInformasiPribadi = onNavigateToInformasiPribadi
                    )
                }

                // ===== 2. STATS =====
                item {
                    SectionTitle(title = "Statistik Keuangan")
                }
                item {
                    FinancialStatsCard(
                        summary = summary,
                        transactionCount = transactionCount
                    )
                }

                // ===== 3. AKUN =====
                item {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Akun")
                }
                item {
                    AccountSection(
                        onWalletIconClick = { showCurrencyDialog = true },
                        onShowSnackbar = { showPlaceholderSnackbar(it) }
                    )
                }

                // ===== 4. PENGATURAN APLIKASI =====
                item {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Pengaturan Aplikasi")
                }
                item {
                    AppSettingsSection(onPlaceholderClick = { showPlaceholderSnackbar(it) })
                }

                // ===== 5. BANTUAN =====
                item {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    SectionTitle(title = "Bantuan")
                }
                item {
                    HelpSection(onPlaceholderClick = { showPlaceholderSnackbar(it) })
                }

                // ===== 6. KELUAR AKUN =====
                item {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                    LogoutButton(onLogout = onLogout)
                }
            }

            // SnackbarHost - positioned above bottom bar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Above bottom navigation
            )
        }

        // Dialog Mata Uang (Wallet Icon)
        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = {
                    Text(
                        text = "Pilih Mata Uang",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                text = {
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pilih mata uang default untuk tampilan nominal:",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                        CurrencyOption(
                            currencyName = "Rupiah Indonesia (IDR)",
                            currencySymbol = "Rp",
                            selected = true,
                            onClick = {
                                showCurrencyDialog = false
                                showPlaceholderSnackbar("Mata Uang IDR")
                            }
                        )
                        CurrencyOption(
                            currencyName = "US Dollar (USD)",
                            currencySymbol = "$",
                            selected = false,
                            onClick = {
                                showCurrencyDialog = false
                                showPlaceholderSnackbar("Mata Uang USD")
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showCurrencyDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = SakuTextSecondary)
                    ) {
                        Text("Batal")
                    }
                },
                containerColor = SakuCreamSurface
            )
        }

        // Dialog Backup
        if (showBackupDialog) {
            BackupDialog(
                onExport = {
                    showBackupDialog = false
                    onNavigateToExport()
                },
                onBackupLocal = {
                    showBackupDialog = false
                    showPlaceholderSnackbar("Backup Lokal")
                },
                onBackupDrive = {
                    showBackupDialog = false
                    showPlaceholderSnackbar("Backup ke Drive")
                },
                onDismiss = { showBackupDialog = false }
            )
        }
    }
}