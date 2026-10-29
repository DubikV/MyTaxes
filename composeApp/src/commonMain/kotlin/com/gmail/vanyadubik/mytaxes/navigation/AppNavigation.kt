package com.gmail.vanyadubik.myassistant.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gmail.vanyadubik.myassistant.featureaccount.AccountScreen
import com.gmail.vanyadubik.myassistant.featureauto.AutoScreen
import com.gmail.vanyadubik.myassistant.featurehome.HomeScreen
import com.gmail.vanyadubik.myassistant.featurelogin.LoginScreen
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.history.TaxesHistoryScreen
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.TaxesScreen
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.nav_account
import com.gmail.vanyadubik.myassistant.resource.nav_auto
import com.gmail.vanyadubik.myassistant.resource.nav_home
import com.gmail.vanyadubik.myassistant.resource.nav_taxes
import com.gmail.vanyadubik.myassistant.resource.sign_in
import com.gmail.vanyadubik.myassistant.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource

sealed class AppScreen(val route: String) {
    data object Login : AppScreen("login")
    data object Home : AppScreen("home")
    data object Taxes : AppScreen("taxes")
    data object TaxesHistory : AppScreen("taxes_history")
    data object Auto : AppScreen("auto")
    data object Account : AppScreen("account")
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    // The session is read from the data store asynchronously, the splash screen covers this time
    val loggedIn = isLoggedIn ?: return
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    LaunchedEffect(loggedIn) {
        val route = navController.currentDestination?.route
        if (loggedIn && route == AppScreen.Login.route) {
            navController.popBackStack()
        } else if (!loggedIn && route == AppScreen.Account.route) {
            navController.navigate(AppScreen.Home.route) {
                popUpTo(AppScreen.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != AppScreen.Login.route) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination.isIn(AppScreen.Home),
                        onClick = { navController.navigateToTab(AppScreen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(Res.string.nav_home)) },
                    )
                    NavigationBarItem(
                        selected = currentDestination.isIn(AppScreen.Taxes) || currentDestination.isIn(AppScreen.TaxesHistory),
                        onClick = { navController.navigateToTab(AppScreen.Taxes) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text(stringResource(Res.string.nav_taxes)) },
                    )
                    NavigationBarItem(
                        selected = currentDestination.isIn(AppScreen.Auto),
                        onClick = { navController.navigateToTab(AppScreen.Auto) },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        label = { Text(stringResource(Res.string.nav_auto)) },
                    )
                    NavigationBarItem(
                        selected = currentDestination.isIn(AppScreen.Account),
                        onClick = {
                            if (loggedIn) navController.navigateToTab(AppScreen.Account)
                            else navController.navigate(AppScreen.Login.route)
                        },
                        icon = {
                            Icon(
                                if (loggedIn) Icons.Default.AccountCircle else Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(if (loggedIn) Res.string.nav_account else Res.string.sign_in)) },
                    )
                }
            }
        },
    ) { padding ->
        // The screens have their own top bars and handle the top insets themselves
        val bottomPadding = PaddingValues(bottom = padding.calculateBottomPadding())
        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.route,
            modifier = Modifier.fillMaxSize().padding(bottomPadding).consumeWindowInsets(bottomPadding),
        ) {
            composable(AppScreen.Login.route) {
                LoginScreen(onBack = { navController.popBackStack() })
            }
            composable(AppScreen.Home.route) {
                HomeScreen(
                    onOpenLogin = { navController.navigate(AppScreen.Login.route) },
                    onOpenTaxes = { navController.navigateToTab(AppScreen.Taxes) },
                    onOpenTaxesHistory = { navController.navigate(AppScreen.TaxesHistory.route) },
                    onOpenAuto = { navController.navigateToTab(AppScreen.Auto) },
                )
            }
            composable(AppScreen.Taxes.route) {
                TaxesScreen(onOpenHistory = { navController.navigate(AppScreen.TaxesHistory.route) })
            }
            composable(AppScreen.TaxesHistory.route) {
                TaxesHistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(AppScreen.Auto.route) {
                AutoScreen(onOpenLogin = { navController.navigate(AppScreen.Login.route) })
            }
            composable(AppScreen.Account.route) {
                AccountScreen()
            }
        }
    }
}

private fun androidx.navigation.NavDestination?.isIn(screen: AppScreen) =
    this?.hierarchy?.any { it.route == screen.route } == true

private fun NavHostController.navigateToTab(screen: AppScreen) {
    navigate(screen.route) {
        popUpTo(AppScreen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
