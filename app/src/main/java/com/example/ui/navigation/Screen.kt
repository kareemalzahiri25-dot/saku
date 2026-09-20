package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    // Main Tabs
    object Dashboard : Screen("dashboard")
    object Kantong : Screen("kantong")
    object AddTransaction : Screen("add_transaction")
    object Laporan : Screen("laporan")
    object Profil : Screen("profil")

    // Sub-screens
    object ExportBackup : Screen("export_backup")
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        label = "Beranda",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Kantong : BottomNavItem(
        route = Screen.Kantong.route,
        label = "Kantong",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    )

    object AddTransaction : BottomNavItem(
        route = Screen.AddTransaction.route,
        label = "Catat",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Filled.AddCircle
    )

    object Laporan : BottomNavItem(
        route = Screen.Laporan.route,
        label = "Laporan",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )

    object Profil : BottomNavItem(
        route = Screen.Profil.route,
        label = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
