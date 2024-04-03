package com.bigbratan.rayvue.ui.auth.inputName

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.bigbratan.rayvue.ui.views.OutlinedTextButton

@Composable
internal fun InputNameScreen(
    viewModel: InputNameViewModel = hiltViewModel(),
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val typedNameState = remember { mutableStateOf(TextFieldValue()) }
    val isTypedNameValid = typedNameState.value.isValidName()

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
                        onNextClick()
                    },
                    isButtonEnabled = isTypedNameValid,
                )
            }
        }
    )
}