package com.financemanager.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.R
import com.financemanager.core.ui.components.AppButton
import com.financemanager.core.ui.theme.isAppInLightTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val light = isAppInLightTheme()
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.home_logo_letter),
                    color = Color.Black,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = scheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }
        item { Spacer(Modifier.height(20.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (light) scheme.surface else Color(0xFF131416),
                ),
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.auth_get_started),
                        style = MaterialTheme.typography.headlineSmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.auth_get_started_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                    )
                    AuthModeSwitch(
                        selected = state.mode,
                        onSelect = viewModel::setMode,
                        light = light,
                    )
                    if (state.mode == AuthMode.SIGN_UP) {
                        AuthField(
                            label = stringResource(R.string.profile_full_name_label),
                            value = state.fullName,
                            onValueChange = viewModel::onFullNameChanged,
                            placeholder = stringResource(R.string.profile_full_name_placeholder),
                            light = light,
                        )
                    }
                    AuthField(
                        label = stringResource(R.string.profile_email_label),
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        placeholder = stringResource(R.string.profile_email_hint_placeholder),
                        light = light,
                    )
                    AuthPasswordField(
                        label = stringResource(R.string.profile_password_label),
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        placeholder = stringResource(R.string.auth_password_placeholder),
                        visible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible },
                        light = light,
                    )
                    if (state.mode == AuthMode.SIGN_UP) {
                        AuthPasswordField(
                            label = stringResource(R.string.profile_confirm_password_label),
                            value = state.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            placeholder = stringResource(R.string.profile_confirm_password_placeholder),
                            visible = confirmVisible,
                            onToggle = { confirmVisible = !confirmVisible },
                            light = light,
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Text(
                                text = stringResource(R.string.auth_forgot_password),
                                color = scheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    state.message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    AppButton(
                        text = stringResource(
                            if (state.mode == AuthMode.SIGN_IN) {
                                R.string.auth_sign_in
                            } else {
                                R.string.auth_create_account
                            },
                        ),
                        onClick = viewModel::submit,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (light) Color.Unspecified else Color.White,
                        contentColor = if (light) Color.Unspecified else Color.Black,
                        isLoading = state.isLoading,
                    )
                    if (state.isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                color = if (light) scheme.onPrimaryContainer else Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun AuthModeSwitch(
    selected: AuthMode,
    onSelect: (AuthMode) -> Unit,
    light: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val track = if (light) scheme.surfaceVariant else Color(0xFF26272A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(track)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(AuthMode.SIGN_IN, AuthMode.SIGN_UP).forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSelected) {
                            if (light) scheme.surface else Color.White
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(if (mode == AuthMode.SIGN_IN) R.string.auth_sign_in else R.string.auth_sign_up),
                    color = when {
                        !isSelected -> scheme.onSurfaceVariant
                        light -> scheme.primary
                        else -> Color.Black
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    light: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val fieldBg = if (light) scheme.surfaceVariant else Color(0xFF1A1B1E)
    val fieldBorder = if (light) scheme.outline else Color(0xFF2E3034)
    val text = scheme.onSurface
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            color = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = text,
                unfocusedTextColor = text,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                focusedBorderColor = fieldBorder,
                unfocusedBorderColor = fieldBorder,
                cursorColor = if (light) scheme.primary else Color.White,
            ),
        )
    }
}

@Composable
private fun AuthPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    light: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val fieldBg = if (light) scheme.surfaceVariant else Color(0xFF1A1B1E)
    val fieldBorder = if (light) scheme.outline else Color(0xFF2E3034)
    val text = scheme.onSurface
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            color = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(
                            if (visible) R.string.cd_hide_password else R.string.cd_show_password,
                        ),
                        tint = scheme.onSurfaceVariant,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = text,
                unfocusedTextColor = text,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                focusedBorderColor = fieldBorder,
                unfocusedBorderColor = fieldBorder,
                cursorColor = if (light) scheme.primary else Color.White,
            ),
        )
    }
}
