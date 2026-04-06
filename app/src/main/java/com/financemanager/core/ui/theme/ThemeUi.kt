package com.financemanager.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

/** True when the active [MaterialTheme] color scheme is the light palette (neutral background). */
@Composable
fun isAppInLightTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f
