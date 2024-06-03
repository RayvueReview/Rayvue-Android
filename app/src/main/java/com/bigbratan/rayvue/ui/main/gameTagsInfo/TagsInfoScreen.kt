package com.bigbratan.rayvue.ui.main.gameTagsInfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.BackNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagsInfoScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        topBar = {
            BackNavigationBar(
                title = stringResource(id = R.string.tags_info_title),
                onBackClick = { onBackClick() },
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal =  24.dp)
            ) {
                item {
                    TagInfoCard(
                        modifier = Modifier.padding(top = 12.dp),
                        title = stringResource(id = R.string.tags_info_sub_title),
                        meaning = stringResource(id = R.string.tags_info_sub_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_micro_title),
                        meaning = stringResource(id = R.string.tags_info_micro_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_pass_title),
                        meaning = stringResource(id = R.string.tags_info_pass_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_loot_title),
                        meaning = stringResource(id = R.string.tags_info_loot_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_p2w_title),
                        meaning = stringResource(id = R.string.tags_info_p2w_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_malicious_title),
                        meaning = stringResource(id = R.string.tags_info_malicious_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_tos_title),
                        meaning = stringResource(id = R.string.tags_info_tos_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_fake_title),
                        meaning = stringResource(id = R.string.tags_info_fake_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_ad_title),
                        meaning = stringResource(id = R.string.tags_info_ad_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        title = stringResource(id = R.string.tags_info_dev_title),
                        meaning = stringResource(id = R.string.tags_info_dev_meaning),
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(
                            vertical = 24.dp,
                            horizontal = 16.dp
                        ),
                    )
                }

                item {
                    TagInfoCard(
                        modifier = Modifier.padding(bottom = 24.dp),
                        title = stringResource(id = R.string.tags_info_scam_title),
                        meaning = stringResource(id = R.string.tags_info_scam_meaning),
                    )
                }
            }
        }
    )
}

@Composable
private fun TagInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    meaning: String
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
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
            modifier = Modifier.padding(top = 12.dp),
            text = meaning,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.15.sp,
            ),
        )
    }
}