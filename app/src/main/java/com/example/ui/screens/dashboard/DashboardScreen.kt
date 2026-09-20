package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.ui.components.SakuCard
import com.example.ui.components.getCategoryIconVector
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuDarkGreenDeep
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuExpenseRedBg
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuIncomeGreenBg
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuMediumGreen
import com.example.ui.theme.SakuSageAccent
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToKantong: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToLaporan: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Header Greeting & Profile
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Halo, ${user?.name ?: "Budi Santoso"} 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = "Kelola finansial pribadimu hari ini",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SakuTextSecondary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onNavigateToProfile,
                        shape = CircleShape,
                        color = SakuLightGreen,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("dashboard_profile_avatar")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profil Pengguna",
                                tint = SakuDarkGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Hero Total Saldo Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SakuDarkGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("total_balance_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SakuDarkGreen, SakuMediumGreen, SakuDarkGreenDeep)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Total Saldo Saku",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "IDR",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.toggleBalanceVisibility() },
                                    modifier = Modifier.size(32.dp).testTag("toggle_balance_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Tampilkan Saldo",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isBalanceVisible) Formatters.formatRupiah(summary.totalBalance) else "Rp ••••••••",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Pemasukan & Pengeluaran Pill row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Pemasukan
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(SakuIncomeGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Pemasukan",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = if (isBalanceVisible) Formatters.formatRupiah(summary.totalIncomeThisMonth) else "••••••",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Pengeluaran
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(SakuExpenseRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Pengeluaran",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = if (isBalanceVisible) Formatters.formatRupiah(summary.totalExpenseThisMonth) else "••••••",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Catat",
                    onClick = onNavigateToAddTransaction
                )
                QuickActionButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Kantong",
                    onClick = onNavigateToKantong
                )
                QuickActionButton(
                    icon = Icons.Default.Analytics,
                    label = "Laporan",
                    onClick = onNavigateToLaporan
                )
                QuickActionButton(
                    icon = Icons.Default.CloudDownload,
                    label = "Ekspor",
                    onClick = onNavigateToExport
                )
            }
        }

        // 4. Kantong Saya Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kantong Dana",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
                TextButton(onClick = onNavigateToKantong) {
                    Text(
                        text = "Kelola (${pockets.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SakuDarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Kantong Horizontal List
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pockets) { pocket ->
                    PocketItemCard(
                        pocket = pocket,
                        isBalanceVisible = isBalanceVisible,
                        onClick = onNavigateToKantong
                    )
                }
            }
        }

        // 5. Transaksi Terakhir Section
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terakhir",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
                TextButton(onClick = onNavigateToLaporan) {
                    Text(
                        text = "Semua Riwayat",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SakuDarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada transaksi. Ketuk '+' untuk mulai mencatat!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextMuted)
                    )
                }
            }
        } else {
            items(recentTransactions) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SakuCreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SakuDarkGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SakuTextPrimary
            )
        )
    }
}

@Composable
fun PocketItemCard(
    pocket: Pocket,
    isBalanceVisible: Boolean,
    onClick: () -> Unit
) {
    val progress = if (pocket.targetAmount > 0) {
        (pocket.balance / pocket.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    SakuCard(
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() },
        backgroundColor = SakuCreamSurface,
        cornerRadius = 20.dp,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SakuLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = SakuDarkGreen,
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
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pocket.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isBalanceVisible) Formatters.formatRupiah(pocket.balance) else "••••••",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (pocket.targetAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = SakuDarkGreen,
                    trackColor = SakuLightGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}% dari target",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    SakuCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isIncome = transaction.type == TransactionType.INCOME
            val isTransfer = transaction.type == TransactionType.TRANSFER

            val iconBg = when {
                isIncome -> SakuIncomeGreenBg
                isTransfer -> SakuGoldLight
                else -> SakuExpenseRedBg
            }
            val iconTint = when {
                isIncome -> SakuIncomeGreen
                isTransfer -> SakuGoldAccent
                else -> SakuExpenseRed
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIconVector(transaction.categoryIcon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SakuTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.categoryName} • ${transaction.pocketName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = when {
                    isIncome -> "+"
                    isTransfer -> "⇄ "
                    else -> "-"
                }
                val amountColor = when {
                    isIncome -> SakuIncomeGreen
                    isTransfer -> SakuGoldAccent
                    else -> SakuExpenseRed
                }

                Text(
                    text = "$prefix${Formatters.formatRupiah(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )
                Text(
                    text = Formatters.formatShortDateIndo(transaction.dateMillis),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
