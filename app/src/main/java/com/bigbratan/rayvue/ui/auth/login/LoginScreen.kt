package com.bigbratan.rayvue.ui.auth.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.auth.AuthTextField
import com.bigbratan.rayvue.ui.auth.AuthView
import com.bigbratan.rayvue.ui.utils.isValidEmail
import com.bigbratan.rayvue.ui.utils.isValidPassword
import com.bigbratan.rayvue.ui.views.Popup
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.OutlinedButton

@Composable
internal fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onFinishClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val sentLoginDataState = viewModel.sentLoginDataState.collectAsState()

    val typedEmailState = remember { mutableStateOf(TextFieldValue()) }
    var typedEmailError by remember { mutableStateOf(false) }
    val isTypedEmailValid = typedEmailState.value.isValidEmail()

    val typedPasswordState = remember { mutableStateOf(TextFieldValue()) }
    var typedPasswordError by remember { mutableStateOf(false) }
    val isTypedPasswordValid = typedPasswordState.value.isValidPassword()

    var isPopupVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (sentLoginDataState.value) {
            SentLoginDataState.Idle -> {
                Unit
            }

            SentLoginDataState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                )
            }

            is SentLoginDataState.Success -> {
                onFinishClick()
                viewModel.resetState()
            }

            SentLoginDataState.EmailBad -> {
                typedEmailError = true
                viewModel.resetState()
            }

            SentLoginDataState.PasswordBad -> {
                typedPasswordError = true
                viewModel.resetState()
            }

            SentLoginDataState.Error -> {
                isPopupVisible = true
            }
        }

        AuthView(
            prompt = stringResource(id = R.string.login_prompt_message),
            onBackClick = { onBackClick() },
            content = {
                Column {
                    AuthTextField(
                        hint = stringResource(id = R.string.login_email_hint),
                        sentAuthData = typedEmailState,
                        support = if (typedEmailError) stringResource(id = R.string.login_email_error) else stringResource(
                            id = R.string.login_email_support
                        ),
                        keyboardType = KeyboardType.Email,
                        isPassword = false,
                        isErrorVisible = typedEmailError,
                    )

                    AuthTextField(
                        modifier = Modifier.padding(top = 24.dp),
                        sentAuthData = typedPasswordState,
                        hint = stringResource(id = R.string.login_password_hint),
                        support = if (typedPasswordError) stringResource(id = R.string.login_password_error) else stringResource(
                            id = R.string.login_password_support
                        ),
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isErrorVisible = typedPasswordError,
                    )

                    OutlinedButton(
                        modifier = Modifier.padding(vertical = 32.dp),
                        label = stringResource(id = R.string.login_button_title),
                        onClick = {
                            typedEmailError = false
                            typedPasswordError = false
                            viewModel.enterAccount(
                                email = typedEmailState.value.text,
                                password = typedPasswordState.value.text,
                            )
                        },
                        isButtonEnabled = isTypedEmailValid && isTypedPasswordValid,
                    )
                }
            }
        )

        Popup(
            title = stringResource(id = R.string.error_title),
            message = stringResource(id = R.string.login_send_data_error_message),
            hasNegativeAction = false,
            isPopupVisible = isPopupVisible,
            onConfirm = {
                isPopupVisible = false
                viewModel.resetState()
            },
            onDismiss = {
                isPopupVisible = false
                viewModel.resetState()
            }
        )
    }
}