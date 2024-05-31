package com.bigbratan.rayvue.ui.utils

import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.text.isDigitsOnly

internal fun TextFieldValue.isValidEmail(): Boolean {
    val pattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    return text.matches(pattern.toRegex()) && text.isNotEmpty()
}

internal fun TextFieldValue.isValidPassword(): Boolean {
    val hasDigit = text.any { it.isDigit() }
    val hasUppercase = text.any { it.isUpperCase() }

    return text.length >= 10 && hasDigit && hasUppercase && text.isNotEmpty()
}

internal fun TextFieldValue.isNotEmptyPassword(): Boolean {
    return text.isNotEmpty()
}

internal fun TextFieldValue.isValidName(): Boolean {
    return text.length in 3..20 && text.isNotEmpty()
}