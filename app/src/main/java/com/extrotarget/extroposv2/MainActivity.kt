package com.extrotarget.extroposv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.extrotarget.extroposv2.core.network.P2PManager
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.util.LocaleHelper
import com.extrotarget.extroposv2.core.work.LhdnPollingWorker
import com.extrotarget.extroposv2.ui.navigation.MainScreen
import com.extrotarget.extroposv2.ui.theme.ExtroPOSV2Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var p2pManager: P2PManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply initial locale before content is set
        val initialLocale = runBlocking { settingsRepository.languageCode.first() }
        LocaleHelper.applyLocale(this, initialLocale)

        // Observe locale changes
        lifecycleScope.launch {
            settingsRepository.languageCode.collect { code ->
                LocaleHelper.applyLocale(this@MainActivity, code)
            }
        }
        
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