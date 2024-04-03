package com.bigbratan.rayvue.ui.auth.inputInvite

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
import com.bigbratan.rayvue.ui.utils.isValidCode
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.OutlinedTextButton
import com.bigbratan.rayvue.ui.views.Popup
import com.bigbratan.rayvue.ui.views.TransparentTextButton

@Composable
internal fun InputInviteScreen(
    viewModel: InputInviteViewModel = hiltViewModel(),
    onFinishClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val sentSignupDataState = viewModel.sentSignupDataState.collectAsState()
    val validatedCodeState = viewModel.validatedCodeState.collectAsState()

    val typedCodeState = remember { mutableStateOf(TextFieldValue()) }
    var typedCodeError by remember { mutableStateOf(false) }
    val isTypedCodeValid = typedCodeState.value.isValidCode()

    var isPopupVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (sentSignupDataState.value) {
            SentSignupDataState.Idle -> {
                Unit
            }

            SentSignupDataState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                )
            }

            is SentSignupDataState.Success -> {
                onFinishClick()
                viewModel.resetStates()
            }

            SentSignupDataState.Error -> {
                isPopupVisible = true
            }
        }

        when (validatedCodeState.value) {
            ValidatedCodeState.Idle -> {
                Unit
            }

            ValidatedCodeState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                )
            }

            is ValidatedCodeState.True -> {
                viewModel.createAccount(isReviewer = true)
                viewModel.resetStates()
            }

            is ValidatedCodeState.False -> {
                typedCodeError = true
                viewModel.resetStates()
            }

            ValidatedCodeState.Error -> {
                isPopupVisible = true
            }
        }

        AuthView(
            prompt = stringResource(id = R.string.input_invite_prompt_message),
            onBackClick = {
                viewModel.removeName()
                onBackClick()
            },
            content = {
                Column {
                    AuthTextField(
                        hint = stringResource(id = R.string.input_invite_invite_hint),
                        sentAuthData = typedCodeState,
                        support = if (typedCodeError) stringResource(id = R.string.input_invite_validate_data_error_message) else stringResource(
                            id = R.string.input_invite_invite_support
                        ),
                        keyboardType = KeyboardType.Number,
                        isPassword = false,
                        isErrorVisible = typedCodeError
                    )

                    OutlinedTextButton(
                        modifier = Modifier.padding(top = 32.dp),
                        label = stringResource(id = R.string.input_invite_button_enter_title),
                        onClick = {
                            typedCodeError = false
                            viewModel.checkCode(inviteCode = typedCodeState.value.text.toInt())
                        },
                        isButtonEnabled = isTypedCodeValid,
                    )

                    TransparentTextButton(
                        modifier = Modifier.padding(vertical = 32.dp),
                        label = stringResource(id = R.string.input_invite_button_skip_title),
                        onClick = {
                            viewModel.createAccount(isReviewer = false)
                        },
                    )
                }
            }
        )

        Popup(
            title = stringResource(id = R.string.error_title),
            message = if (typedCodeError) stringResource(id = R.string.input_invite_send_code_data_error_message) else stringResource(
                id = R.string.input_invite_send_signup_data_error_message
            ),
            hasNegativeAction = false,
            isPopupVisible = isPopupVisible,
            onConfirm = {
                isPopupVisible = false
                viewModel.resetStates()
            },
            onDismiss = {
                isPopupVisible = false
                viewModel.resetStates()
            }
        )
    }
}