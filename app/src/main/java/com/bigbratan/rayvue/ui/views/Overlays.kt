package com.bigbratan.rayvue.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.theme.Black50
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans

@Composable
internal fun FadingScrimBackground(
    aspectRatio: Float,
    bottomColor: Color,
    roundedCornerShape: RoundedCornerShape,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, bottomColor)
    )

    Box(
        modifier = Modifier
            .clip(roundedCornerShape)
            .background(brush = gradient)
            .aspectRatio(aspectRatio)
    )
}

@Composable
internal fun SolidScrimBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black50)
    )
}

@Composable
internal fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ErrorMessage(
    message: String,
    isInHomeScreen: Boolean,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        if (!isInHomeScreen) {
            TransparentIconButton(
                modifier = Modifier
                    .padding(24.dp),
                imageVector = Icons.Filled.ArrowBack,
                onClick = onBackClick
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
            text = message,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.15.sp,
            ),
        )
    }
}

@Composable
internal fun Popup(
    title: String,
    message: String,
    hasNegativeAction: Boolean = true,
    isPopupVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (isPopupVisible) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = { onConfirm() }) {
                    Text(text = stringResource(id = R.string.action_positive_title))
                }
            },
            dismissButton = {
                if (hasNegativeAction) {
                    Button(onClick = { onDismiss() }) {
                        Text(text = stringResource(id = R.string.action_negative_title))
                    }
                }
            }
        )
    }
}