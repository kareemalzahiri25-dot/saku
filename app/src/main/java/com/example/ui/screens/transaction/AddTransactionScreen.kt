package com.example.ui.screens.transaction

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.core.ThousandSeparatorTransformation
import com.example.domain.model.TransactionType
import com.example.domain.model.Asset
import com.example.ui.components.SakuCard
import com.example.ui.components.SakuTopBar
import com.example.ui.components.getCategoryIconVector
import com.example.ui.screens.profile.ApiKeyVerificationStatus
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuCreamSurfaceVariant
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuExpenseRedBg
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuIncomeGreenBg
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale

private fun createTempReceiptImageUri(context: Context): Uri {
    val tempDir = File(context.cacheDir, "receipts").apply { mkdirs() }
    val file = File(tempDir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val activeAssets by viewModel.activeAssets.collectAsStateWithLifecycle()
    val selectedAssetId by viewModel.selectedAssetId.collectAsStateWithLifecycle("")
    val scrollState = rememberScrollState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredCategories = categories.filter { it.type == formState.type }

    // Uri camera temporer
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val uri = tempCameraUri!!
            val bytes = readBytesFromUri(context, uri)
            if (bytes != null && bytes.isNotEmpty()) {
                viewModel.processReceipt(bytes, uri.toString())
            } else {
                viewModel.onScanError("Gagal membaca hasil foto dari kamera.")
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bytes = readBytesFromUri(context, uri)
            if (bytes != null && bytes.isNotEmpty()) {
                viewModel.processReceipt(bytes, uri.toString())
            } else {
                viewModel.onScanError("Gagal membaca file gambar dari galeri.")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempReceiptImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            viewModel.onCameraPermissionDenied()
        }
    }

    fun launchCameraFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = createTempReceiptImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGalleryFlow() {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun proceedWithChosenSource() {
        if (formState.selectedScanSource == ReceiptSource.CAMERA) {
            launchCameraFlow()
        } else {
            launchGalleryFlow()
        }
    }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = formState.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SakuCreamSurface)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                // Divider top
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SakuCreamBorder,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                val isFormValid = formState.amountString.isNotBlank() &&
                        formState.title.isNotBlank() &&
                        formState.selectedCategoryId.isNotBlank() &&
                        formState.selectedPocketId.isNotBlank() &&
                        selectedAssetId.isNotEmpty()

                Button(
                    onClick = { viewModel.saveTransaction(onNavigateBack) },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuDarkGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SakuCreamBorder,
                        disabledContentColor = SakuTextMuted
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = "Simpan Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Main content with bottom padding for sticky save button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
        ) {
            SakuTopBar(
                title = "Catat Transaksi",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // 1. Transaction Type Segmented Switcher
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SakuCreamSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isExpense = formState.type == TransactionType.EXPENSE
                        Surface(
                            onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExpense) SakuExpenseRed else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("tab_expense")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Pengeluaran",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpense) Color.White else SakuTextSecondary
                                    )
                                )
                            }
                        }

                        val isIncome = formState.type == TransactionType.INCOME
                        Surface(
                            onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIncome) SakuIncomeGreen else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("tab_income")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Pemasukan",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncome) Color.White else SakuTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Nominal + 3. Quick amount in one section
                SakuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SakuCreamSurface,
                    cornerRadius = 22.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nominal Transaksi",
                            style = MaterialTheme.typography.labelMedium.copy(color = SakuTextSecondary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Rp",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = formState.amountString,
                                onValueChange = { viewModel.onAmountChange(it) },
                                placeholder = { Text("0", fontSize = 28.sp, color = SakuTextMuted) },
                                singleLine = true,
                                visualTransformation = ThousandSeparatorTransformation,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen,
                                    textAlign = TextAlign.Start
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transaction_amount_input")
                            )
                        }

                        // Quick Increment Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            listOf(20000L, 50000L, 100000L, 500000L).forEach { addVal ->
                                Surface(
                                    onClick = {
                                        val cur = Formatters.parseAmount(formState.amountString)
                                        viewModel.onAmountChange((cur + addVal).toLong().toString())
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = SakuLightGreen,
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = "+${addVal / 1000}rb",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SakuDarkGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Kategori Selector
                Text(
                    text = "Pilih Kategori",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCategories) { cat ->
                        val isSelected = cat.id == formState.selectedCategoryId
                        Surface(
                            onClick = { viewModel.onCategorySelect(cat.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SakuDarkGreen else SakuCreamBorder
                            ),
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier.testTag("cat_chip_${cat.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getCategoryIconVector(cat.icon),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SakuDarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SakuTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Aset Selector
                Text(
                    text = "Pilih Aset",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (activeAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada aset. Tambahkan aset dulu.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SakuTextMuted
                            )
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeAssets) { asset ->
                            val isSelected = asset.id == selectedAssetId
                            Surface(
                                onClick = { viewModel.onAssetSelect(asset.id, asset.name) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SakuDarkGreen else SakuCreamBorder
                                ),
                                shadowElevation = if (isSelected) 3.dp else 1.dp,
                                modifier = Modifier.testTag("asset_chip_${asset.id}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = asset.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else SakuTextPrimary
                                        )
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(asset.balance),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else SakuTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Alokasi Kantong (Pengeluaran only)
                AnimatedVisibility(visible = formState.type == TransactionType.EXPENSE) {
                    Column {
                        Text(
                            text = "Alokasi ke Kantong",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pockets) { pocket ->
                                val isSelected = pocket.id == formState.selectedPocketId
                                Surface(
                                    onClick = { viewModel.onPocketSelect(pocket.id) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) SakuDarkGreen else SakuCreamBorder
                                    ),
                                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                                    modifier = Modifier.testTag("pocket_chip_${pocket.id}")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(
                                            text = pocket.name,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else SakuTextPrimary
                                            )
                                        )
                                        Text(
                                            text = Formatters.formatRupiah(pocket.targetAmount),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else SakuTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. Date Picker
                Column {
                    Text(
                        text = "Tanggal Transaksi",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date display field
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = formState.selectedDate.format(dateFormatter)
                    
                    Surface(
                        onClick = { viewModel.onToggleDatePicker() },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅",
                                fontSize = 20.sp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = SakuTextPrimary
                                )
                            )
                        }
                    }

                    // Date picker dialog
                    if (formState.isDatePickerOpen) {
                        DatePickerDialog(
                            onDismissRequest = { viewModel.dismissDatePicker() },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = LocalDate.ofInstant(
                                                java.time.Instant.ofEpochMilli(millis),
                                                ZoneId.systemDefault()
                                            )
                                            viewModel.onDateSelect(date)
                                        }
                                    }
                                ) {
                                    Text("Pilih", color = SakuDarkGreen)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { viewModel.dismissDatePicker() }
                                ) {
                                    Text("Batal", color = SakuTextSecondary)
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. Keterangan (Title)
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Nama / Keterangan Transaksi") },
                    placeholder = { Text("misal: Makan Siang Nasi Padang, Belanja Mingguan") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SakuDarkGreen,
                        unfocusedBorderColor = SakuCreamBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = SakuCreamSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 9. Catatan (Note)
                OutlinedTextField(
                    value = formState.note,
                    onValueChange = { viewModel.onNoteChange(it) },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Catatan kecil terkait transaksi ini...") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SakuDarkGreen,
                        unfocusedBorderColor = SakuCreamBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = SakuCreamSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("transaction_note_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 10. Scan Struk (Pengeluaran only, inline collapsed)
                AnimatedVisibility(visible = formState.type == TransactionType.EXPENSE) {
                    Column {
                        // Collapsible header
                        Surface(
                            onClick = { viewModel.onToggleScanSection() },
                            shape = RoundedCornerShape(14.dp),
                            color = SakuCreamSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Scan Struk Belanja",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                    Text(
                                        text = if (formState.isScanSectionExpanded) "Tekan untuk tutup" else "Tekan untuk buka",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                                    )
                                }
                                Icon(
                                    imageVector = if (formState.isScanSectionExpanded)
                                        Icons.Filled.CheckCircle
                                    else
                                        Icons.Filled.DocumentScanner,
                                    contentDescription = null,
                                    tint = SakuDarkGreen
                                )
                            }
                        }

                        // Expanded content
                        AnimatedVisibility(visible = formState.isScanSectionExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "Ekstraksi data transaksi dari foto struk fisik atau e-receipt",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Opsi 1: Scan dengan Kamera
                                    Surface(
                                        onClick = { viewModel.onSelectScanSource(ReceiptSource.CAMERA) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = SakuCreamSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scan_with_camera_button")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(SakuLightGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = "Scan Kamera",
                                                    tint = SakuDarkGreen,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Kamera",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = SakuTextPrimary
                                                )
                                            )
                                        }
                                    }

                                    // Opsi 2: Scan dengan Galeri
                                    Surface(
                                        onClick = { viewModel.onSelectScanSource(ReceiptSource.GALLERY) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = SakuCreamSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scan_with_gallery_button")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(SakuCreamSurfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = "Scan Galeri",
                                                    tint = SakuDarkGreen,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Galeri",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = SakuTextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (formState.errorMessage != null) {
                    Text(
                        text = formState.errorMessage ?: "",
                        color = SakuExpenseRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // --- Bottom Sheet: Pilih Metode Scan (OCR Biasa vs AI Scan) ---
        if (formState.isScanMethodSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissScanMethodSheet() },
                sheetState = sheetState,
                containerColor = SakuCreamSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Pilih Metode Scan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = "Pilih cara sistem memproses foto struk (${formState.selectedScanSource?.label ?: "Sumber"})",
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Pilihan 1: OCR Biasa
                    Surface(
                        onClick = {
                            viewModel.onSelectScanMethod(ScanEngineMode.OCR)
                            proceedWithChosenSource()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = SakuCreamSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("method_ocr_option")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SakuLightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = SakuDarkGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "OCR Biasa",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SakuLightGreen
                                    ) {
                                        Text(
                                            text = "Offline / Standar",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SakuDarkGreen
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ekstrak teks dari struk. Tidak memerlukan API key Gemini. Anda mengisi & mengonfirmasi data transaksi secara mandiri.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SakuTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pilihan 2: AI Scan
                    val aiVerificationStatus = formState.apiKeyVerificationStatus
                    val isAiEnabled = viewModel.isAiConfigured() && aiVerificationStatus == ApiKeyVerificationStatus.Verified
                    Surface(
                        onClick = {
                            // Cek sinkron saat tombol diklik (polling state bisa stale)
                            val ready = viewModel.onSelectScanMethod(ScanEngineMode.AI)
                            if (ready) {
                                proceedWithChosenSource()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAiEnabled) SakuGoldLight else SakuCreamSurface.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAiEnabled) SakuGoldAccent.copy(alpha = 0.5f) else SakuCreamBorder.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("method_ai_option")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isAiEnabled) SakuGoldAccent else SakuTextMuted.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAiEnabled) Icons.Filled.CheckCircle else Icons.Default.Lock,
                                    contentDescription = if (isAiEnabled) null else "AI Scan terkunci",
                                    tint = if (isAiEnabled) Color.White else SakuTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AI Scan",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAiEnabled) SakuDarkGreen else SakuTextMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (!isAiEnabled) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SakuTextMuted.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Memerlukan API Key",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SakuTextMuted
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SakuGoldAccent
                                        ) {
                                            Text(
                                                text = "Gemini Vision AI",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isAiEnabled)
                                        "Analisis gambar struk menggunakan AI Gemini. Ekstraksi otomatis merchant, total nominal, tanggal, kategori, dan rincian transaksi."
                                    else if (!viewModel.isAiConfigured())
                                        "API Key Gemini belum dikonfigurasi. Atur di menu Profil untuk menggunakan AI Scan."
                                    else
                                        "API Key Gemini belum diverifikasi. Verifikasi di menu Profil untuk mengaktifkan AI Scan.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SakuTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // --- Dialog 1: Kunci API Gemini Belum Dikonfigurasi ---
        if (formState.isApiKeyMissingDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissApiKeyMissingDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = SakuGoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Kunci API Gemini Diperlukan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                text = {
                    Text(
                        text = "Fitur AI Scan memerlukan API Key Gemini untuk membaca dan menganalisis struk belanja secara otomatis.\n\nAnda dapat memasukkan API Key di menu Profil, atau melanjutkan dengan OCR Biasa tanpa API key.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissApiKeyMissingDialog()
                            onNavigateToProfile()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SakuDarkGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("api_key_missing_profile_button")
                    ) {
                        Text("Atur di Profil")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.switchToOcrFromMissingKey()
                            proceedWithChosenSource()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("api_key_missing_ocr_button")
                    ) {
                        Text("Gunakan OCR Biasa", color = SakuDarkGreen)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }

        // --- Dialog 2: Izin Kamera Ditolak ---
        if (formState.isCameraPermissionDeniedDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissCameraPermissionDeniedDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SakuExpenseRed,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Izin Kamera Diperlukan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                text = {
                    Text(
                        text = "Aplikasi Saku membutuhkan izin kamera untuk memotret struk belanja. Anda dapat memilih foto dari galeri sebagai alternatif.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissCameraPermissionDeniedDialog()
                            launchGalleryFlow()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SakuDarkGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pilih dari Galeri")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissCameraPermissionDeniedDialog() }) {
                        Text("Tutup", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }

        // --- Dialog 3: Processing State Loading Overlay ---
        if (formState.isScanning) {
            Dialog(onDismissRequest = { /* Sedang memproses */ }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SakuCreamSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SakuDarkGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Memproses Struk...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formState.scanStatusMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- Dialog 4: Scan Success Overlay ---
        if (formState.scanSuccessMessage != null) {
            Dialog(onDismissRequest = { viewModel.clearScanSuccessMessage() }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SakuCreamSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SakuLightGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Struk Berhasil Dipindai!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Data transaksi telah terisi secara otomatis. Silakan periksa dan simpan.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.clearScanSuccessMessage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SakuDarkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lanjutkan", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        // --- Dialog 5: Scan Error (dengan retry) ---
        if (formState.scanErrorMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearScanError() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SakuExpenseRed,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Gagal Memindai Struk",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                },
                text = {
                    Text(
                        text = formState.scanErrorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
                    )
                },
                confirmButton = {
                    if (formState.canRetryScan) {
                        Button(
                            onClick = { viewModel.retryScan() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SakuDarkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("scan_retry_button")
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clearScanError() }) {
                        Text("Tutup", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }
    }
}
