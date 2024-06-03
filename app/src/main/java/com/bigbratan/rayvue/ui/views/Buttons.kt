package com.bigbratan.rayvue.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans

@Composable
internal fun TonalIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick,
            ),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Icon(
            modifier = Modifier.padding(12.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
    }
}

@Composable
internal fun TransparentIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick,
            )
            .clip(CircleShape),
        imageVector = imageVector,
        tint = tint,
        contentDescription = null,
    )
}

@Composable
internal fun OutlinedIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {

}

@Composable
internal fun OutlinedTextButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isButtonEnabled: Boolean = true,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(56.dp))
            .run {
                if (isButtonEnabled) clickable(onClick = onClick) else this
            }
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (!isButtonEnabled) {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ),
                shape = RoundedCornerShape(56.dp)
            )
            .padding(vertical = 12.dp),
        text = label,
        fontFamily = plusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = if (isButtonEnabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        },
        style = TextStyle(
            platformStyle = noFontPadding,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
internal fun TonalTextButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isButtonEnabled: Boolean = true,
) {
    val backgroundColor = if (isButtonEnabled) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    }

    Text(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(56.dp))
            .run {
                if (isButtonEnabled) clickable(onClick = onClick) else this
            }
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        text = label,
        fontFamily = plusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        style = TextStyle(
            platformStyle = noFontPadding,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
internal fun TransparentTextButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(56.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        text = label,
        fontFamily = plusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = TextStyle(
            platformStyle = noFontPadding,
            textAlign = TextAlign.Center,
        ),
    )
}