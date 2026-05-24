package com.yomi.mangaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yomi.mangaflow.data.local.SettingsDataStore
import com.yomi.mangaflow.data.local.ThemePreference
import com.yomi.mangaflow.ui.navigation.AppNavigation
import com.yomi.mangaflow.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settingsDataStore: SettingsDataStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by settingsDataStore.themePreference
                .collectAsState(initial = ThemePreference.SYSTEM)
            AppTheme(themePreference = theme) {
                AppNavigation()
            }
        }
    }
}
