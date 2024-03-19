package com.bigbratan.rayvue.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.theme.rubikMonoOne
import com.bigbratan.rayvue.ui.views.OutlinedButton
import com.bigbratan.rayvue.ui.views.TonalButton
import com.bigbratan.rayvue.ui.views.TransparentButton

@Composable
internal fun AccountScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onSkipClick: () -> Unit,
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
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.app_name),
                fontFamily = rubikMonoOne,
                fontSize = 56.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    platformStyle = noFontPadding,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                ),
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                text = stringResource(id = R.string.account_prompt_message),
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    platformStyle = noFontPadding,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }

        Column {
            OutlinedButton(
                label = stringResource(id = R.string.account_button_login_title),
                onClick = { onLoginClick() }
            )

            TonalButton(
                modifier = Modifier.padding(top = 32.dp),
                label = stringResource(id = R.string.account_button_signup_title),
                onClick = { onSignupClick() }
            )

            TransparentButton(
                modifier = Modifier.padding(vertical = 32.dp),
                label = stringResource(id = R.string.account_button_no_account_title),
                onClick = { onSkipClick() }
            )
        }
    }
}