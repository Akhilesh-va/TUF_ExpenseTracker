package com.financemanager.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.financemanager.core.ui.theme.AppRadius
import com.financemanager.core.ui.theme.isAppInLightTheme

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    /** Use [Color.Unspecified] to apply the default: tonal fill in light theme, solid primary in dark. */
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
) {
    val scheme = MaterialTheme.colorScheme
    val light = isAppInLightTheme()
    val resolvedContainer = when {
        containerColor != Color.Unspecified -> containerColor
        light -> scheme.primaryContainer
        else -> scheme.primary
    }
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        light -> scheme.onPrimaryContainer
        else -> scheme.onPrimary
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(AppRadius.Button),
        colors = ButtonDefaults.buttonColors(
            containerColor = resolvedContainer,
            contentColor = resolvedContent,
            disabledContainerColor = resolvedContainer.copy(alpha = 0.5f),
            disabledContentColor = resolvedContent.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.width(20.dp),
                color = resolvedContent,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
