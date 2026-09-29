package com.example.ui.screens.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.ui.screens.profile.ApiKeyVerificationStatus
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformasiPribadiScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form fields (memory only - Batch 1)
    var namaLengkap by remember { mutableStateOf(user?.name ?: "Budi Santoso") }
    var email by remember { mutableStateOf(user?.email ?: "budi.santoso@email.com") }
    var nomorHp by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAvatarLoaded by remember { mutableStateOf(false) }

    // Sync form dengan user data terbaru dari Room (saat back-stack restore atau user data change)
    LaunchedEffect(user?.name, user?.email) {
        user?.let {
            namaLengkap = it.name
            email = it.email
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri ->
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                val bitmap = AvatarBitmapUtil.decodeBitmapFromUri(context, imageUri)
                if (bitmap != null) {
                    val saved = AvatarBitmapUtil.saveAvatarBitmap(context, bitmap)
                    if (saved) {
                        // Update state on main thread
                        avatarBitmap = bitmap
                    }
                }
            }
        }
    }

    // Load avatar on first render
    LaunchedEffect(Unit) {
        if (!isAvatarLoaded) {
            isAvatarLoaded = true
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                val bitmap = AvatarBitmapUtil.loadAvatarBitmap(context)
                if (bitmap != null) {
                    avatarBitmap = bitmap
                }
            }
        }
    }

    fun showSnackbar(featureName: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Fitur $featureName sedang dikembangkan",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Confirmation dialog for save
    if (uiState.showSaveConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeSaveConfirmationDialog() },
            title = { Text(text = "Simpan Perubahan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen)) },
            text = { Text(text = "Apakah data sudah benar?", style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextPrimary)) },
            confirmButton = {
                        Button(
                            onClick = { viewModel.confirmSaveData(namaLengkap, email) },
                            colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeSaveConfirmationDialog() }) {
                            Text("Batal")
                        }
                    }
        )
    }

    Scaffold(
        containerColor = SakuCreamBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Informasi Pribadi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SakuDarkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SakuCreamBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ===== 1. HEADER - Avatar =====
            AvatarSection(
                name = namaLengkap,
                avatarBitmap = avatarBitmap,
                onChangePhoto = { galleryLauncher.launch("image/*") },
                onDeletePhoto = {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        AvatarBitmapUtil.deleteAvatarFile(context)
                    }
                    avatarBitmap = null
                }
            )

            // ===== 2. DATA DIRI =====
            DataDiriSection(
                namaLengkap = namaLengkap,
                onNamaLengkapChange = { namaLengkap = it },
                email = email,
                onEmailChange = { email = it },
                nomorHp = nomorHp,
                onNomorHpChange = { nomorHp = it },
                tanggalLahir = tanggalLahir,
                onTanggalLahirChange = { tanggalLahir = it }
            )

            // ===== 3. KEAMANAN AKUN =====
            KeamananAkunSection(onShowSnackbar = { showSnackbar(it) })

            // ===== 4. KONFIGURASI API KEY =====
            ApiKeyConfigurationSection(
                uiState = uiState,
                onApiKeyChange = { viewModel.onApiKeyChange(it) },
                onVerify = { viewModel.verifyApiKey() },
                onSave = { viewModel.saveApiKey() },
                onShowSnackbar = { showSnackbar(it) }
            )

            // ===== 5. TOMBOL SIMPAN DI BAWAH FORM =====
            Button(
                onClick = { viewModel.openSaveConfirmationDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen)
            ) {
                Text("Simpan", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AvatarSection(
    name: String,
    avatarBitmap: Bitmap?,
    onChangePhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar dengan initial letter atau bitmap dari galeri
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (avatarBitmap != null) {
                // Tampilkan bitmap dari galeri
                val painter = remember(avatarBitmap) {
                    BitmapPainter(avatarBitmap.asImageBitmap())
                }
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )
            } else {
                // Fallback: initial letter avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SakuDarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "B",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons: Ganti Foto | Hapus Foto
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onChangePhoto,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ganti Foto")
            }

            OutlinedButton(
                onClick = onDeletePhoto,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = SakuExpenseRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hapus Foto", color = SakuExpenseRed)
            }
        }
    }
}

@Composable
fun DataDiriSection(
    namaLengkap: String,
    onNamaLengkapChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    nomorHp: String,
    onNomorHpChange: (String) -> Unit,
    tanggalLahir: String,
    onTanggalLahirChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Data Diri",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SakuDarkGreen
            )
        )

        OutlinedTextField(
            value = namaLengkap,
            onValueChange = onNamaLengkapChange,
            label = { Text("Nama Lengkap") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuDarkGreen,
                unfocusedBorderColor = SakuTextMuted
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuDarkGreen,
                unfocusedBorderColor = SakuTextMuted
            )
        )

        OutlinedTextField(
            value = nomorHp,
            onValueChange = onNomorHpChange,
            label = { Text("Nomor HP (opsional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuDarkGreen,
                unfocusedBorderColor = SakuTextMuted
            )
        )

        OutlinedTextField(
            value = tanggalLahir,
            onValueChange = onTanggalLahirChange,
            label = { Text("Tanggal Lahir (opsional)") },
            placeholder = { Text("DD/MM/YYYY") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuDarkGreen,
                unfocusedBorderColor = SakuTextMuted
            )
        )
    }
}

@Composable
fun KeamananAkunSection(
    onShowSnackbar: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Keamanan Akun",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SakuDarkGreen
            )
        )

        KeamananMenuItem(
            icon = Icons.Default.Lock,
            title = "Ubah Password",
            subtitle = "Ganti kata sandi akun",
            onClick = { onShowSnackbar("Ubah Password") }
        )

        KeamananMenuItem(
            icon = Icons.Default.Key,
            title = "PIN Transaksi",
            subtitle = "Kelola PIN untuk transaksi",
            onClick = { onShowSnackbar("PIN Transaksi") }
        )

        KeamananMenuItem(
            icon = Icons.Default.Fingerprint,
            title = "Biometric / Fingerprint",
            subtitle = "Aktifkan login sidik jari",
            onClick = { onShowSnackbar("Biometric") }
        )
    }
}

@Composable
fun KeamananMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SakuCreamSurface)
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
    }
}

@Composable
fun ApiKeyConfigurationSection(
    uiState: ProfileUiState,
    onApiKeyChange: (String) -> Unit,
    onVerify: () -> Unit,
    onSave: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Konfigurasi API Key",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SakuDarkGreen
            )
        )

        OutlinedTextField(
            value = uiState.geminiApiKeyInput,
            onValueChange = onApiKeyChange,
            label = { Text("API Key Gemini") },
            placeholder = { Text("AIzaSy...") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuDarkGreen,
                unfocusedBorderColor = SakuTextMuted
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onVerify,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Verifikasi")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SakuDarkGreen
                )
            ) {
                Text("Simpan")
            }
        }

        // Verification status display
        val status = uiState.apiKeyVerificationStatus
        val statusColor = when (status) {
            ApiKeyVerificationStatus.Verified -> SakuIncomeGreen
            ApiKeyVerificationStatus.Invalid, ApiKeyVerificationStatus.QuotaExceeded -> SakuExpenseRed
            ApiKeyVerificationStatus.NetworkError -> SakuExpenseRed
            ApiKeyVerificationStatus.Verifying -> SakuGoldAccent
            ApiKeyVerificationStatus.Unknown -> SakuTextMuted
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (status) {
                    ApiKeyVerificationStatus.Verified -> Icons.Default.CheckCircle
                    ApiKeyVerificationStatus.Invalid, ApiKeyVerificationStatus.QuotaExceeded -> Icons.Default.Close
                    ApiKeyVerificationStatus.NetworkError -> Icons.Default.WifiOff
                    ApiKeyVerificationStatus.Verifying -> Icons.Default.HourglassTop
                    ApiKeyVerificationStatus.Unknown -> Icons.Default.Help
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Status: ${status.label}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = statusColor,
                    fontWeight = if (status == ApiKeyVerificationStatus.Verified) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}