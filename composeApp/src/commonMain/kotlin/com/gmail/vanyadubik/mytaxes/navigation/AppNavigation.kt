package com.gmail.vanyadubik.mytaxes.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
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
import com.gmail.vanyadubik.mytaxes.featureaccount.AccountScreen
import com.gmail.vanyadubik.mytaxes.featurecalculator.CalculatorScreen
import com.gmail.vanyadubik.mytaxes.featurehistory.HistoryScreen
import com.gmail.vanyadubik.mytaxes.featurehome.HomeScreen
import com.gmail.vanyadubik.mytaxes.featurelogin.LoginScreen
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.history
import com.gmail.vanyadubik.mytaxes.resource.nav_account
import com.gmail.vanyadubik.mytaxes.resource.nav_calculator
import com.gmail.vanyadubik.mytaxes.resource.nav_home
import com.gmail.vanyadubik.mytaxes.resource.sign_in
import com.gmail.vanyadubik.mytaxes.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource

sealed class AppScreen(val route: String) {
    data object Login : AppScreen("login")
    data object Home : AppScreen("home")
    data object Calculator : AppScreen("calculator")
    data object History : AppScreen("history")
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

    // Leave the login screen once the user has signed in, and the history once they signed out
    LaunchedEffect(loggedIn) {
        val route = navController.currentDestination?.route
        if (loggedIn && route == AppScreen.Login.route) {
            navController.popBackStack()
        } else if (!loggedIn && (route == AppScreen.History.route || route == AppScreen.Account.route)) {
            // Do not keep the state of the signed out user's tabs
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
                        selected = currentDestination.isIn(AppScreen.Calculator),
                        onClick = { navController.navigateToTab(AppScreen.Calculator) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                        label = { Text(stringResource(Res.string.nav_calculator)) },
                    )
                    // History is stored per user, so it only exists for signed in users
                    if (loggedIn) {
                        NavigationBarItem(
                            selected = currentDestination.isIn(AppScreen.History),
                            onClick = { navController.navigateToTab(AppScreen.History) },
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text(stringResource(Res.string.history)) },
                        )
                    }
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
                HomeScreen(onOpenLogin = { navController.navigate(AppScreen.Login.route) })
            }
            composable(AppScreen.Calculator.route) {
                CalculatorScreen(onOpenLogin = { navController.navigate(AppScreen.Login.route) })
            }
            composable(AppScreen.History.route) {
                HistoryScreen()
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
