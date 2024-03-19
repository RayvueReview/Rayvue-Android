package com.bigbratan.rayvue.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans

@Composable
internal fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null,
                onClick = {
                    if (onClick != null) {
                        onClick()
                    }
                }
            )
            .padding(
                vertical = 12.dp,
                horizontal = 24.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight(500),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.15.sp,
            ),
        )

        if (imageVector != null && onClick != null) {
            TransparentIconButton(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = imageVector,
                onClick = onClick,
            )
        }
    }
}
