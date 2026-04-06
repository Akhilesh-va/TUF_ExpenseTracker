package com.financemanager.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.financemanager.presentation.auth.AuthScreen
import com.financemanager.presentation.auth.AuthViewModel
import com.financemanager.presentation.balance.BalanceScreen
import com.financemanager.presentation.balance.BalanceViewModel
import com.financemanager.presentation.home.HomeScreen
import com.financemanager.presentation.home.HomeViewModel
import com.financemanager.presentation.profile.ProfileScreen
import com.financemanager.presentation.profile.ProfileViewModel
import com.financemanager.presentation.summary.SummaryScreen
import com.financemanager.presentation.summary.SummaryViewModel
import kotlinx.coroutines.launch

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(320)) +
                slideInVertically(animationSpec = tween(320)) { it / 10 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(220))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutVertically(animationSpec = tween(180)) { it / 10 }
        },
    ) {
        composable(Screen.Auth.route) {
            val vm: AuthViewModel = hiltViewModel()
            AuthScreen(viewModel = vm)
        }
        composable(Screen.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = vm)
        }
        composable(Screen.Balances.route) {
            val vm: BalanceViewModel = hiltViewModel()
            BalanceScreen(viewModel = vm)
        }
        composable(Screen.Summary.route) {
            val vm: SummaryViewModel = hiltViewModel()
            SummaryScreen(viewModel = vm)
        }
        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = vm,
                onMessage = { msg ->
                    keyboard?.hide()
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                },
            )
        }
    }
}
