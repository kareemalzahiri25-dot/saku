package com.example.ui.screens.kantong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.domain.model.Pocket
import com.example.ui.components.SakuCard
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuCreamSurfaceVariant
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuDarkGreenDeep
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuForestGreen
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuMediumGreen
import com.example.ui.theme.SakuSageAccent
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

// Option Data Classes for Buat Kantong Baru
data class PocketTypeOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val defaultIconName: String
)

data class PocketColorOption(
    val hex: String,
    val label: String,
    val color: Color
)

data class PocketIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

data class RecommendedPocket(
    val name: String,
    val target: String,
    val type: String,
    val iconName: String,
    val colorHex: String,
    val emoji: String
)

val POCKET_TYPES = listOf(
    PocketTypeOption("Uang Tunai", "Uang Tunai", Icons.Default.Payments, "payments"),
    PocketTypeOption("Bank", "Bank", Icons.Default.AccountBalance, "account_balance"),
    PocketTypeOption("Tabungan", "Tabungan", Icons.Default.Savings, "savings"),
    PocketTypeOption("Investasi", "Investasi", Icons.AutoMirrored.Filled.TrendingUp, "trending_up"),
    PocketTypeOption("Crypto", "Crypto", Icons.Default.CurrencyExchange, "currency_exchange"),
    PocketTypeOption("Lainnya", "Lainnya", Icons.Default.Category, "account_balance_wallet")
)

val POCKET_COLORS = listOf(
    PocketColorOption("#133E35", "Saku Hijau", SakuDarkGreen),
    PocketColorOption("#266E5E", "Forest Green", SakuForestGreen),
    PocketColorOption("#4C7E6A", "Sage Green", SakuSageAccent),
    PocketColorOption("#C58E2E", "Gold Amber", SakuGoldAccent),
    PocketColorOption("#1E3A8A", "Navy Blue", Color(0xFF1E3A8A)),
    PocketColorOption("#D32F2F", "Crimson Red", SakuExpenseRed),
    PocketColorOption("#7C3AED", "Royal Purple", Color(0xFF7C3AED)),
    PocketColorOption("#0D9488", "Teal Ocean", Color(0xFF0D9488)),
    PocketColorOption("#EA580C", "Warm Orange", Color(0xFFEA580C))
)

val POCKET_ICONS = listOf(
    PocketIconOption("account_balance_wallet", "Dompet", Icons.Default.AccountBalanceWallet),
    PocketIconOption("savings", "Tabungan", Icons.Default.Savings),
    PocketIconOption("account_balance", "Bank", Icons.Default.AccountBalance),
    PocketIconOption("shopping_cart", "Belanja", Icons.Default.ShoppingCart),
    PocketIconOption("flight_takeoff", "Liburan", Icons.Default.FlightTakeoff),
    PocketIconOption("home", "Rumah", Icons.Default.Home),
    PocketIconOption("directions_car", "Kendaraan", Icons.Default.DirectionsCar),
    PocketIconOption("school", "Sekolah", Icons.Default.School),
    PocketIconOption("favorite", "Kesehatan", Icons.Default.Favorite),
    PocketIconOption("restaurant", "Makan", Icons.Default.Restaurant),
    PocketIconOption("work", "Pekerjaan", Icons.Default.Work),
    PocketIconOption("trending_up", "Investasi", Icons.AutoMirrored.Filled.TrendingUp)
)

val RECOMMENDED_POCKETS = listOf(
    RecommendedPocket("Dana Darurat", "15000000", "Tabungan", "savings", "#133E35", "🎯"),
    RecommendedPocket("Liburan Akhir Tahun", "8000000", "Tabungan", "flight_takeoff", "#266E5E", "🏖️"),
    RecommendedPocket("Cicilan Rumah", "5000000", "Bank", "home", "#1E3A8A", "🏠"),
    RecommendedPocket("Pendidikan Anak", "12000000", "Tabungan", "school", "#4C7E6A", "🎓"),
    RecommendedPocket("Servis Kendaraan", "3000000", "Lainnya", "directions_car", "#EA580C", "🚗"),
    RecommendedPocket("Belanja Bulanan", "4500000", "Uang Tunai", "shopping_cart", "#C58E2E", "🛒"),
    RecommendedPocket("Investasi Saham", "10000000", "Investasi", "trending_up", "#0D9488", "📈"),
    RecommendedPocket("Hadiah & Sedekah", "2000000", "Lainnya", "favorite", "#D32F2F", "🎁")
)

fun getPocketProgress(currentBalance: Double, targetAmount: Double): Float {
    if (targetAmount <= 0.0) return 0f
    return (currentBalance / targetAmount).toFloat().coerceIn(0f, 1f)
}

fun getPocketProgressPercentage(currentBalance: Double, targetAmount: Double): Int {
    if (targetAmount <= 0.0) return 0
    return ((currentBalance / targetAmount) * 100).toInt().coerceIn(0, 100)
}

fun parsePocketColor(colorHex: String, defaultColor: Color = SakuDarkGreen): Color {
    return try {
        val clean = colorHex.trim().removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> android.graphics.Color.parseColor("#$clean")
            8 -> android.graphics.Color.parseColor("#$clean")
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getPocketIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "savings", "tabungan", "piggy" -> Icons.Default.Savings
        "account_balance", "bank" -> Icons.Default.AccountBalance
        "shopping_cart", "cart", "belanja" -> Icons.Default.ShoppingCart
        "flight_takeoff", "flight", "liburan", "travel" -> Icons.Default.FlightTakeoff
        "home", "rumah" -> Icons.Default.Home
        "directions_car", "car", "kendaraan" -> Icons.Default.DirectionsCar
        "school", "pendidikan" -> Icons.Default.School
        "favorite", "health", "kesehatan" -> Icons.Default.Favorite
        "restaurant", "food", "makanan" -> Icons.Default.Restaurant
        "work", "bisnis" -> Icons.Default.Work
        "trending_up", "investasi" -> Icons.AutoMirrored.Filled.TrendingUp
        "payments", "tunai", "cash" -> Icons.Default.Payments
        "currency_exchange", "crypto" -> Icons.Default.CurrencyExchange
        else -> Icons.Default.AccountBalanceWallet
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KantongScreen(
    viewModel: KantongViewModel,
    onNavigateToAddTransaction: () -> Unit
) {
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val totalAllocated = pockets.sumOf { it.balance }

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
            // 1. Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kantong Dana",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SakuDarkGreen
                            )
                        )
                        Text(
                            text = "Bagi dan alokasikan anggaran sesuai pos keuangan",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                    }

                    Surface(
                        onClick = { viewModel.openTransferDialog() },
                        shape = CircleShape,
                        color = SakuLightGreen,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("transfer_pockets_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Pindah Dana",
                                tint = SakuDarkGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Total Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SakuDarkGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SakuDarkGreen, SakuMediumGreen, SakuDarkGreenDeep)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Seluruh Kantong",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "${pockets.size} Pos Aktif",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = Formatters.formatRupiah(totalAllocated),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 3. Action Cards (Buat Kantong Baru & Transfer Saldo)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Buat Kantong Baru
                    SakuCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openAddPocketDialog() }
                            .testTag("add_pocket_button"),
                        backgroundColor = SakuCreamSurface,
                        borderColor = SakuCreamBorder,
                        cornerRadius = 18.dp,
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(SakuLightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = SakuDarkGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Buat Kantong",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Text(
                                    text = "Pos baru",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Card 2: Transfer Dana
                    SakuCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openTransferDialog() }
                            .testTag("transfer_pocket_button"),
                        backgroundColor = SakuCreamSurface,
                        borderColor = SakuCreamBorder,
                        cornerRadius = 18.dp,
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(SakuLightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = SakuDarkGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pindah Saldo",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Text(
                                    text = "Antar pos",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Rekomendasi Kantong (Horizontal Recommendation Chips)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rekomendasi Kantong",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                        Text(
                            text = "Ketuk untuk buat cepat",
                            style = MaterialTheme.typography.labelSmall.copy(color = SakuTextMuted, fontSize = 11.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RECOMMENDED_POCKETS.forEach { rec ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SakuCreamSurface,
                                border = BorderStroke(1.dp, SakuCreamBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable {
                                    viewModel.openAddPocketDialog(
                                        presetName = rec.name,
                                        presetTarget = rec.target,
                                        presetType = rec.type,
                                        presetIcon = rec.iconName,
                                        presetColor = rec.colorHex
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = rec.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rec.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section Header with View Mode Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daftar Kantong",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = SakuLightGreen
                        ) {
                            Text(
                                text = "${pockets.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SakuDarkGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // View Switcher (Grid vs List)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SakuCreamSurfaceVariant,
                        border = BorderStroke(1.dp, SakuCreamBorder)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.viewMode == KantongViewMode.GRID) SakuDarkGreen else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.setViewMode(KantongViewMode.GRID) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Tampilan Grid 2 Kolom",
                                    tint = if (uiState.viewMode == KantongViewMode.GRID) Color.White else SakuTextSecondary,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .size(18.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.viewMode == KantongViewMode.LIST) SakuDarkGreen else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.setViewMode(KantongViewMode.LIST) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "Tampilan Daftar Detail",
                                    tint = if (uiState.viewMode == KantongViewMode.LIST) Color.White else SakuTextSecondary,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Content: 2-Column Compact Grid OR Detailed List Cards
            if (uiState.viewMode == KantongViewMode.GRID) {
                // 2-Column Grid as in Figma reference
                val chunkedPockets = pockets.chunked(2)
                items(chunkedPockets) { rowPockets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PocketCompactGridCard(
                            pocket = rowPockets[0],
                            modifier = Modifier.weight(1f),
                            onTransfer = { viewModel.openTransferDialog(rowPockets[0].id) }
                        )

                        if (rowPockets.size > 1) {
                            PocketCompactGridCard(
                                pocket = rowPockets[1],
                                modifier = Modifier.weight(1f),
                                onTransfer = { viewModel.openTransferDialog(rowPockets[1].id) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Detailed List Cards
                items(pockets) { pocket ->
                    PocketDetailedCard(
                        pocket = pocket,
                        onTransfer = { viewModel.openTransferDialog(pocket.id) },
                        onDelete = { viewModel.deletePocket(pocket.id) }
                    )
                }
            }
        }

        // Modal / Dialog: Buat Kantong Baru (Figma Fidelity)
        if (uiState.isAddPocketDialogOpen) {
            BuatKantongModal(
                uiState = uiState,
                onDismiss = { viewModel.closeAddPocketDialog() },
                onNameChange = { viewModel.onNewPocketNameChange(it) },
                onTypeChange = { viewModel.onNewPocketTypeChange(it) },
                onBalanceChange = { viewModel.onNewPocketBalanceChange(it) },
                onTargetChange = { viewModel.onNewPocketTargetChange(it) },
                onColorChange = { viewModel.onNewPocketColorChange(it) },
                onIconChange = { viewModel.onNewPocketIconChange(it) },
                onDescriptionChange = { viewModel.onNewPocketDescriptionChange(it) },
                onSave = { viewModel.saveNewPocket() }
            )
        }

        // Modal / Dialog: Transfer Saldo Antar Kantong
        if (uiState.isTransferDialogOpen) {
            TransferSaldoModal(
                uiState = uiState,
                pockets = pockets,
                onDismiss = { viewModel.closeTransferDialog() },
                onSourceChange = { viewModel.onTransferSourceChange(it) },
                onTargetChange = { viewModel.onTransferTargetChange(it) },
                onAmountChange = { viewModel.onTransferAmountChange(it) },
                onNoteChange = { viewModel.onTransferNoteChange(it) },
                onConfirm = { viewModel.executeTransfer() }
            )
        }
    }
}

// Compact 2-Column Grid Pocket Card (Figma Reference)
@Composable
fun PocketCompactGridCard(
    pocket: Pocket,
    modifier: Modifier = Modifier,
    onTransfer: () -> Unit
) {
    val pocketColor = parsePocketColor(pocket.colorHex)
    val iconVector = getPocketIconVector(pocket.iconName)
    val progress = getPocketProgress(pocket.balance, pocket.targetAmount)
    val progressPercentage = getPocketProgressPercentage(pocket.balance, pocket.targetAmount)

    Card(
        modifier = modifier
            .border(1.dp, SakuCreamBorder, RoundedCornerShape(18.dp))
            .clickable { onTransfer() }
            .testTag("pocket_card_${pocket.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SakuCreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Category-specific icon in small colored rounded-square + optional badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored rounded-square container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(pocketColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = pocket.name,
                        tint = pocketColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (pocket.isMain) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SakuGoldLight
                    ) {
                        Text(
                            text = "Utama",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SakuGoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (pocket.targetAmount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SakuLightGreen
                    ) {
                        Text(
                            text = "$progressPercentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SakuDarkGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pocket Name
            Text(
                text = pocket.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Current Balance
            Text(
                text = Formatters.formatRupiah(pocket.balance),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SakuTextPrimary,
                    fontSize = 14.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Target Amount
            if (pocket.targetAmount > 0) {
                Text(
                    text = "Target ${Formatters.formatRupiah(pocket.targetAmount)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextSecondary,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Tanpa target saldo",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextMuted,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thin progress bar + percentage
            if (pocket.targetAmount > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = pocketColor,
                    trackColor = SakuLightGreen
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SakuCreamBorder)
                )
            }
        }
    }
}

// Detailed List Pocket Card (Figma Reference)
@Composable
fun PocketDetailedCard(
    pocket: Pocket,
    onTransfer: () -> Unit,
    onDelete: () -> Unit
) {
    val pocketColor = parsePocketColor(pocket.colorHex)
    val iconVector = getPocketIconVector(pocket.iconName)
    val progress = getPocketProgress(pocket.balance, pocket.targetAmount)
    val progressPercentage = getPocketProgressPercentage(pocket.balance, pocket.targetAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SakuCreamBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SakuCreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(pocketColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = pocket.name,
                            tint = pocketColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pocket.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            if (pocket.isMain) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SakuGoldLight
                                ) {
                                    Text(
                                        text = "Utama",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SakuGoldAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (pocket.description.isNotBlank()) {
                            Text(
                                text = pocket.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!pocket.isMain) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Kantong",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo Saat Ini",
                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                    )
                    Text(
                        text = Formatters.formatRupiah(pocket.balance),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                }

                if (pocket.targetAmount > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target / Anggaran",
                            style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                        )
                        Text(
                            text = Formatters.formatRupiah(pocket.targetAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SakuTextPrimary
                            )
                        )
                    }
                }
            }

            if (pocket.targetAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = pocketColor,
                    trackColor = SakuLightGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$progressPercentage% tercapai",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SakuTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                    val remaining = (pocket.targetAmount - pocket.balance).coerceAtLeast(0.0)
                    Text(
                        text = "Sisa ${Formatters.formatRupiah(remaining)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SakuTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Pindah Dana" action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SakuLightGreen,
                    modifier = Modifier.clickable { onTransfer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pindah Dana",
                            style = MaterialTheme.typography.labelMedium.copy(
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

// Buat Kantong Baru Modal / Dialog (Matching Saku Figma Reference)
@Composable
fun BuatKantongModal(
    uiState: KantongUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = SakuCreamBackground),
                border = BorderStroke(1.dp, SakuCreamBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Header with Back / Close Button, Title, and Subtitle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = SakuDarkGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Buat Kantong Baru",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Atur pos anggaran keuanganmu",
                                style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Form Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Nama Kantong
                        Column {
                            Text(
                                text = "Nama Kantong",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newPocketName,
                                onValueChange = onNameChange,
                                placeholder = { Text("Contoh: Tabungan Liburan, Kebutuhan Rumah") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_pocket_name_input")
                            )
                        }

                        // 2. Jenis Kantong (Selectable Chips with Icons)
                        Column {
                            Text(
                                text = "Jenis Kantong",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                POCKET_TYPES.forEach { opt ->
                                    val isSelected = uiState.newPocketType == opt.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) SakuDarkGreen else SakuCreamBorder
                                        ),
                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                        modifier = Modifier.clickable {
                                            onTypeChange(opt.id)
                                            onIconChange(opt.defaultIconName)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = opt.icon,
                                                contentDescription = opt.label,
                                                tint = if (isSelected) Color.White else SakuDarkGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = opt.label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else SakuTextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Saldo Awal
                        Column {
                            Text(
                                text = "Saldo Awal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newPocketBalance,
                                onValueChange = onBalanceChange,
                                placeholder = { Text("0") },
                                prefix = {
                                    Text(
                                        text = "Rp ",
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_pocket_balance_input")
                            )
                        }

                        // 4. Target Saldo & Helper Text
                        Column {
                            Text(
                                text = "Target Saldo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newPocketTarget,
                                onValueChange = onTargetChange,
                                placeholder = { Text("10.000.000") },
                                prefix = {
                                    Text(
                                        text = "Rp ",
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tentukan target untuk memantau progres tabunganmu",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // 5. Warna Kantong (Circular Color Choices)
                        Column {
                            Text(
                                text = "Warna Kantong",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                POCKET_COLORS.forEach { colorOpt ->
                                    val isSelected = uiState.newPocketColorHex.equals(colorOpt.hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(colorOpt.color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) SakuDarkGreen else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { onColorChange(colorOpt.hex) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Terpilih",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Pilih Icon (Selectable Icon Buttons)
                        Column {
                            Text(
                                text = "Pilih Icon",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                POCKET_ICONS.forEach { iconOpt ->
                                    val isSelected = uiState.newPocketIcon == iconOpt.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SakuLightGreen else SakuCreamSurface,
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) SakuDarkGreen else SakuCreamBorder
                                        ),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable { onIconChange(iconOpt.id) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = iconOpt.icon,
                                                contentDescription = iconOpt.label,
                                                tint = if (isSelected) SakuDarkGreen else SakuTextSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 7. Catatan / Deskripsi (Opsional)
                        Column {
                            Text(
                                text = "Catatan Tambahan (Opsional)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newPocketDescription,
                                onValueChange = onDescriptionChange,
                                placeholder = { Text("Tuliskan tujuan atau detail anggaran...") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Error Banner
                        if (uiState.errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SakuExpenseRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = SakuExpenseRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Button
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SakuDarkGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_pocket_button")
                    ) {
                        Text(
                            text = "Buat Kantong",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// Transfer Saldo Modal / Dialog (Matching Saku Figma Reference)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSaldoModal(
    uiState: KantongUiState,
    pockets: List<Pocket>,
    onDismiss: () -> Unit,
    onSourceChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = SakuCreamBackground),
                border = BorderStroke(1.dp, SakuCreamBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Batal",
                                tint = SakuDarkGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pindah Saldo",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Transfer dana antar pos keuangan",
                                style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Source Pocket Selector
                        var sourceExpanded by remember { mutableStateOf(false) }
                        val sourcePocket = pockets.find { it.id == uiState.transferSourcePocketId }

                        Column {
                            Text(
                                text = "Dari Kantong (Asal)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = sourceExpanded,
                                onExpandedChange = { sourceExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = sourcePocket?.let { "${it.name} (${Formatters.formatRupiah(it.balance)})" }
                                        ?: "Pilih Kantong Asal",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SakuCreamSurface,
                                        unfocusedContainerColor = SakuCreamSurface,
                                        focusedBorderColor = SakuDarkGreen,
                                        unfocusedBorderColor = SakuCreamBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = sourceExpanded,
                                    onDismissRequest = { sourceExpanded = false }
                                ) {
                                    pockets.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text("${p.name} (${Formatters.formatRupiah(p.balance)})") },
                                            onClick = {
                                                onSourceChange(p.id)
                                                sourceExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Target Pocket Selector
                        var targetExpanded by remember { mutableStateOf(false) }
                        val targetPocket = pockets.find { it.id == uiState.transferTargetPocketId }

                        Column {
                            Text(
                                text = "Ke Kantong (Tujuan)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = targetExpanded,
                                onExpandedChange = { targetExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = targetPocket?.let { "${it.name} (${Formatters.formatRupiah(it.balance)})" }
                                        ?: "Pilih Kantong Tujuan",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SakuCreamSurface,
                                        unfocusedContainerColor = SakuCreamSurface,
                                        focusedBorderColor = SakuDarkGreen,
                                        unfocusedBorderColor = SakuCreamBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = targetExpanded,
                                    onDismissRequest = { targetExpanded = false }
                                ) {
                                    pockets.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text("${p.name} (${Formatters.formatRupiah(p.balance)})") },
                                            onClick = {
                                                onTargetChange(p.id)
                                                targetExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Nominal Transfer
                        Column {
                            Text(
                                text = "Jumlah Transfer",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.transferAmount,
                                onValueChange = onAmountChange,
                                placeholder = { Text("100.000") },
                                prefix = {
                                    Text(
                                        text = "Rp ",
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_amount_input")
                            )
                        }

                        // Catatan Transfer
                        Column {
                            Text(
                                text = "Catatan Transfer (Opsional)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.transferNote,
                                onValueChange = onNoteChange,
                                placeholder = { Text("Contoh: Top up tabungan liburan") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (uiState.errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SakuExpenseRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = SakuExpenseRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SakuDarkGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_transfer_button")
                    ) {
                        Text(
                            text = "Pindahkan Saldo",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
