package com.gmail.vanyadubik.myassistant.featurelogin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.language.LanguageSwitchButton
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ConfirmBottomSheet
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.button.ThemeSwitchButton
import com.gmail.vanyadubik.myassistant.featurelogin.model.LoginUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.app_title
import com.gmail.vanyadubik.myassistant.resource.back
import com.gmail.vanyadubik.myassistant.resource.email
import com.gmail.vanyadubik.myassistant.resource.email_empty
import com.gmail.vanyadubik.myassistant.resource.have_account
import com.gmail.vanyadubik.myassistant.resource.loading
import com.gmail.vanyadubik.myassistant.resource.login_title
import com.gmail.vanyadubik.myassistant.resource.nickname
import com.gmail.vanyadubik.myassistant.resource.nickname_empty
import com.gmail.vanyadubik.myassistant.resource.no_account
import com.gmail.vanyadubik.myassistant.resource.oops_error_title
import com.gmail.vanyadubik.myassistant.resource.password
import com.gmail.vanyadubik.myassistant.resource.password_empty
import com.gmail.vanyadubik.myassistant.resource.register_title
import com.gmail.vanyadubik.myassistant.resource.retry
import com.gmail.vanyadubik.myassistant.resource.sign_in
import com.gmail.vanyadubik.myassistant.resource.sign_up
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(onBack: () -> Unit, viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    LoginContent(
        state = state,
        themeAnimationHandler = viewModel.themeAnimationHandler,
        onBack = onBack,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onNicknameChanged = viewModel::onNicknameChanged,
        onSubmit = viewModel::submit,
        onToggleMode = viewModel::toggleMode,
    )

    if (state.actionError != null) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.oops_error_title),
            message = state.actionError,
            confirmText = stringResource(Res.string.retry),
            onConfirm = viewModel::retryLastAction,
            onDismiss = viewModel::dismissActionError,
        )
    }

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    themeAnimationHandler: ThemeAnimationHandler,
    onBack: () -> Unit,
    onEmailChanged: () -> Unit,
    onPasswordChanged: () -> Unit,
    onNicknameChanged: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    onToggleMode: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
        }
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageSwitchButton()
            ThemeSwitchButton(
                themeHandler = themeAnimationHandler,
                iconModifier = Modifier.padding(8.dp),
                iconSize = 24.dp,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.app_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(if (state.isRegister) Res.string.register_title else Res.string.login_title),
                style = MaterialTheme.typography.titleMedium,
            )

            if (state.isRegister) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it; onNicknameChanged() },
                    label = { Text(stringResource(Res.string.nickname)) },
                    isError = state.nicknameError,
                    supportingText = if (state.nicknameError) {
                        { Text(stringResource(Res.string.nickname_empty)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onEmailChanged() },
                label = { Text(stringResource(Res.string.email)) },
                isError = state.emailError,
                supportingText = if (state.emailError) {
                    { Text(stringResource(Res.string.email_empty)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onPasswordChanged() },
                label = { Text(stringResource(Res.string.password)) },
                isError = state.passwordError,
                supportingText = if (state.passwordError) {
                    { Text(stringResource(Res.string.password_empty)) }
                } else null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = stringResource(if (state.isRegister) Res.string.sign_up else Res.string.sign_in),
                onClick = { onSubmit(email, password, nickname) },
                loadingMessage = if (state.isLoading) stringResource(Res.string.loading) else null,
            )
            TextButton(onClick = onToggleMode) {
                Text(stringResource(if (state.isRegister) Res.string.have_account else Res.string.no_account))
            }
        }
    }
}
