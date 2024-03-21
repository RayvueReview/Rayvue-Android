package com.bigbratan.rayvue.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.bigbratan.rayvue.navigation.Navigation
import com.bigbratan.rayvue.ui.theme.RayvueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            RayvueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Navigation()
                }
            }
        }
    }
}