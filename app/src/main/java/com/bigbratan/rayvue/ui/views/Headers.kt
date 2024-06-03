package com.bigbratan.rayvue.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans

@Composable
internal fun ContentSectionHeader(
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
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(platformStyle = noFontPadding),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackNavigationBar(
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = title,
                fontFamily = plusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            TransparentIconButton(
                modifier = Modifier.padding(start = 16.dp),
                imageVector = Icons.Filled.ArrowBack,
                onClick = onBackClick,
            )
        }
    )
}
