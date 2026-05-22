package com.extrotarget.extroposv2.keygen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.extrotarget.extroposv2.ui.keygen.KeyGenScreen
import com.extrotarget.extroposv2.ui.theme.ExtroPOSV2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KeygenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ExtroPOSV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KeyGenScreen()
                }
            }
        }
    }
}
