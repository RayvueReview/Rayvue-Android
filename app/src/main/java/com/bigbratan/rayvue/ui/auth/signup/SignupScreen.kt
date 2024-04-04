package com.bigbratan.rayvue.ui.auth.signup

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
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.OutlinedTextButton
import com.bigbratan.rayvue.ui.views.Popup

@Composable
internal fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val validatedAuthDataState = viewModel.validatedAuthDataState.collectAsState()

    val typedEmailState = remember { mutableStateOf(TextFieldValue()) }
    var typedEmailError by remember { mutableStateOf(false) }
    val isTypedEmailValid = typedEmailState.value.isValidEmail()

    val typedPasswordState = remember { mutableStateOf(TextFieldValue()) }
    val isTypedPasswordValid = typedPasswordState.value.isValidPassword()

    var isPopupVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (validatedAuthDataState.value) {
            ValidatedAuthDataState.Idle -> {
                Unit
            }

            ValidatedAuthDataState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                )
            }

            is ValidatedAuthDataState.True -> {
                viewModel.addAuthData(
                    email = typedEmailState.value.text,
                    password = typedPasswordState.value.text,
                )
                viewModel.resetState()
                onNextClick()
            }

            is ValidatedAuthDataState.False -> {
                typedEmailError = true
                viewModel.resetState()
            }

            ValidatedAuthDataState.Error -> {
                isPopupVisible = true
            }
        }

        AuthView(
            prompt = stringResource(id = R.string.signup_prompt_message),
            onBackClick = {
                viewModel.removeAllData()
                onBackClick()
            },
            content = {
                Column {
                    AuthTextField(
                        sentAuthData = typedEmailState,
                        hint = stringResource(id = R.string.signup_email_hint),
                        support = if (typedEmailError) stringResource(id = R.string.signup_email_error) else stringResource(
                            id = R.string.signup_email_support
                        ),
                        keyboardType = KeyboardType.Email,
                        isPassword = false,
                        isErrorVisible = typedEmailError,
                    )

                    AuthTextField(
                        modifier = Modifier.padding(top = 24.dp),
                        sentAuthData = typedPasswordState,
                        hint = stringResource(id = R.string.signup_password_hint),
                        support = stringResource(id = R.string.signup_password_support),
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                    )

                    OutlinedTextButton(
                        modifier = Modifier.padding(vertical = 32.dp),
                        label = stringResource(id = R.string.signup_button_title),
                        onClick = {
                            typedEmailError = false
                            viewModel.checkEmail(email = typedEmailState.value.text)
                        },
                        isButtonEnabled = isTypedEmailValid && isTypedPasswordValid,
                    )
                }
            }
        )

        Popup(
            title = stringResource(id = R.string.error_title),
            message = stringResource(id = R.string.signup_validate_data_error_message),
            hasNegativeAction = false,
            isPopupVisible = isPopupVisible,
            onDismiss = {
                isPopupVisible = false
                viewModel.resetState()
            },
            onConfirm = {
                isPopupVisible = false
                viewModel.resetState()
            }
        )
    }
}