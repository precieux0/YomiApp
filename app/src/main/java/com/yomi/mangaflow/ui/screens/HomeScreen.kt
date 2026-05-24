package com.yomi.mangaflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.yomi.mangaflow.data.model.Manga
import com.yomi.mangaflow.ui.viewmodel.MainViewModel
import com.yomi.mangaflow.ui.viewmodel.UiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = koinViewModel(),
    onOpenDrawer: () -> Unit = {}
) {
    val popularState by viewModel.popularManga.collectAsState()
    val latestState by viewModel.latestManga.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()
    val filterLang by viewModel.filterLang.collectAsState()
    val filterGenre by viewModel.filterGenre.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val hasFilters = filterLang != "all" || filterGenre != "all" || filterStatus != "all"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yomi") },
                navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, null) } },
                actions = { IconButton(onClick = { navController.navigate("search") }) { Icon(Icons.Default.Search, null) } }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Découvrir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Source : $currentSource", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (hasFilters) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (filterLang != "all") AssistChip(onClick = { viewModel.setFilterLang("all") }, label = { Text("Langue: ${filterLang.uppercase()}") })
                            if (filterGenre != "all") AssistChip(onClick = { viewModel.setFilterGenre("all") }, label = { Text("Genre: $filterGenre") })
                            if (filterStatus != "all") AssistChip(onClick = { viewModel.setFilterStatus("all") }, label = { Text("Statut: $filterStatus") })
                        }
                    }
                }
            }
            item { SectionHeader("Populaires", "Voir tout") { navController.navigate("library") } }
            item { MangaSection(popularState, viewModel, navController, hasFilters) }
            item { SectionHeader("Derniers ajouts", "Voir tout") { navController.navigate("library") } }
            item { MangaSection(latestState, viewModel, navController, hasFilters) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onAction))
    }
}

@Composable
private fun MangaSection(state: UiState<List<Manga>>, viewModel: MainViewModel, navController: NavController, hasFilters: Boolean) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Text(state.message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
        is UiState.Success -> {
            if (state.data.isEmpty() && hasFilters) Text("Aucun manga ne correspond aux filtres", Modifier.padding(16.dp))
            else MangaRow(state.data, viewModel, navController)
        }
    }
}

@Composable
private fun MangaRow(mangaList: List<Manga>, viewModel: MainViewModel, navController: NavController) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(mangaList) { manga ->
            Column(Modifier.width(120.dp).clickable { viewModel.selectManga(manga); navController.navigate("manga_detail/${manga.id}") }) {
                Card(Modifier.fillMaxWidth().height(170.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    AsyncImage(model = manga.coverUrl, contentDescription = manga.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(4.dp))
                Text(manga.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            }
        }
    }
}
