package com.bigbratan.rayvue.ui.main.awards

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun AwardsScreen(
    viewModel: AwardsViewModel = hiltViewModel(),
) {
    AwardsView()
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun AwardsView() {
    Scaffold(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxSize(),
    ) {

    }
}