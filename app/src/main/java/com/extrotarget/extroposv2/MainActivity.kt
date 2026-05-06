package com.extrotarget.extroposv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.extrotarget.extroposv2.core.network.P2PManager
import com.extrotarget.extroposv2.core.work.LhdnPollingWorker
import com.extrotarget.extroposv2.ui.navigation.MainScreen
import com.extrotarget.extroposv2.ui.theme.ExtroPOSV2Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var p2pManager: P2PManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize P2P Services
        p2pManager.initialize()
        
        // Enqueue periodic LHDN polling
        LhdnPollingWorker.enqueue(this)

        setContent {
            ExtroPOSV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}