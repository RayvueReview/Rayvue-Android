package com.bigbratan.rayvue.ui.utils

import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.text.isDigitsOnly

internal fun TextFieldValue.isValidEmail(): Boolean {
    val pattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    return text.matches(pattern.toRegex()) && text.isNotEmpty()
}

internal fun TextFieldValue.isValidPassword(): Boolean {
    return text.length >= 10 && text.isNotEmpty()
}

internal fun TextFieldValue.isValidName(): Boolean {
    return text.length in 3..20 && text.isNotEmpty()
}

internal fun TextFieldValue.isValidCode(): Boolean {
    return text.length == 9 && text.isDigitsOnly() && text.isNotEmpty()
}