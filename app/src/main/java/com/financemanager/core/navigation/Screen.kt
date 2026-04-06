package com.financemanager.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Auth : Screen("auth", "Auth", Icons.Outlined.Home)
    data object Home : Screen("home", "Home", Icons.Outlined.Home)
    data object Balances : Screen("balances", "Balances", Icons.Outlined.AccountBalanceWallet)
    data object Summary : Screen("summary", "Summary", Icons.Outlined.PieChart)
    data object Profile : Screen("profile", "Profile", Icons.Outlined.Person)
}

/**
 * Top-level list avoids companion/nested `data object` initialization ordering that can
 * yield null entries in the list on first access (runtime NPE on [Screen.route]).
 */
val BottomNavDestinations: List<Screen> = listOf(
    Screen.Home,
    Screen.Balances,
    Screen.Summary,
    Screen.Profile,
)
