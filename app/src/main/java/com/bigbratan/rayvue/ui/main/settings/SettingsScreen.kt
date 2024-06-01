package com.bigbratan.rayvue.ui.main.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.BackNavigationBar
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.Popup

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val sentLogOutDataState = viewModel.sentLogOutDataState.collectAsState()
    val sentSignOutDataState = viewModel.sentSignOutDataState.collectAsState()
    val obtainedUserState = viewModel.obtainedUserState.collectAsState()

    val areLoginSignupVisible = viewModel.areLoginSignupVisible.collectAsState()

    var isLogOutErrorPopupVisible by remember { mutableStateOf(false) }
    var isSignOutErrorPopupVisible by remember { mutableStateOf(false) }
    var isLogOutPopupVisible by remember { mutableStateOf(false) }
    var isSignOutPopupVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getUser()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (obtainedUserState.value) {
            ObtainedUserState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier.fillMaxSize()
                )
            }

            ObtainedUserState.Error -> {
                ErrorMessage(
                    message = stringResource(
                        id = R.string.settings_get_data_error_message
                    ),
                    isInHomeScreen = false,
                    onBackClick = { onBackClick() }
                )
            }

            is ObtainedUserState.Success -> {
                val userName = (obtainedUserState.value as ObtainedUserState.Success).userName

                when (sentLogOutDataState.value) {
                    SentLogOutDataState.Idle -> {
                        Unit
                    }

                    SentLogOutDataState.Loading -> {
                        LoadingAnimation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                                .align(Alignment.TopCenter)
                        )
                    }

                    is SentLogOutDataState.Success -> {
                        onBackClick()
                        viewModel.resetStates()
                    }

                    SentLogOutDataState.Error -> {
                        isLogOutPopupVisible = false
                        isLogOutErrorPopupVisible = true
                    }
                }

                when (sentSignOutDataState.value) {
                    SentSignOutDataState.Idle -> {
                        Unit
                    }

                    SentSignOutDataState.Loading -> {
                        LoadingAnimation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                                .align(Alignment.TopCenter)
                        )
                    }

                    is SentSignOutDataState.Success -> {
                        onBackClick()
                        viewModel.resetStates()
                    }

                    SentSignOutDataState.Error -> {
                        isSignOutPopupVisible = false
                        isSignOutErrorPopupVisible = true
                    }
                }

                Popup(
                    title = stringResource(id = R.string.error_title),
                    message = if (isSignOutErrorPopupVisible) stringResource(id = R.string.settings_sing_out_error_message) else stringResource(
                        id = R.string.settings_log_out_error_message
                    ),
                    isPopupVisible = isLogOutErrorPopupVisible || isSignOutErrorPopupVisible,
                    hasNegativeAction = false,
                    onConfirm = {
                        isLogOutErrorPopupVisible = false
                        isSignOutErrorPopupVisible = false
                        viewModel.resetStates()
                    },
                    onDismiss = {
                        isLogOutErrorPopupVisible = false
                        isSignOutErrorPopupVisible = false
                        viewModel.resetStates()
                    },
                )

                SettingsView(
                    userName = userName,
                    areLoginSignupVisible = areLoginSignupVisible.value,
                    onBackClick = { onBackClick() },
                    onLogOutClick = { viewModel.exitAccount() },
                    onSignOutClick = { viewModel.deleteAccount() },
                    onLoginClick = { onLoginClick() },
                    onSignupClick = { onSignupClick() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsView(
    userName: String?,
    areLoginSignupVisible: Boolean,
    onLogOutClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var isLogOutPopupVisible by remember { mutableStateOf(false) }
    var isSignOutPopupVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        topBar = {
            BackNavigationBar(
                title = stringResource(id = R.string.settings_title),
                onBackClick = { onBackClick() },
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 12.dp
                    )
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    text = userName?.let { userName ->
                        stringResource(
                            id = R.string.settings_welcome_account_message,
                            userName
                        )
                    }
                        ?: stringResource(id = R.string.settings_welcome_no_account_message),
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                )

                Text(
                    text = stringResource(id = R.string.settings_category_account_title),
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                )

                if (areLoginSignupVisible) {
                    SettingCard(
                        modifier = Modifier
                            .padding(
                                top = 8.dp,
                                bottom = 2.dp,
                            )
                            .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)),
                        icon = Icons.Filled.Login,
                        text = stringResource(id = R.string.settings_login_text),
                        subtext = stringResource(id = R.string.settings_login_subtext),
                        shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp),
                        onSettingClick = { onLoginClick() },
                    )

                    SettingCard(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)),
                        icon = Icons.Filled.PersonAdd,
                        text = stringResource(id = R.string.settings_signup_text),
                        subtext = stringResource(id = R.string.settings_signup_subtext),
                        shape = RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp),
                        onSettingClick = { onSignupClick() },
                    )
                } else {
                    SettingCard(
                        modifier = Modifier
                            .padding(
                                top = 8.dp,
                                bottom = 2.dp,
                            )
                            .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)),
                        icon = Icons.Filled.Logout,
                        text = stringResource(id = R.string.settings_log_out_text),
                        subtext = stringResource(id = R.string.settings_log_out_subtext),
                        shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp),
                        onSettingClick = { isLogOutPopupVisible = true },
                    )

                    SettingCard(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)),
                        icon = Icons.Filled.Delete,
                        text = stringResource(id = R.string.settings_sign_out_text),
                        subtext = stringResource(id = R.string.settings_sign_out_subtext),
                        shape = RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp),
                        onSettingClick = { isSignOutPopupVisible = true },
                    )
                }

                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = stringResource(id = R.string.settings_category_info_title),
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                )

                SettingCard(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            bottom = 2.dp,
                        )
                        .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)),
                    icon = Icons.Filled.Info,
                    text = stringResource(R.string.settings_about_reviewer_text),
                    subtext = stringResource(R.string.settings_about_reviewer_subtext),
                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp),
                    onSettingClick = {},
                )

                SettingCard(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    icon = Icons.Filled.Info,
                    text = stringResource(R.string.settings_about_invite_text),
                    subtext = stringResource(R.string.settings_about_invite_subtext),
                    shape = RoundedCornerShape(4.dp),
                    onSettingClick = {},
                )

                SettingCard(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)),
                    icon = Icons.Filled.Code,
                    text = stringResource(
                        id = R.string.settings_app_version_text,
                        LocalContext.current.packageManager.getPackageInfo(
                            LocalContext.current.packageName,
                            PackageManager.GET_META_DATA
                        ).longVersionCode
                    ),
                    shape = RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp),
                    onSettingClick = {},
                )
            }
        }
    )

    Popup(
        title = if (isSignOutPopupVisible) stringResource(id = R.string.warning_title) else stringResource(
            id = R.string.question_title
        ),
        message = if (isSignOutPopupVisible) stringResource(id = R.string.settings_sign_out_prompt_message) else stringResource(
            id = R.string.settings_log_out_prompt_message
        ),
        isPopupVisible = isLogOutPopupVisible || isSignOutPopupVisible,
        onConfirm = {
            if (isLogOutPopupVisible) {
                onLogOutClick()
                isLogOutPopupVisible = false
            } else if (isSignOutPopupVisible) {
                onSignOutClick()
                isSignOutPopupVisible = false
            }
        },
        onDismiss = {
            isLogOutPopupVisible = false
            isSignOutPopupVisible = false
        },
    )
}

@Composable
private fun SettingCard(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    subtext: String? = null,
    shape: RoundedCornerShape,
    onSettingClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSettingClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .padding(vertical = 8.dp)
                    .size(24.dp),
                imageVector = icon,
                contentDescription = null,
            )

            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = text,
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                )

                if (subtext != null) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = subtext,
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            platformStyle = noFontPadding,
                            letterSpacing = 0.15.sp,
                        ),
                    )
                }
            }
        }
    }
}