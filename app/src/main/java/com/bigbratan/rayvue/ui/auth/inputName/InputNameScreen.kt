package com.bigbratan.rayvue.ui.auth.inputName

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
import com.bigbratan.rayvue.ui.utils.isValidName
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.OutlinedTextButton
import com.bigbratan.rayvue.ui.views.Popup

@Composable
internal fun InputNameScreen(
    viewModel: InputNameViewModel = hiltViewModel(),
    onFinishClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val sentSignupDataState = viewModel.sentSignupDataState.collectAsState()

    val typedNameState = remember { mutableStateOf(TextFieldValue()) }
    val isTypedNameValid = typedNameState.value.isValidName()

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
                viewModel.resetState()
            }

            SentSignupDataState.Error -> {
                isPopupVisible = true
            }
        }


        AuthView(
            prompt = stringResource(id = R.string.input_name_prompt_message),
            onBackClick = {
                viewModel.removeAllData()
                onBackClick()
            },
            content = {
                Column {
                    AuthTextField(
                        hint = stringResource(id = R.string.input_name_name_hint),
                        sentAuthData = typedNameState,
                        support = stringResource(id = R.string.input_name_name_support),
                        keyboardType = KeyboardType.Text,
                        isPassword = false,
                    )

                    OutlinedTextButton(
                        modifier = Modifier.padding(vertical = 32.dp),
                        label = stringResource(id = R.string.input_name_button_title),
                        onClick = {
                            viewModel.addName(
                                userName = typedNameState.value.text,
                            )
                            viewModel.createAccount()
                        },
                        isButtonEnabled = isTypedNameValid,
                    )
                }
            }
        )

        Popup(
            title = stringResource(id = R.string.error_title),
            message = stringResource(
                id = R.string.input_name_send_signup_data_error_message
            ),
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