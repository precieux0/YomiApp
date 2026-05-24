package com.yomi.mangaflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yomi.mangaflow.ui.viewmodel.MainViewModel
import com.yomi.mangaflow.ui.viewmodel.SortOrder
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    navController: NavController,
    viewModel: MainViewModel = koinViewModel()
) {
    val currentLang by viewModel.filterLang.collectAsState()
    val currentGenre by viewModel.filterGenre.collectAsState()
    val currentStatus by viewModel.filterStatus.collectAsState()
    val currentSort by viewModel.sortOrder.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    val genres = remember(availableTags) { availableTags.filter { it.category == "genre" }.sortedBy { it.name } }

    val languages = listOf("all" to "Toutes", "en" to "Anglais", "fr" to "Français", "jp" to "Japonais", "ko" to "Coréen", "zh" to "Chinois", "es" to "Espagnol")
    val statuses = listOf("all" to "Tous", "Ongoing" to "En cours", "Completed" to "Terminé", "Hiatus" to "En pause", "Cancelled" to "Annulé")
    val sortOrders = listOf(SortOrder.Popularity to "Popularité", SortOrder.Title to "Titre (A-Z)", SortOrder.Latest to "Plus récents", SortOrder.Rating to "Note")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtres et Tri") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { TextButton(onClick = { viewModel.resetFilters() }) { Text("Réinitialiser") } }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("Trier par", style = MaterialTheme.typography.titleMedium)
                sortOrders.forEach { (order, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        RadioButton(selected = currentSort == order, onClick = { viewModel.setSortOrder(order) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
            item {
                Text("Langue", style = MaterialTheme.typography.titleMedium)
                languages.forEach { (code, label) ->
                    FilterRadioRow(label, currentLang == code) { viewModel.setFilterLang(code) }
                }
            }
            item {
                Text("Statut", style = MaterialTheme.typography.titleMedium)
                statuses.forEach { (code, label) ->
                    FilterRadioRow(label, currentStatus == code) { viewModel.setFilterStatus(code) }
                }
            }
            if (genres.isNotEmpty()) {
                item {
                    Text("Genres", style = MaterialTheme.typography.titleMedium)
                    FilterRadioRow("Tous", currentGenre == "all") { viewModel.setFilterGenre("all") }
                    genres.forEach { tag ->
                        FilterRadioRow(tag.name, currentGenre == tag.name) { viewModel.setFilterGenre(tag.name) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
