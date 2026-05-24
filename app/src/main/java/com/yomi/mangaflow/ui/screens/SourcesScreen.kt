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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    navController: NavController,
    viewModel: MainViewModel = koinViewModel()
) {
    val currentSource by viewModel.currentSource.collectAsState()
    val availableSources by viewModel.availableSources.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sources") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text("Choisissez la source active", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                LazyColumn {
                    items(availableSources) { sourceId ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.setCurrentSource(sourceId) }.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sourceId)
                            if (currentSource == sourceId) Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
