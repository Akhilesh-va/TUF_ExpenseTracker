package com.financemanager.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.R
import com.financemanager.core.ui.components.AppButton
import com.financemanager.core.ui.components.AppTopBar
import com.financemanager.core.ui.components.ConfirmDialog
import com.financemanager.core.ui.utils.formatCurrency
import com.financemanager.core.ui.theme.isAppInLightTheme

private enum class ProfileTab { Preview, Edit }

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(ProfileTab.Preview) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is ProfileUiEvent.Snackbar -> onMessage(ev.message)
            }
        }
    }

    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_screen_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        AppTopBar(Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            ProfileUiState.Loading -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = scheme.primary)
                }
            }
            is ProfileUiState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is ProfileUiState.Success -> {
                val c = s.content
                ProfileHeaderRow(displayName = c.displayName)
                Spacer(Modifier.height(20.dp))
                ProfileSegmentedControl(
                    selected = tab,
                    onSelect = { tab = it },
                )
                Spacer(Modifier.height(24.dp))
                when (tab) {
                    ProfileTab.Preview -> ProfilePreviewContent(content = c)
                    ProfileTab.Edit -> ProfileEditContent(
                        content = c,
                        onUpdateDetails = { n, e, p, cp ->
                            viewModel.updateProfileDetails(n, e, p, cp)
                        },
                        onDarkModeChanged = { enabled ->
                            viewModel.onDarkModeChanged(enabled)
                        },
                        onLogout = { viewModel.logout() },
                        onClearClick = { showClearDialog = true },
                    )
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = stringResource(R.string.profile_clear_dialog_title),
            message = stringResource(R.string.profile_clear_dialog_message),
            confirmLabel = stringResource(R.string.profile_clear_confirm),
            dismissLabel = stringResource(R.string.profile_clear_cancel),
            onConfirm = {
                showClearDialog = false
                viewModel.clearAllData()
            },
            onDismiss = { showClearDialog = false },
        )
    }
}

@Composable
private fun ProfileHeaderRow(displayName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val letter = displayName.trim().firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: stringResource(R.string.home_logo_letter)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
        }
        Text(
            text = displayName.ifBlank { stringResource(R.string.profile_default_name_hint) },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ProfileSegmentedControl(
    selected: ProfileTab,
    onSelect: (ProfileTab) -> Unit,
) {
    val light = isAppInLightTheme()
    val scheme = MaterialTheme.colorScheme
    val track = if (light) scheme.surfaceVariant else Color(0xFF2C2C2C)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(track)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ProfileTab.entries.forEach { t ->
            val isSelected = t == selected
            val label = when (t) {
                ProfileTab.Preview -> stringResource(R.string.profile_tab_preview)
                ProfileTab.Edit -> stringResource(R.string.profile_tab_edit)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) {
                            if (light) scheme.surface else Color.White
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(t) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        !isSelected -> scheme.onSurfaceVariant
                        light -> scheme.primary
                        else -> Color.Black
                    },
                )
            }
        }
    }
}

@Composable
private fun ProfilePreviewContent(content: ProfileContent) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ProfilePreviewLine(
            label = stringResource(R.string.profile_total_spendings),
            value = formatCurrency(content.totalSpendings),
        )
        ProfilePreviewLine(
            label = stringResource(R.string.profile_email_label),
            value = content.email.ifBlank {
                stringResource(R.string.profile_email_placeholder)
            },
        )
        ProfilePreviewLine(
            label = stringResource(R.string.profile_balance_label),
            value = formatCurrency(content.balance),
        )
    }
}

@Composable
private fun ProfilePreviewLine(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun ProfileEditContent(
    content: ProfileContent,
    onUpdateDetails: (String, String, String, String) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onClearClick: () -> Unit,
) {
    var nameDraft by remember(content.displayName) { mutableStateOf(content.displayName) }
    var emailDraft by remember(content.email) { mutableStateOf(content.email) }
    var passwordDraft by remember { mutableStateOf("") }
    var confirmPasswordDraft by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(content.displayName, content.email) {
        nameDraft = content.displayName
        emailDraft = content.email
    }

    val light = isAppInLightTheme()
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ProfileLabeledField(
            label = stringResource(R.string.profile_full_name_label),
            value = nameDraft,
            onValueChange = { nameDraft = it },
            placeholder = stringResource(R.string.profile_full_name_placeholder),
        )
        ProfileLabeledField(
            label = stringResource(R.string.profile_email_label),
            value = emailDraft,
            onValueChange = { emailDraft = it },
            placeholder = stringResource(R.string.profile_email_hint_placeholder),
        )
        ProfileLabeledPasswordField(
            label = stringResource(R.string.profile_password_label),
            value = passwordDraft,
            onValueChange = { passwordDraft = it },
            placeholder = stringResource(R.string.profile_password_placeholder),
            visible = passwordVisible,
            onToggleVisible = { passwordVisible = !passwordVisible },
        )
        ProfileLabeledPasswordField(
            label = stringResource(R.string.profile_confirm_password_label),
            value = confirmPasswordDraft,
            onValueChange = { confirmPasswordDraft = it },
            placeholder = stringResource(R.string.profile_confirm_password_placeholder),
            visible = confirmPasswordVisible,
            onToggleVisible = { confirmPasswordVisible = !confirmPasswordVisible },
        )

        Spacer(Modifier.height(4.dp))
        AppButton(
            text = stringResource(R.string.profile_update_details),
            onClick = {
                onUpdateDetails(
                    nameDraft,
                    emailDraft,
                    passwordDraft,
                    confirmPasswordDraft,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (light) Color.Unspecified else Color.White,
            contentColor = if (light) Color.Unspecified else Color.Black,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = scheme.outline, thickness = 1.dp)
        Text(
            text = stringResource(R.string.profile_more_settings),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant,
        )
        val themeSwitchScale = remember { Animatable(1f) }
        LaunchedEffect(content.isDarkTheme) {
            themeSwitchScale.snapTo(1f)
            themeSwitchScale.animateTo(1.14f, tween(85))
            themeSwitchScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (content.isDarkTheme) {
                    stringResource(R.string.profile_dark_theme)
                } else {
                    stringResource(R.string.profile_light_theme)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = themeSwitchScale.value
                    scaleY = themeSwitchScale.value
                },
            ) {
                Switch(
                    checked = content.isDarkTheme,
                    onCheckedChange = { enabled ->
                        onDarkModeChanged(enabled)
                    },
                    colors = if (light) {
                        SwitchDefaults.colors(
                            checkedThumbColor = scheme.onPrimary,
                            checkedTrackColor = scheme.primary,
                            uncheckedThumbColor = scheme.primary,
                            uncheckedTrackColor = scheme.primaryContainer,
                            disabledCheckedThumbColor = scheme.onPrimary.copy(alpha = 0.5f),
                            disabledCheckedTrackColor = scheme.primary.copy(alpha = 0.4f),
                            disabledUncheckedThumbColor = scheme.primary.copy(alpha = 0.5f),
                            disabledUncheckedTrackColor = scheme.primaryContainer.copy(alpha = 0.5f),
                        )
                    } else {
                        SwitchDefaults.colors()
                    },
                )
            }
        }
        Text(
            text = stringResource(R.string.profile_version_line, content.appVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        AppButton(
            text = stringResource(R.string.profile_logout),
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (light) scheme.surfaceContainerHigh else Color(0xFF2A2A2A),
            contentColor = if (light) scheme.onSurface else Color.White,
        )
        AppButton(
            text = stringResource(R.string.profile_clear_data),
            onClick = onClearClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
private fun ProfileLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val light = isAppInLightTheme()
    val scheme = MaterialTheme.colorScheme
    val fieldBg = if (light) scheme.surfaceVariant else Color(0xFF1A1A1A)
    val fieldBorder = if (light) scheme.outline else Color(0xFF333333)
    val text = scheme.onSurface
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = text,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = text,
                unfocusedTextColor = text,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                disabledContainerColor = fieldBg,
                focusedBorderColor = fieldBorder,
                unfocusedBorderColor = fieldBorder,
                cursorColor = if (light) scheme.primary else Color.White,
            ),
        )
    }
}

@Composable
private fun ProfileLabeledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
) {
    val light = isAppInLightTheme()
    val scheme = MaterialTheme.colorScheme
    val fieldBg = if (light) scheme.surfaceVariant else Color(0xFF1A1A1A)
    val fieldBorder = if (light) scheme.outline else Color(0xFF333333)
    val text = scheme.onSurface
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = text,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisible) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visible) {
                            stringResource(R.string.cd_hide_password)
                        } else {
                            stringResource(R.string.cd_show_password)
                        },
                        tint = scheme.onSurfaceVariant,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = text,
                unfocusedTextColor = text,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                disabledContainerColor = fieldBg,
                focusedBorderColor = fieldBorder,
                unfocusedBorderColor = fieldBorder,
                focusedTrailingIconColor = scheme.onSurfaceVariant,
                unfocusedTrailingIconColor = scheme.onSurfaceVariant,
                cursorColor = if (light) scheme.primary else Color.White,
            ),
        )
    }
}
