package com.bigbratan.rayvue.ui.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal suspend fun encodeField(field: String) = withContext(Dispatchers.IO) {
    URLEncoder.encode(
        field,
        StandardCharsets.UTF_8.toString()
    )
}