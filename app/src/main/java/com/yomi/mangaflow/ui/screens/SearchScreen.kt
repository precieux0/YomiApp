package com.yomi.mangaflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: MainViewModel = koinViewModel()
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val currentSource by viewModel.currentSource.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(query, currentSource) {
        if (query.isNotBlank()) {
            isLoading = true
            try {
                // Appel direct au repository via viewModel (à implémenter)
                // Pour l'exemple, on utilise une méthode temporaire
                val found = viewModel.searchManga(currentSource, query)
                results = found ?: emptyList()
            } catch (e: Exception) {
                results = emptyList()
            } finally {
                isLoading = false
            }
        } else {
            results = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Rechercher un manga...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!navController.popBackStack()) navController.navigate("home")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                query.isBlank() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp))
                        Text("Tapez votre recherche", style = MaterialTheme.typography.titleMedium)
                    }
                }
                results.isEmpty() -> {
                    Text("Aucun résultat", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(results) { manga ->
                            Column(modifier = Modifier.clickable {
                                viewModel.selectManga(manga)
                                navController.navigate("manga_detail/${manga.id}")
                            }) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().height(170.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    AsyncImage(
                                        model = manga.coverUrl,
                                        contentDescription = manga.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(manga.title, style = MaterialTheme.typography.labelMedium,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
