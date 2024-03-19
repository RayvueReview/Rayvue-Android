package com.bigbratan.rayvue.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.TransparentIconButton

@Composable
internal fun AuthView(
    prompt: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                vertical = 52.dp,
                horizontal = 36.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.height(LocalConfiguration.current.screenHeightDp.dp / 2),
            verticalArrangement = Arrangement.Bottom,
        ) {
            TransparentIconButton(
                imageVector = Icons.Filled.ArrowBack,
                onClick = onBackClick
            )

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = prompt,
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    platformStyle = noFontPadding,
                    letterSpacing = 0.15.sp,
                ),
            )
        }

        content()
    }
}

@Composable
internal fun AuthTextField(
    modifier: Modifier = Modifier,
    sentAuthData: MutableState<TextFieldValue>,
    hint: String,
    support: String,
    keyboardType: KeyboardType,
    isPassword: Boolean,
    isErrorVisible: Boolean = false,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    val imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility

    val visualTransformation =
        if (isPassword) {
            if (isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation()
        } else VisualTransformation.None

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        singleLine = true,
        isError = isErrorVisible,
        label = {
            Text(
                text = hint,
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
            )
        },
        supportingText = {
            Text(
                text = support,
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
            )
        },
        trailingIcon = {
            if (isPassword) {
                TransparentIconButton(
                    imageVector = imageVector,
                    onClick = {
                        isPasswordVisible = !isPasswordVisible
                    }
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
        value = sentAuthData.value,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        onValueChange = { newValue ->
            sentAuthData.value = newValue
        }
    )
}