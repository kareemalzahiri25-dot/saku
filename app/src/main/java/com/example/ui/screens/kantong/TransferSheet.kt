package com.example.ui.screens.kantong

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.Formatters
import com.example.domain.model.Asset
import com.example.ui.components.SakuCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    uiState: KantongUiState,
    assets: List<Asset>,
    onDismiss: () -> Unit,
    onSourceAssetChange: (String) -> Unit,
    onTargetAssetChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (java.time.LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSourceDropdown by remember { mutableStateOf(false) }
    var showTargetDropdown by remember { mutableStateOf(false) }

    // Validasi: asal ≠ tujuan, nominal > 0, nominal ≤ saldo asal
    val sourceAsset = assets.find { it.id == uiState.transferSourceAssetId }
    val targetAsset = assets.find { it.id == uiState.transferTargetAssetId }
    val amount = Formatters.parseAmount(uiState.transferAmountString)

    val isValid = sourceAsset != null &&
            targetAsset != null &&
            sourceAsset.id != targetAsset.id &&
            amount > 0 &&
            amount <= sourceAsset.balance

    val validationError = when {
        sourceAsset == null -> "Pilih aset asal"
        targetAsset == null -> "Pilih aset tujuan"
        sourceAsset.id == targetAsset.id -> "Aset asal dan tujuan tidak boleh sama"
        amount <= 0 -> "Nominal harus lebih besar dari 0"
        amount > sourceAsset.balance -> "Nominal melebihi saldo"
        else -> null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SakuCreamBackground,
        scrimColor = SakuTextMuted.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transfer Dana",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = SakuTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // From asset (clickable dropdown)
            Text(
                "Dari Aset",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen)
            )
            Box {
                SakuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSourceDropdown = true },
                    backgroundColor = SakuCreamSurface,
                    borderColor = SakuCreamBorder,
                    cornerRadius = 12.dp,
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sourceAsset?.name ?: "Pilih aset asal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (sourceAsset != null) SakuTextPrimary else SakuTextMuted
                                )
                            )
                            if (sourceAsset != null) {
                                Text(
                                    "Saldo: ${Formatters.formatRupiah(sourceAsset.balance)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SakuTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSourceDropdown,
                    onDismissRequest = { showSourceDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    assets.forEach { asset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(asset.name)
                                    Text(
                                        Formatters.formatRupiah(asset.balance),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            onClick = {
                                onSourceAssetChange(asset.id)
                                showSourceDropdown = false
                            }
                        )
                    }
                }
            }

            // Swap button
            Button(
                onClick = {
                    if (sourceAsset != null && targetAsset != null) {
                        onSourceAssetChange(targetAsset.id)
                        onTargetAssetChange(sourceAsset.id)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(44.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SakuLightGreen)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Tukar", tint = SakuDarkGreen)
            }

            // To asset (clickable dropdown)
            Text(
                "Ke Aset",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen)
            )
            Box {
                SakuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTargetDropdown = true },
                    backgroundColor = SakuCreamSurface,
                    borderColor = SakuCreamBorder,
                    cornerRadius = 12.dp,
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                targetAsset?.name ?: "Pilih aset tujuan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (targetAsset != null) SakuTextPrimary else SakuTextMuted
                                )
                            )
                            if (targetAsset != null) {
                                Text(
                                    "Saldo: ${Formatters.formatRupiah(targetAsset.balance)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SakuTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showTargetDropdown,
                    onDismissRequest = { showTargetDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    assets.filter { it.id != uiState.transferSourceAssetId }.forEach { asset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(asset.name)
                                    Text(
                                        Formatters.formatRupiah(asset.balance),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            onClick = {
                                onTargetAssetChange(asset.id)
                                showTargetDropdown = false
                            }
                        )
                    }
                }
            }

            // Amount (auto-format live)
            Text(
                "Nominal",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen)
            )
            OutlinedTextField(
                value = uiState.transferAmountString,
                onValueChange = { newValue ->
                    val clean = newValue.filter { it.isDigit() }
                    onAmountChange(Formatters.formatRupiah(clean.toLongOrNull() ?: 0L))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            // Note
            Text(
                "Catatan (opsional)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SakuDarkGreen)
            )
            OutlinedTextField(
                value = uiState.transferNote,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tambahkan catatan...") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Warning overplanning
            if (uiState.transferWarning != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SakuExpenseRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        uiState.transferWarning!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuExpenseRed),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Validation error (if not valid)
            if (!isValid && validationError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SakuExpenseRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        validationError,
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuExpenseRed),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", color = SakuTextPrimary)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuDarkGreen,
                        disabledContainerColor = SakuDarkGreen.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Transfer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}