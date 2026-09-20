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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.TransactionType
import com.example.ui.components.SakuCard
import com.example.ui.components.SakuTopBar
import com.example.ui.components.getCategoryIconVector
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
import java.text.SimpleDateFormat
import java.util.Date
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            SakuTopBar(
                title = "Catat Transaksi",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
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

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Large Amount Input Card
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

                Spacer(modifier = Modifier.height(18.dp))

                // --- 3. Receipt Input Section (Dua Opsi: Kamera & Galeri) ---
                Text(
                    text = "Pindai Struk Belanja",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
                Text(
                    text = "Ekstraksi data transaksi dari foto struk fisik atau e-receipt",
                    style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary),
                    modifier = Modifier.padding(bottom = 10.dp)
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
                                text = "Scan Kamera",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Foto struk fisik",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Opsi 2: Pilih dari Galeri
                    Surface(
                        onClick = { viewModel.onSelectScanSource(ReceiptSource.GALLERY) },
                        shape = RoundedCornerShape(16.dp),
                        color = SakuCreamSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_from_gallery_button")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SakuGoldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pilih Galeri",
                                    tint = SakuGoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pilih Galeri",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Gambar / e-struk",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Notifikasi sukses hasil ekstraksi struk diterapkan
                if (formState.scanSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SakuIncomeGreenBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SakuIncomeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formState.scanSuccessMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SakuDarkGreen,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearScanSuccessMessage() }) {
                                Text("OK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Title / Transaction Name Field
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

                Spacer(modifier = Modifier.height(18.dp))

                // 5. Kategori Selector
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
                                    imageVector = getCategoryIconVector(cat.iconName),
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

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Kantong Selector
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
                                    text = Formatters.formatRupiah(pocket.balance),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else SakuTextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 7. Notes Field
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

                if (formState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formState.errorMessage ?: "",
                        color = SakuExpenseRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 8. Save Button
                Button(
                    onClick = { viewModel.saveTransaction(onNavigateBack) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuDarkGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = "Simpan Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                    Surface(
                        onClick = {
                            val ready = viewModel.onSelectScanMethod(ScanEngineMode.AI)
                            if (ready) {
                                proceedWithChosenSource()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = SakuGoldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuGoldAccent.copy(alpha = 0.5f)),
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
                                    .background(SakuGoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
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
                                            color = SakuDarkGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Analisis gambar struk menggunakan AI Gemini. Ekstraksi otomatis merchant, total nominal, tanggal, kategori, dan rincian transaksi.",
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
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
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
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formState.scanStatusMessage ?: "Menganalisis data gambar struk...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SakuTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }

        // --- Dialog 4: Review Hasil Ekstraksi Struk (User Confirmation) ---
        if (formState.isReviewDialogOpen && formState.scannedResult != null) {
            val result = formState.scannedResult!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissReviewDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Review Hasil Pindai",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Badge Mode Scan
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (formState.scanMode == ScanEngineMode.AI) SakuGoldLight else SakuLightGreen,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Metode: ${formState.scanMode.label}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Detail Preview
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SakuCreamSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Merchant / Toko",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                                )
                                Text(
                                    text = result.merchantName ?: "(Tidak terdeteksi - isi manual)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Total Pembayaran",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                                )
                                Text(
                                    text = result.totalAmount?.let { Formatters.formatRupiah(it) } ?: "(Belum terdeteksi - isi manual)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (result.suggestedType == TransactionType.EXPENSE) SakuExpenseRed else SakuIncomeGreen
                                    )
                                )

                                if (!result.suggestedCategory.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Saran Kategori",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                                    )
                                    Text(
                                        text = result.suggestedCategory,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = SakuDarkGreen
                                        )
                                    )
                                }

                                if (result.items.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Rincian Item (${result.items.size}):",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                                    )
                                    result.items.take(4).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.quantity}x ${item.name}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = SakuTextPrimary),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = Formatters.formatRupiah(item.price),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                            )
                                        }
                                    }
                                    if (result.items.size > 4) {
                                        Text(
                                            text = "+${result.items.size - 4} item lainnya",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Data ini akan diterapkan ke formulir transaksi. Anda dapat mengedit setiap kolom sebelum menyimpannya ke Saku.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SakuTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.applyScannedResultToForm(result) },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_scanned_result_button")
                    ) {
                        Text("Terapkan ke Formulir")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissReviewDialog() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dismiss_review_dialog_button")
                    ) {
                        Text("Batal", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface
            )
        }

        // --- Dialog 5: Scan Error Alert ---
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
                        text = "Gagal Memproses Struk",
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
                    Button(
                        onClick = { viewModel.clearScanError() },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tutup")
                    }
                },
                containerColor = SakuCreamSurface
            )
        }
    }
}
