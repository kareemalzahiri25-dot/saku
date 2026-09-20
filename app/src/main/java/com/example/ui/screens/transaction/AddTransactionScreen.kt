package com.example.ui.screens.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.domain.model.Category
import com.example.domain.model.Pocket
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredCategories = categories.filter { it.type == formState.type }

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
                        // Pengeluaran Tab
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

                        // Pemasukan Tab
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

                Spacer(modifier = Modifier.height(16.dp))

                // AI / OCR Scan Button Banner
                Surface(
                    onClick = { viewModel.openScanningSheet() },
                    shape = RoundedCornerShape(16.dp),
                    color = SakuGoldLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SakuGoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_scan_sheet_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SakuGoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pindai Struk / AI Vision",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Ekstraksi nominal & kategori otomatis (Arsitektur Siap)",
                                style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Title / Transaction Name Field
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

                // 5. Kantong Selector
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

                // 6. Notes Field
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

                // 7. Save Button
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

        // Modal Bottom Sheet for Receipt Scanning (Prepared Architecture)
        if (formState.isScanningSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeScanningSheet() },
                sheetState = sheetState,
                containerColor = SakuCreamSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SakuLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pindai Struk & Ekstraksi AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )

                    Text(
                        text = "Arsitektur ReceiptScannerService telah siap dihubungkan ke Google ML Kit OCR dan Gemini Multimodal API.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SakuTextSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (formState.isScanning) {
                        CircularProgressIndicator(
                            color = SakuDarkGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Menganalisis data struk belanja...",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuDarkGreen)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.triggerOcrDemoScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_ocr_scan_button")
                        ) {
                            Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uji Coba Ekstraksi OCR (Supermarket)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.triggerAiDemoScan() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_ai_scan_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SakuGoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uji Coba Analisis AI Gemini (Restoran)", color = SakuDarkGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
