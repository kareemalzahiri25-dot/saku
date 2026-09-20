package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomNavItem
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                )
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = SakuDarkGreen
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SakuCreamBackground,
            titleContentColor = SakuDarkGreen
        )
    )
}

@Composable
fun SakuBottomNavigation(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Kantong,
        BottomNavItem.AddTransaction,
        BottomNavItem.Laporan,
        BottomNavItem.Profil
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = SakuCreamSurface,
        tonalElevation = 6.dp
    ) {
        NavigationBar(
            containerColor = SakuCreamSurface,
            tonalElevation = 0.dp,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val isAdd = item == BottomNavItem.AddTransaction

                if (isAdd) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = { onNavigateToRoute(item.route) },
                            shape = CircleShape,
                            color = SakuDarkGreen,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("bottom_nav_add_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Catat Transaksi",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                } else {
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onNavigateToRoute(item.route) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) SakuDarkGreen else SakuTextMuted
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SakuDarkGreen,
                            selectedTextColor = SakuDarkGreen,
                            indicatorColor = SakuLightGreen,
                            unselectedIconColor = SakuTextMuted,
                            unselectedTextColor = SakuTextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${item.route}")
                    )
                }
            }
        }
    }
}

@Composable
fun SakuCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SakuCreamSurface,
    borderColor: Color = SakuCreamBorder,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 2.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}

fun getCategoryIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "directions_car", "car", "transport" -> Icons.Default.DirectionsCar
        "shopping_bag", "shopping_cart", "cart" -> Icons.Default.ShoppingBag
        "receipt_long", "bills" -> Icons.Default.ReceiptLong
        "movie", "entertainment" -> Icons.Default.Movie
        "medication", "health_and_safety", "health" -> Icons.Default.LocalHospital
        "school", "education" -> Icons.Default.School
        "payments", "salary" -> Icons.Default.Payments
        "savings", "savings_pot" -> Icons.Default.Savings
        "trending_up", "investment" -> Icons.Default.TrendingUp
        "work", "laptop_mac", "freelance" -> Icons.Default.Work
        "swap_horiz", "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}
