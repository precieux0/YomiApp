package com.yomi.mangaflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yomi.mangaflow.data.local.SettingsDataStore
import com.yomi.mangaflow.data.local.ThemePreference
import com.yomi.mangaflow.ui.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel = koinViewModel(),
    settingsDataStore: SettingsDataStore = koinInject()
) {
    val theme by settingsDataStore.themePreference.collectAsState(initial = ThemePreference.SYSTEM)
    val syncEnabled by settingsDataStore.syncEnabled.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSection(title = "Apparence") {
                ThemeSelector(
                    currentTheme = theme,
                    onThemeSelected = { viewModel.setTheme(it) }
                )
            }

            SettingsSection(title = "Lecture") {
                SettingsItem(
                    icon = Icons.Default.MenuBook,
                    title = "Mode de lecture par défaut",
                    subtitle = "Standard",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Activer les mises à jour",
                    onClick = { },
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                )
            }

            SettingsSection(title = "Synchronisation") {
                SettingsItem(
                    icon = Icons.Default.CloudSync,
                    title = "Synchronisation",
                    subtitle = if (syncEnabled) "Activée" else "Désactivée",
                    onClick = { viewModel.setSyncEnabled(!syncEnabled) },
                    trailing = {
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { viewModel.setSyncEnabled(it) }
                        )
                    }
                )
            }

            SettingsSection(title = "À propos") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "À propos",
                    subtitle = "Okitakoy Corp · Crédits",
                    onClick = { navController.navigate("about") }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) { content() }
        }
    }
}

@Composable
private fun ThemeSelector(currentTheme: ThemePreference, onThemeSelected: (ThemePreference) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Thème", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip(Icons.Default.BrightnessAuto, "Système", currentTheme == ThemePreference.SYSTEM) { onThemeSelected(ThemePreference.SYSTEM) }
            ThemeChip(Icons.Default.LightMode, "Clair", currentTheme == ThemePreference.LIGHT) { onThemeSelected(ThemePreference.LIGHT) }
            ThemeChip(Icons.Default.DarkMode, "Sombre", currentTheme == ThemePreference.DARK) { onThemeSelected(ThemePreference.DARK) }
            ThemeChip(Icons.Default.Palette, "Sépia", currentTheme == ThemePreference.SEPIA) { onThemeSelected(ThemePreference.SEPIA) }
        }
    }
}

@Composable
private fun ThemeChip(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Card(
            modifier = Modifier.size(56.dp).clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            ),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector, title: String, subtitle: String,
    onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke() ?: Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)
        )
    }
}
