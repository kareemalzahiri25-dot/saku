package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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
    actions: @Composable RowScope.() -> Unit = {},
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

    val navbarHeight = 68.dp
    val cornerRadius = 28.dp
    val buttonSize = 52.dp
    val buttonElevation = 16.dp
    val notchDepth = 20.dp
    
    // Custom navbar dengan curve notch di tengah
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(navbarHeight + notchDepth)
    ) {
        // Background dengan notch
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
        ) {
            val width = size.width
            val height = size.height
            val notchWidth = buttonSize.toPx() * 1.8f
            val notchX = width / 2
            
            val path = Path().apply {
                // Mulai dari kiri bawah
                moveTo(0f, height)
                // Ke kiri atas
                lineTo(0f, notchDepth.toPx())
                // Curve ke kanan atas (sudut kiri atas)
                quadraticBezierTo(0f, 0f, cornerRadius.toPx(), 0f)
                // Horizontal ke notch kiri
                lineTo(notchX - notchWidth / 2 - cornerRadius.toPx(), 0f)
                // Curve bezier ke notch (kiri)
                cubicTo(
                    x1 = notchX - notchWidth / 2 + 8.dp.toPx(),
                    y1 = 0f,
                    x2 = notchX - notchWidth / 2 + 12.dp.toPx(),
                    y2 = notchDepth.toPx() * 0.3f,
                    x3 = notchX - notchWidth / 2 + 24.dp.toPx(),
                    y3 = notchDepth.toPx() * 0.8f
                )
                // Curve bezier notch tengah bawah
                cubicTo(
                    x1 = notchX - 16.dp.toPx(),
                    y1 = notchDepth.toPx() + 8.dp.toPx(),
                    x2 = notchX + 16.dp.toPx(),
                    y2 = notchDepth.toPx() + 8.dp.toPx(),
                    x3 = notchX + notchWidth / 2 - 24.dp.toPx(),
                    y3 = notchDepth.toPx() * 0.8f
                )
                // Curve bezier notch kanan
                cubicTo(
                    x1 = notchX + notchWidth / 2 - 12.dp.toPx(),
                    y1 = notchDepth.toPx() * 0.3f,
                    x2 = notchX + notchWidth / 2 - 8.dp.toPx(),
                    y2 = 0f,
                    x3 = notchX + notchWidth / 2 + cornerRadius.toPx(),
                    y3 = 0f
                )
                // Horizontal ke kanan atas
                lineTo(width - cornerRadius.toPx(), 0f)
                // Curve kanan atas
                quadraticBezierTo(width, 0f, width, cornerRadius.toPx())
                // Ke kanan bawah
                lineTo(width, height)
                // Tutup path
                close()
            }
            
            drawPath(path, color = SakuCreamSurface)
        }
        
        // Layout untuk 5 item (add di tengah, 2 kiri, 2 kanan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(navbarHeight),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Item 1: Dashboard
            items[0].let { item ->
                NavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onNavigate = onNavigateToRoute
                )
            }
            
            // Item 2: Kantong
            items[1].let { item ->
                NavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onNavigate = onNavigateToRoute
                )
            }
            
            // Spacer untuk add button yang elevated
            Box(modifier = Modifier.weight(1f)) {
                // Elevated add button dengan label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = -buttonElevation)
                        .align(Alignment.BottomCenter)
                ) {
                    Surface(
                        onClick = { onNavigateToRoute(items[2].route) },
                        shape = CircleShape,
                        color = SakuDarkGreen,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .size(buttonSize)
                            .testTag("bottom_nav_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Transaksi",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tambah",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = SakuTextMuted
                        )
                    )
                }
            }
            
            // Item 4: Laporan
            items[3].let { item ->
                NavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onNavigate = onNavigateToRoute
                )
            }
            
            // Item 5: Profil
            items[4].let { item ->
                NavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onNavigate = onNavigateToRoute
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    item: BottomNavItem,
    selected: Boolean,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onNavigate(item.route) }
            .padding(vertical = 8.dp)
            .testTag("nav_item_${item.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = if (selected) SakuDarkGreen else SakuTextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) SakuDarkGreen else SakuTextMuted
            )
        )
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
        "receipt_long", "bills" -> Icons.AutoMirrored.Filled.ReceiptLong
        "movie", "entertainment" -> Icons.Default.Movie
        "medication", "health_and_safety", "health" -> Icons.Default.LocalHospital
        "school", "education" -> Icons.Default.School
        "payments", "salary" -> Icons.Default.Payments
        "savings", "savings_pot" -> Icons.Default.Savings
        "trending_up", "investment" -> Icons.AutoMirrored.Filled.TrendingUp
        "work", "laptop_mac", "freelance" -> Icons.Default.Work
        "swap_horiz", "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}
