package com.gmail.vanyadubik.mytaxes.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import com.gmail.vanyadubik.mytaxes.featurecalculator.CalculatorScreen
import com.gmail.vanyadubik.mytaxes.featurehistory.HistoryScreen
import com.gmail.vanyadubik.mytaxes.featurelogin.LoginScreen
import com.gmail.vanyadubik.mytaxes.viewmodel.MainViewModel

sealed class AppScreen(val route: String) {
    data object Login : AppScreen("login")
    data object Calculator : AppScreen("calculator")
    data object History : AppScreen("history")
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    // The session is read from the data store asynchronously, the splash screen covers this time
    val loggedIn = isLoggedIn ?: return
    val navController = rememberNavController()

    // Leave the login screen once the user has signed in, and the history once they signed out
    LaunchedEffect(loggedIn) {
        val route = navController.currentDestination?.route
        if (loggedIn && route == AppScreen.Login.route || !loggedIn && route == AppScreen.History.route) {
            navController.popBackStack()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreen.Calculator.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(AppScreen.Login.route) {
            LoginScreen(onBack = { navController.popBackStack() })
        }
        composable(AppScreen.Calculator.route) {
            CalculatorScreen(
                onOpenHistory = { navController.navigate(AppScreen.History.route) },
                onOpenLogin = { navController.navigate(AppScreen.Login.route) },
            )
        }
        composable(AppScreen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
