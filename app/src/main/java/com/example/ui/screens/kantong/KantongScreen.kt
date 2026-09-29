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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.OutlinedButton
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
import java.time.LocalDate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.domain.model.Asset
import com.example.domain.model.AssetType
import com.example.domain.model.Pocket
import com.example.domain.model.PocketStats
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

data class AssetTypeOption(
    val type: AssetType,
    val label: String,
    val icon: ImageVector,
    val defaultIconName: String
)

val ASSET_TYPE_OPTIONS = listOf(
    AssetTypeOption(AssetType.BANK, "Bank", Icons.Default.AccountBalance, "account_balance"),
    AssetTypeOption(AssetType.E_WALLET, "E-Wallet", Icons.Default.AccountBalanceWallet, "account_balance_wallet"),
    AssetTypeOption(AssetType.CASH, "Uang Tunai", Icons.Default.Payments, "payments"),
    AssetTypeOption(AssetType.INVESTMENT, "Investasi", Icons.AutoMirrored.Filled.TrendingUp, "trending_up"),
    AssetTypeOption(AssetType.CRYPTO, "Crypto", Icons.Default.CurrencyExchange, "currency_exchange"),
    AssetTypeOption(AssetType.OTHER, "Lainnya", Icons.Default.Category, "category")
)

fun getAssetIconVector(iconName: String, type: AssetType = AssetType.BANK): ImageVector {
    return when (iconName.lowercase()) {
        "account_balance", "bank" -> Icons.Default.AccountBalance
        "account_balance_wallet", "wallet", "dompet", "e_wallet" -> Icons.Default.AccountBalanceWallet
        "payments", "tunai", "cash" -> Icons.Default.Payments
        "trending_up", "investasi", "investment" -> Icons.AutoMirrored.Filled.TrendingUp
        "currency_exchange", "crypto" -> Icons.Default.CurrencyExchange
        "savings", "tabungan" -> Icons.Default.Savings
        "shopping_cart", "cart" -> Icons.Default.ShoppingCart
        "flight_takeoff" -> Icons.Default.FlightTakeoff
        "home" -> Icons.Default.Home
        "directions_car" -> Icons.Default.DirectionsCar
        "school" -> Icons.Default.School
        "favorite" -> Icons.Default.Favorite
        "restaurant" -> Icons.Default.Restaurant
        "work" -> Icons.Default.Work
        "category", "lainnya" -> Icons.Default.Category
        else -> when (type) {
            AssetType.BANK -> Icons.Default.AccountBalance
            AssetType.E_WALLET -> Icons.Default.AccountBalanceWallet
            AssetType.CASH -> Icons.Default.Payments
            AssetType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
            AssetType.CRYPTO -> Icons.Default.CurrencyExchange
            AssetType.OTHER -> Icons.Default.Category
        }
    }
}

fun getAssetTypeLabel(type: AssetType): String {
    return when (type) {
        AssetType.BANK -> "Bank"
        AssetType.E_WALLET -> "E-Wallet"
        AssetType.CASH -> "Uang Tunai"
        AssetType.INVESTMENT -> "Investasi"
        AssetType.CRYPTO -> "Crypto"
        AssetType.OTHER -> "Lainnya"
    }
}

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
    val pocketStats by viewModel.pocketStats.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val activeAssets by viewModel.activeAssets.collectAsStateWithLifecycle()
    val totalAssetBalance by viewModel.totalAssetBalance.collectAsStateWithLifecycle()
    val assetPlanning by viewModel.assetPlanning.collectAsStateWithLifecycle()
    val overPlannedAssets by viewModel.overPlannedAssets.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val totalAllocated = pocketStats.sumOf { it.realization }
    val activeAssetCount = activeAssets.size

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
            // 1. Screen Header (Dynamic based on selected tab)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (uiState.selectedTab == SakuPageTab.ASET) "Aset" else "Kantong Dana",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SakuDarkGreen
                            )
                        )
                        Text(
                            text = if (uiState.selectedTab == SakuPageTab.ASET) {
                                "Kelola rekening dan tempat penyimpanan uang Anda"
                            } else {
                                "Bagi dan alokasikan anggaran sesuai pos keuangan"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                    }
                }
            }

            // 2. Tab Switcher (Kantong Dana | Aset)
            item {
                SakuPageTabSwitcher(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }

            if (uiState.selectedTab == SakuPageTab.KANTONG) {
                // ==================== TAB KANTONG DANA ====================

                // Total Overview Card
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

                // Action Cards (Buat Kantong Baru)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                    }
                }

                // Rekomendasi Kantong (Horizontal Recommendation Chips)
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

                // Section Header with View Mode Switcher
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

                // Content: 2-Column Compact Grid OR Detailed List Cards
                if (uiState.viewMode == KantongViewMode.GRID) {
                    val chunkedPockets = pockets.chunked(2)
                    items(chunkedPockets) { rowPockets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PocketCompactGridCard(
                                pocket = rowPockets[0],
                                pocketStats = pocketStats,
                                modifier = Modifier.weight(1f),
                                onEdit = { viewModel.openEditPocketDialog(rowPockets[0]) }
                            )

                            if (rowPockets.size > 1) {
                                PocketCompactGridCard(
                                    pocket = rowPockets[1],
                                    pocketStats = pocketStats,
                                    modifier = Modifier.weight(1f),
                                    onEdit = { viewModel.openEditPocketDialog(rowPockets[1]) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(pockets) { pocket ->
                        PocketDetailedCard(
                            pocket = pocket,
                            pocketStats = pocketStats,
                            onEdit = { viewModel.openEditPocketDialog(pocket) },
                            onDelete = { viewModel.deletePocket(pocket.id) }
                        )
                    }
                }
            } else {
                // ==================== TAB ASET ====================

                // Asset Overview Card
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
                                        text = "Total Saldo Aset",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "$activeAssetCount Akun Aktif",
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
                                    text = Formatters.formatRupiah(totalAssetBalance),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Action Card (Tambah Aset Baru)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SakuCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.openAddAssetDialog() }
                                .testTag("add_asset_button"),
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
                                        text = "Tambah Aset",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                    Text(
                                        text = "Rekening / Dompet Baru",
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

                // Section Header with View Mode Switcher
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daftar Aset",
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
                                    text = "$activeAssetCount",
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
                                    color = if (uiState.assetViewMode == AssetViewMode.GRID) SakuDarkGreen else Color.Transparent,
                                    modifier = Modifier.clickable { viewModel.setAssetViewMode(AssetViewMode.GRID) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Tampilan Grid 2 Kolom",
                                        tint = if (uiState.assetViewMode == AssetViewMode.GRID) Color.White else SakuTextSecondary,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .size(18.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (uiState.assetViewMode == AssetViewMode.LIST) SakuDarkGreen else Color.Transparent,
                                    modifier = Modifier.clickable { viewModel.setAssetViewMode(AssetViewMode.LIST) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewList,
                                        contentDescription = "Tampilan Daftar Detail",
                                        tint = if (uiState.assetViewMode == AssetViewMode.LIST) Color.White else SakuTextSecondary,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Banner Over-Planning
                item {
                    if (uiState.selectedTab == SakuPageTab.ASET && overPlannedAssets.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            overPlannedAssets.forEach { (asset, planned) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SakuExpenseRed.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().testTag("over_planning_banner_${asset.id}")
                                ) {
                                    Text(
                                        text = "⚠️ Perhatian: Saldo ${asset.name} (${Formatters.formatRupiah(asset.balance)}) kurang dari total alokasi (${Formatters.formatRupiah(planned)}). Review kantong atau tambah saldo.",
                                        color = SakuExpenseRed,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Content: 2-Column Compact Grid OR Detailed List Cards for Assets
                if (uiState.assetViewMode == AssetViewMode.GRID) {
                    val chunkedAssets = assets.chunked(2)
                    items(chunkedAssets) { rowAssets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AssetCompactGridCard(
                                asset = rowAssets[0],
                                planning = assetPlanning[rowAssets[0].id] ?: 0.0,
                                modifier = Modifier.weight(1f),
                                onEdit = { viewModel.openEditAssetDialog(rowAssets[0]) },
                                onTransfer = { viewModel.openTransferSheet(rowAssets[0]) }
                            )

                            if (rowAssets.size > 1) {
                                AssetCompactGridCard(
                                    asset = rowAssets[1],
                                    planning = assetPlanning[rowAssets[1].id] ?: 0.0,
                                    modifier = Modifier.weight(1f),
                                    onEdit = { viewModel.openEditAssetDialog(rowAssets[1]) },
                                    onTransfer = { viewModel.openTransferSheet(rowAssets[1]) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(assets) { asset ->
                        AssetDetailedCard(
                            asset = asset,
                            planning = assetPlanning[asset.id] ?: 0.0,
                            onEdit = { viewModel.openEditAssetDialog(asset) },
                            onDelete = { viewModel.deleteAsset(asset.id) },
                            onTransfer = { viewModel.openTransferSheet(asset) }
                        )
                    }
                }
            }
        }

        // Modal / Dialog: Buat Kantong Baru
        if (uiState.isAddPocketDialogOpen) {
            BuatKantongModal(
                uiState = uiState,
                onDismiss = { viewModel.closeAddPocketDialog() },
                onNameChange = { viewModel.onNewPocketNameChange(it) },
                onTypeChange = { viewModel.onNewPocketTypeChange(it) },
                onTargetChange = { viewModel.onNewPocketTargetChange(it) },
                onColorChange = { viewModel.onNewPocketColorChange(it) },
                onIconChange = { viewModel.onNewPocketIconChange(it) },
                onDescriptionChange = { viewModel.onNewPocketDescriptionChange(it) },
                onSave = { viewModel.saveNewPocket() }
            )
        }

        // Modal / Dialog: Edit Kantong
        if (uiState.isEditPocketDialogOpen) {
            EditKantongModal(
                uiState = uiState,
                assets = assets,
                onDismiss = { viewModel.closeEditPocketDialog() },
                onNameChange = { viewModel.onEditPocketNameChange(it) },
                onTypeChange = { viewModel.onEditPocketTypeChange(it) },
                onTargetChange = { viewModel.onEditPocketTargetChange(it) },
                onColorChange = { viewModel.onEditPocketColorChange(it) },
                onIconChange = { viewModel.onEditPocketIconChange(it) },
                onDescriptionChange = { viewModel.onEditPocketDescriptionChange(it) },
                onAllocAdd = { viewModel.addEditPocketAllocation(it) },
                onAllocRemove = { viewModel.removeEditPocketAllocation(it) },
                onAllocNominalChange = { idx, value -> viewModel.onEditPocketAllocationNominalChange(idx, value) },
                onConfirmClearAlloc = { viewModel.saveEditPocket(confirmedClear = true) },
                onCancelClearAlloc = { viewModel.cancelClearPocketAllocations() },
                onSave = { viewModel.saveEditPocket() }
            )
        }

        // Modal / Dialog: Buat Aset Baru
        if (uiState.isAddAssetDialogOpen) {
            BuatAsetModal(
                uiState = uiState,
                onDismiss = { viewModel.closeAddAssetDialog() },
                onNameChange = { viewModel.onNewAssetNameChange(it) },
                onBalanceChange = { viewModel.onNewAssetBalanceChange(it) },
                onTypeChange = { viewModel.onNewAssetTypeChange(it) },
                onColorChange = { viewModel.onNewAssetColorChange(it) },
                onIconChange = { viewModel.onNewAssetIconChange(it) },
                onSave = { viewModel.saveNewAsset() }
            )
        }

        // Modal / Dialog: Edit Aset
        if (uiState.isEditAssetDialogOpen) {
            EditAsetModal(
                uiState = uiState,
                onDismiss = { viewModel.closeEditAssetDialog() },
                onNameChange = { viewModel.onEditAssetNameChange(it) },
                onBalanceChange = { viewModel.onEditAssetBalanceChange(it) },
                onTypeChange = { viewModel.onEditAssetTypeChange(it) },
                onColorChange = { viewModel.onEditAssetColorChange(it) },
                onIconChange = { viewModel.onEditAssetIconChange(it) },
                onSave = { viewModel.saveEditAsset() }
            )
        }

        // Bottom Sheet: Transfer Dana
        if (uiState.isTransferSheetOpen) {
            TransferSheet(
                uiState = uiState,
                assets = assets,
                onDismiss = { viewModel.closeTransferSheet() },
                onTargetAssetChange = { viewModel.onTransferTargetAssetChange(it) },
                onAmountChange = { viewModel.onTransferAmountChange(it) },
                onDateChange = { viewModel.onTransferDateChange(it) },
                onNoteChange = { viewModel.onTransferNoteChange(it) },
                onSave = { viewModel.saveTransfer() }
            )
        }

    }
}

// Compact 2-Column Grid Pocket Card (Figma Reference)
@Composable
fun PocketCompactGridCard(
    pocket: Pocket,
    pocketStats: List<PocketStats>,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    val pocketColor = parsePocketColor(pocket.colorHex)
    val iconVector = getPocketIconVector(pocket.iconName)
    val pocketStat = pocketStats.find { it.pocketId == pocket.id }
    val realization = pocketStat?.realization ?: 0.0
    val progress = getPocketProgress(realization, pocket.targetAmount)
    val progressPercentage = getPocketProgressPercentage(realization, pocket.targetAmount)

    Card(
        modifier = modifier
            .border(1.dp, SakuCreamBorder, RoundedCornerShape(18.dp))
            .clickable { onEdit() }
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
            // Top Row: Category-specific icon in small colored rounded-square + optional badge + Edit action
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

                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_pocket_btn_${pocket.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kantong",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(16.dp)
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

            // Current Balance (Realization)
            val pocketStat = pocketStats.find { it.pocketId == pocket.id }
            val realization = pocketStat?.realization ?: 0.0
            Text(
                text = Formatters.formatRupiah(realization),
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
    pocketStats: List<PocketStats>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val pocketColor = parsePocketColor(pocket.colorHex)
    val iconVector = getPocketIconVector(pocket.iconName)
    val pocketStat = pocketStats.find { it.pocketId == pocket.id }
    val realization = pocketStat?.realization ?: 0.0
    val progress = getPocketProgress(realization, pocket.targetAmount)
    val progressPercentage = getPocketProgressPercentage(realization, pocket.targetAmount)

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_pocket_btn_${pocket.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kantong",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (!pocket.isMain) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_pocket_btn_${pocket.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Kantong",
                                tint = SakuTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                        text = Formatters.formatRupiah(realization),
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
                    val remaining = (pocket.targetAmount - realization).coerceAtLeast(0.0)
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

// ==================== SAKU PAGE TAB SWITCHER ====================
@Composable
fun SakuPageTabSwitcher(
    selectedTab: SakuPageTab,
    onTabSelected: (SakuPageTab) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SakuCreamSurfaceVariant,
        border = BorderStroke(1.dp, SakuCreamBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SakuTabOptionButton(
                label = "Kantong Dana",
                isSelected = selectedTab == SakuPageTab.KANTONG,
                onClick = { onTabSelected(SakuPageTab.KANTONG) }
            )
            SakuTabOptionButton(
                label = "Aset",
                isSelected = selectedTab == SakuPageTab.ASET,
                onClick = { onTabSelected(SakuPageTab.ASET) }
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SakuTabOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) SakuDarkGreen else Color.Transparent,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else SakuTextPrimary
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==================== ASSET COMPACT GRID CARD ====================
@Composable
fun AssetCompactGridCard(
    asset: Asset,
    planning: Double,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onTransfer: () -> Unit
) {
    val assetColor = parsePocketColor(asset.colorHex)
    val iconVector = getAssetIconVector(asset.iconName, asset.type)
    val typeLabel = getAssetTypeLabel(asset.type)
    val available = asset.balance - planning
    val isOver = available < 0

    Card(
        modifier = modifier
            .border(1.dp, SakuCreamBorder, RoundedCornerShape(18.dp))
            .clickable { onEdit() }
            .testTag("asset_card_${asset.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SakuCreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Icon + Type Badge + Edit action + Transfer action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(assetColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = asset.name,
                        tint = assetColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SakuLightGreen
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SakuDarkGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Transfer Button
                    IconButton(
                        onClick = onTransfer,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("transfer_asset_btn_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer Dana",
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Edit Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_asset_btn_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Aset",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Asset Name
            Text(
                text = asset.name,
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
                text = Formatters.formatRupiah(asset.balance),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SakuTextPrimary,
                    fontSize = 14.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Planning & Available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terplanning ${Formatters.formatRupiah(planning)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextSecondary,
                        fontSize = 10.5.sp
                    )
                )
                Text(
                    text = "Tersedia ${Formatters.formatRupiah(available)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isOver) SakuExpenseRed else SakuDarkGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SakuCreamBorder)
            )
        }
    }
}

// ==================== ASSET DETAILED CARD ====================
@Composable
fun AssetDetailedCard(
    asset: Asset,
    planning: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTransfer: () -> Unit
) {
    val assetColor = parsePocketColor(asset.colorHex)
    val iconVector = getAssetIconVector(asset.iconName, asset.type)
    val typeLabel = getAssetTypeLabel(asset.type)
    val available = asset.balance - planning
    val isOver = available < 0

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
                            .background(assetColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = asset.name,
                            tint = assetColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.name,
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
                                    text = typeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuDarkGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTransfer,
                        modifier = Modifier.testTag("transfer_asset_btn_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer Dana",
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_asset_btn_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Aset",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_asset_btn_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Aset",
                            tint = SakuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

                        // Planning & Available row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Terplanning ${Formatters.formatRupiah(planning)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "Tersedia ${Formatters.formatRupiah(available)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isOver) SakuExpenseRed else SakuDarkGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    text = Formatters.formatRupiah(asset.balance),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

// ==================== BUAT ASET MODAL ====================
@Composable
fun BuatAsetModal(
    uiState: KantongUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onTypeChange: (AssetType) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
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
                                contentDescription = "Kembali",
                                tint = SakuDarkGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Tambah Aset Baru",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Tambahkan rekening, e-wallet, atau pos kas",
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
                        // 1. Nama Aset
                        Column {
                            Text(
                                text = "Nama Aset",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newAssetName,
                                onValueChange = onNameChange,
                                placeholder = { Text("Contoh: BCA Payroll, GoPay, Dompet Tunai") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_asset_name_input")
                            )
                        }

                        // 2. Jenis Aset (Selectable Chips with Icons)
                        Column {
                            Text(
                                text = "Jenis Aset",
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
                                ASSET_TYPE_OPTIONS.forEach { opt ->
                                    val isSelected = uiState.newAssetType == opt.type
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) SakuDarkGreen else SakuCreamBorder
                                        ),
                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                        modifier = Modifier.clickable {
                                            onTypeChange(opt.type)
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

                        // 3. Saldo Saat Ini
                        Column {
                            Text(
                                text = "Saldo Saat Ini",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.newAssetBalance,
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saldo aktual yang ada di akun/rekening ini saat ini.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // 4. Warna Aset (Circular Color Choices)
                        Column {
                            Text(
                                text = "Warna Aset",
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
                                    val isSelected = uiState.newAssetColorHex.equals(colorOpt.hex, ignoreCase = true)
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

                        // 5. Pilih Icon (Selectable Icon Buttons)
                        Column {
                            Text(
                                text = "Pilih Ikon",
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
                                    val isSelected = uiState.newAssetIcon == iconOpt.id
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
                            .testTag("save_asset_button")
                    ) {
                        Text(
                            text = "Simpan Aset",
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

// ==================== EDIT ASET MODAL ====================
@Composable
fun EditAsetModal(
    uiState: KantongUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onTypeChange: (AssetType) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
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
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_edit_asset_button")
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
                                text = "Edit Aset",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Ubah informasi dan saldo aset",
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
                        // 1. Nama Aset
                        Column {
                            Text(
                                text = "Nama Aset",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.editAssetName,
                                onValueChange = onNameChange,
                                placeholder = { Text("Nama aset") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_asset_name_input")
                            )
                        }

                        // 2. Jenis Aset (Selectable Chips with Icons)
                        Column {
                            Text(
                                text = "Jenis Aset",
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
                                ASSET_TYPE_OPTIONS.forEach { opt ->
                                    val isSelected = uiState.editAssetType == opt.type
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SakuDarkGreen else SakuCreamSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) SakuDarkGreen else SakuCreamBorder
                                        ),
                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                        modifier = Modifier.clickable {
                                            onTypeChange(opt.type)
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

                        // 3. Saldo Saat Ini
                        Column {
                            Text(
                                text = "Saldo Saat Ini",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.editAssetBalance,
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
                                    .testTag("edit_asset_balance_input")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saldo aktual yang ada di akun/rekening ini saat ini.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // 4. Warna Aset (Circular Color Choices)
                        Column {
                            Text(
                                text = "Warna Aset",
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
                                    val isSelected = uiState.editAssetColorHex.equals(colorOpt.hex, ignoreCase = true)
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

                        // 5. Pilih Icon (Selectable Icon Buttons)
                        Column {
                            Text(
                                text = "Pilih Ikon",
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
                                    val isSelected = uiState.editAssetIcon == iconOpt.id
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

                    // Bottom Action Buttons: Batal and Simpan Perubahan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("cancel_edit_asset_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, SakuCreamBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SakuTextSecondary
                            )
                        ) {
                            Text(
                                text = "Batal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuTextSecondary
                                )
                            )
                        }

                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SakuDarkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("save_edit_asset_button")
                        ) {
                            Text(
                                text = "Simpan Perubahan",
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
}

// Edit Kantong Modal / Dialog (Matching Buat Kantong Baru Design)
@Composable
fun EditKantongModal(
    uiState: KantongUiState,
    assets: List<Asset>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAllocAdd: (String) -> Unit,
    onAllocRemove: (Int) -> Unit,
    onAllocNominalChange: (Int, String) -> Unit,
    onConfirmClearAlloc: () -> Unit,
    onCancelClearAlloc: () -> Unit,
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
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_edit_pocket_button")
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
                                text = "Edit Kantong",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen
                                )
                            )
                            Text(
                                text = "Ubah informasi dan target anggaran kantong",
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
                                value = uiState.editPocketName,
                                onValueChange = onNameChange,
                                placeholder = { Text("Nama kantong") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SakuCreamSurface,
                                    unfocusedContainerColor = SakuCreamSurface,
                                    focusedBorderColor = SakuDarkGreen,
                                    unfocusedBorderColor = SakuCreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_pocket_name_input")
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
                                    val isSelected = uiState.editPocketType == opt.id
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

                        // 3. Saldo Saat Ini (Read-only / Non-editable)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldo Saat Ini",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SakuLightGreen
                                ) {
                                    Text(
                                        text = "Hanya Lihat",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SakuDarkGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SakuCreamSurfaceVariant,
                                border = BorderStroke(1.dp, SakuCreamBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = Formatters.formatRupiah(uiState.editingPocketCurrentRealization),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Saldo terkunci",
                                        tint = SakuTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saldo dihitung otomatis dari riwayat transaksi dan perpindahan dana.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SakuTextSecondary,
                                    fontSize = 11.5.sp
                                )
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
                                value = uiState.editPocketTarget,
                                onValueChange = onTargetChange,
                                placeholder = { Text("0 (Kosongkan jika tanpa target)") },
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
                                    .testTag("edit_pocket_target_input")
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
                                    val isSelected = uiState.editPocketColorHex.equals(colorOpt.hex, ignoreCase = true)
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
                                    val isSelected = uiState.editPocketIcon == iconOpt.id
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
                                value = uiState.editPocketDescription,
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

                        // 8. Ambil dari Aset (Multi-Aset Planning)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ambil dari Aset",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Text(
                                    text = "Alokasikan nominal dari beberapa aset",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SakuTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Existing allocations list
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(uiState.editingPocketAllocations) { index, alloc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .background(SakuCreamSurface, RoundedCornerShape(12.dp))
                                            .border(BorderStroke(1.dp, SakuCreamBorder), RoundedCornerShape(12.dp))
                                    ) {
                                        // Asset badge/icon
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    parsePocketColor(alloc.assetName.hashCode().toString()).copy(alpha = 0.14f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getAssetIconVector(alloc.icon, AssetType.BANK),
                                                contentDescription = alloc.assetName,
                                                tint = parsePocketColor(alloc.assetName.hashCode().toString()),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Asset name + nominal input
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = alloc.assetName,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SakuTextPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            OutlinedTextField(
                                                value = alloc.nominalStr,
                                                onValueChange = { onAllocNominalChange(index, it) },
                                                placeholder = { Text("0") },
                                                prefix = { Text(text = "Rp ", color = SakuDarkGreen, fontWeight = FontWeight.Bold) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = SakuCreamSurface,
                                                    unfocusedContainerColor = SakuCreamSurface,
                                                    focusedBorderColor = SakuDarkGreen,
                                                    unfocusedBorderColor = SakuCreamBorder
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        // Delete button
                                        IconButton(
                                            onClick = { onAllocRemove(index) },
                                            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus alokasi",
                                                tint = SakuExpenseRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // + Tambah Aset button
                            val availableAssets = assets.filter { asset ->
                                uiState.editingPocketAllocations.none { it.assetId == asset.id }
                            }
                            if (availableAssets.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        availableAssets.firstOrNull()?.let { asset ->
                                            onAllocAdd(asset.id)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, SakuDarkGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SakuDarkGreen
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = SakuDarkGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "+ Tambah Aset",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SakuDarkGreen
                                            )
                                        )
                                    }
                                }
                            }

                            // Total allocation summary
                            val totalAllocated = uiState.editingPocketAllocations
                                .sumOf { Formatters.parseAmount(it.nominalStr.trim()).coerceAtLeast(0.0) }
                            if (totalAllocated > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total Alokasi",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SakuTextPrimary
                                        )
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(totalAllocated),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SakuDarkGreen
                                        )
                                    )
                                }
                            }
                        }

                        // Clear allocation confirmation
                        if (uiState.isClearAllocConfirmOpen) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SakuExpenseRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, SakuExpenseRed.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Hapus semua alokasi kantong ini?",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SakuExpenseRed
                                        )
                                    )
                                    Text(
                                        text = "Semua alokasi aset akan dihapus. Tindakan ini tidak dapat dibatalkan.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = onCancelClearAlloc,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Batal", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                        Button(
                                            onClick = onConfirmClearAlloc,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SakuExpenseRed)
                                        ) {
                                            Text("Hapus Semua", style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold, color = Color.White))
                                        }
                                    }
                                }
                            }
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

                    // Bottom Action Buttons: Batal and Simpan Perubahan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("cancel_edit_pocket_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, SakuCreamBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SakuTextSecondary
                            )
                        ) {
                            Text(
                                text = "Batal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuTextSecondary
                                )
                            )
                        }

                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SakuDarkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("save_edit_pocket_button")
                        ) {
                            Text(
                                text = "Simpan Perubahan",
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
}

// ==================== TRANSFER SHEET ====================
@Composable
fun TransferSheet(
    uiState: KantongUiState,
    assets: List<Asset>,
    onDismiss: () -> Unit,
    onTargetAssetChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = SakuCreamBackground
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Transfer Dana", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            
            // From
            Text("Dari: ${uiState.transferSourceAssetName}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            // To dropdown
            Text("Ke Aset", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            val targetAssets = assets.filter { it.id != uiState.transferSourceAssetId }
            if (targetAssets.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    targetAssets.take(3).forEach { asset ->
                        Button(onClick = { onTargetAssetChange(asset.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (asset.id == uiState.transferTargetAssetId) SakuDarkGreen else SakuCreamSurface
                            )
                        ) {
                            Text(asset.name.take(10), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount
            Text("Nominal", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            OutlinedTextField(
                value = uiState.transferAmountString,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0") },
                prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (uiState.transferWarning != null) {
                Text(uiState.transferWarning!!, style = MaterialTheme.typography.bodySmall.copy(color = SakuExpenseRed), modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Note
            OutlinedTextField(
                value = uiState.transferNote,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Catatan (opsional)") }
            )
            
            // Error
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, style = MaterialTheme.typography.bodySmall.copy(color = SakuExpenseRed), modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Batal")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen)) {
                    Text("Transfer")
                }
            }
        }
    }
}
