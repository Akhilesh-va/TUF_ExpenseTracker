package com.financemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financemanager.core.navigation.BottomNavBar
import com.financemanager.core.navigation.FinanceNavHost
import com.financemanager.core.navigation.Screen
import com.financemanager.core.ui.theme.FinanceManagerTheme
import com.financemanager.domain.usecase.ObserveThemeModeUseCase
import com.financemanager.presentation.main.MainUiViewModel
import com.financemanager.presentation.main.SessionState
import com.financemanager.presentation.splash.SplashScreen
import com.financemanager.presentation.transactions.AddTransactionBottomSheet
import com.financemanager.presentation.transactions.AddTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeThemeModeUseCase: ObserveThemeModeUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by observeThemeModeUseCase()
                .collectAsStateWithLifecycle(initialValue = "DARK")
            FinanceManagerTheme(darkTheme = themeMode == "DARK") {
                val navController = rememberNavController()
                var showAddSheet by rememberSaveable { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }
                val addVm: AddTransactionViewModel = hiltViewModel()
                val mainUi: MainUiViewModel = hiltViewModel()
                val sessionState by mainUi.sessionState.collectAsStateWithLifecycle()
                var splashDone by rememberSaveable { mutableStateOf(false) }
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val isAuthenticated = sessionState == SessionState.AUTHENTICATED
                val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route

                LaunchedEffect(Unit) {
                    delay(1900)
                    splashDone = true
                }

                val shouldShowSplash = !splashDone || sessionState == SessionState.CHECKING
                if (shouldShowSplash) {
                    SplashScreen(modifier = Modifier)
                    return@FinanceManagerTheme
                }

                LaunchedEffect(isAuthenticated, currentRoute) {
                    if (!isAuthenticated && currentRoute != Screen.Auth.route) {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (isAuthenticated && currentRoute == Screen.Auth.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (isAuthenticated) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                    floatingActionButton = {
                        if (isAuthenticated) {
                            val interaction = remember { MutableInteractionSource() }
                            val pressed by interaction.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (pressed) 0.9f else 1f,
                                animationSpec = tween(120),
                                label = "fabScale",
                            )
                            FloatingActionButton(
                                onClick = { showAddSheet = true },
                                interactionSource = interaction,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                },
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                            }
                        }
                    },
                ) { padding ->
                    FinanceNavHost(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding),
                    )
                    if (isAuthenticated) {
                        AddTransactionBottomSheet(
                            visible = showAddSheet,
                            onDismiss = { showAddSheet = false },
                            viewModel = addVm,
                        )
                    }
                }
            }
        }
    }
}
