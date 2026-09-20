package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.SakuAppContainer
import com.example.ui.components.SakuBottomNavigation
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.export.ExportBackupScreen
import com.example.ui.screens.export.ExportBackupViewModel
import com.example.ui.screens.kantong.KantongScreen
import com.example.ui.screens.kantong.KantongViewModel
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.report.ReportScreen
import com.example.ui.screens.report.ReportViewModel
import com.example.ui.screens.transaction.AddTransactionScreen
import com.example.ui.screens.transaction.TransactionViewModel
import com.example.ui.theme.SakuCreamBackground

@Composable
fun SakuNavHost(
    container: SakuAppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Kantong.route,
        Screen.AddTransaction.route,
        Screen.Laporan.route,
        Screen.Profil.route
    )

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = SakuCreamBackground,
        bottomBar = {
            if (showBottomBar) {
                SakuBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Auth Destinations
            composable(Screen.Login.route) {
                val authVm = viewModel { AuthViewModel(container.repository) }
                LoginScreen(
                    viewModel = authVm,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                val authVm = viewModel { AuthViewModel(container.repository) }
                RegisterScreen(
                    viewModel = authVm,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                val authVm = viewModel { AuthViewModel(container.repository) }
                ForgotPasswordScreen(
                    viewModel = authVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Main Tab Destinations
            composable(Screen.Dashboard.route) {
                val dashboardVm = viewModel { DashboardViewModel(container.repository) }
                DashboardScreen(
                    viewModel = dashboardVm,
                    onNavigateToKantong = { navController.navigate(Screen.Kantong.route) },
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToLaporan = { navController.navigate(Screen.Laporan.route) },
                    onNavigateToExport = { navController.navigate(Screen.ExportBackup.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profil.route) }
                )
            }

            composable(Screen.Kantong.route) {
                val kantongVm = viewModel { KantongViewModel(container.repository) }
                KantongScreen(
                    viewModel = kantongVm,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                )
            }

            composable(Screen.AddTransaction.route) {
                val txVm = viewModel {
                    TransactionViewModel(container.repository, container.receiptScannerService)
                }
                AddTransactionScreen(
                    viewModel = txVm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profil.route)
                    }
                )
            }

            composable(Screen.Laporan.route) {
                val reportVm = viewModel { ReportViewModel(container.repository) }
                ReportScreen(viewModel = reportVm)
            }

            composable(Screen.Profil.route) {
                val profileVm = viewModel {
                    ProfileViewModel(
                        container.repository,
                        container.apiKeyConfigService,
                        container.currencyConversionService
                    )
                }
                ProfileScreen(
                    viewModel = profileVm,
                    onNavigateToExport = { navController.navigate(Screen.ExportBackup.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Sub-destinations
            composable(Screen.ExportBackup.route) {
                val exportVm = viewModel { ExportBackupViewModel(container.repository) }
                ExportBackupScreen(
                    viewModel = exportVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
