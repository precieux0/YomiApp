package com.yomi.mangaflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.yomi.mangaflow.data.model.ReadingMode
import com.yomi.mangaflow.ui.viewmodel.MainViewModel
import com.yomi.mangaflow.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    mangaId: String,
    chapterId: String,
    viewModel: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    val readerMode by viewModel.readerMode.collectAsStateWithLifecycle()
    val mangaDetailState by viewModel.mangaDetail.collectAsStateWithLifecycle()
    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()

    var pagesState by remember { mutableStateOf<UiState<List<String>>>(UiState.Loading) }
    var currentPageIndex by remember { mutableStateOf(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPageIndex)

    LaunchedEffect(chapterId, mangaId) {
        pagesState = UiState.Loading
        try {
            val chapter = (mangaDetailState as? UiState.Success)?.data?.chapters?.find { it.id == chapterId }
            val savedPage = chapter?.lastPageRead ?: viewModel.getProgress(mangaId, chapterId)
            val pages = viewModel.getChapterPages(mangaId, currentChapter ?: return@LaunchedEffect)
            pagesState = UiState.Success(pages)
            currentPageIndex = savedPage.coerceIn(0, pages.size - 1)
            scope.launch {
                listState.scrollToItem(currentPageIndex)
            }
        } catch (e: Exception) {
            pagesState = UiState.Error(e.message ?: "Erreur de chargement")
        }
    }

    LaunchedEffect(currentPageIndex, chapterId) {
        delay(1000)
        viewModel.saveProgress(mangaId, chapterId, currentPageIndex)
    }

    val detail = (mangaDetailState as? UiState.Success)?.data
    val chapters = detail?.chapters ?: emptyList()
    val currentIndex = chapters.indexOfFirst { it.id == chapterId }
    val hasNext = currentIndex > 0
    val hasPrev = currentIndex >= 0 && currentIndex < chapters.size - 1

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (showControls) {
                    TopAppBar(
                        title = { Text(currentChapter?.title ?: "Chapitre") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.85f),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                if (showControls) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (hasPrev) {
                                    val prev = chapters[currentIndex + 1]
                                    viewModel.selectChapter(prev)
                                    navController.navigate("reader/$mangaId/${prev.id}") {
                                        popUpTo("reader/$mangaId/$chapterId") { inclusive = true }
                                    }
                                }
                            }, enabled = hasPrev) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Précédent", tint = if (hasPrev) Color.White else Color.Gray)
                            }
                            Text("${chapters.size - currentIndex} / ${chapters.size}", color = Color.White)
                            IconButton(onClick = {
                                if (hasNext) {
                                    val next = chapters[currentIndex - 1]
                                    viewModel.selectChapter(next)
                                    navController.navigate("reader/$mangaId/${next.id}") {
                                        popUpTo("reader/$mangaId/$chapterId") { inclusive = true }
                                    }
                                }
                            }, enabled = hasNext) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, "Suivant", tint = if (hasNext) Color.White else Color.Gray)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            when (pagesState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Erreur : ${(pagesState as UiState.Error).message}", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { /* recharger */ }) { Text("Réessayer") }
                        }
                    }
                }
                is UiState.Success -> {
                    val pages = (pagesState as UiState.Success).data
                    ReaderContent(
                        pages = pages,
                        readingMode = readerMode,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .pointerInput(Unit) { detectTapGestures(onTap = { showControls = !showControls }) },
                        onPageChange = { index -> currentPageIndex = index },
                        listState = listState
                    )
                }
            }
        }

        if (showSettings) {
            ReaderSettingsSheet(
                currentMode = readerMode,
                onModeChange = { viewModel.setReaderMode(it) },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun ReaderContent(
    pages: List<String>,
    readingMode: ReadingMode,
    modifier: Modifier,
    onPageChange: (Int) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    when (readingMode) {
        ReadingMode.WEBTOON -> {
            LazyColumn(state = listState, modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items(pages.size, key = { pages[it] }) { index ->
                    ZoomableImage(pages[index], Modifier.fillMaxWidth())
                    LaunchedEffect(listState.firstVisibleItemIndex) { onPageChange(listState.firstVisibleItemIndex) }
                }
            }
        }
        ReadingMode.DUAL_PAGE -> {
            LazyColumn(state = listState, modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pages.chunked(2)) { pair ->
                    Row(Modifier.fillMaxWidth()) {
                        pair.forEach { page ->
                            ZoomableImage(page, Modifier.weight(1f).padding(horizontal = 4.dp))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                LaunchedEffect(listState.firstVisibleItemIndex) { onPageChange(listState.firstVisibleItemIndex * 2) }
            }
        }
        else -> {
            LazyColumn(state = listState, modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pages.size, key = { pages[it] }) { index ->
                    ZoomableImage(pages[index], Modifier.fillMaxWidth())
                    LaunchedEffect(listState.firstVisibleItemIndex) { onPageChange(listState.firstVisibleItemIndex) }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String, modifier: Modifier = Modifier) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var centroid by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = modifier) {
        val imageModifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        scale = 2f
                    }
                })
            }
            .pointerInput(Unit) {
                var previousScale = 1f
                var previousCentroid = Offset.Zero

                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes

                        when (pointers.size) {
                            2 -> {
                                val p1 = pointers[0].position
                                val p2 = pointers[1].position
                                val newCentroid = Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                                val distance = hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()).toFloat()
                                val previousDistance = hypot(
                                    (pointers[0].previousPosition.x - pointers[1].previousPosition.x).toDouble(),
                                    (pointers[0].previousPosition.y - pointers[1].previousPosition.y).toDouble()
                                ).toFloat()
                                val delta = distance / previousDistance

                                if (centroid == Offset.Zero) {
                                    centroid = newCentroid
                                    previousScale = scale
                                }
                                scale = (previousScale * delta).coerceIn(0.5f, 3f)

                                if (scale != previousScale) {
                                    val dx = (centroid.x - size.width / 2) * (1 - delta)
                                    val dy = (centroid.y - size.height / 2) * (1 - delta)
                                    offsetX += dx
                                    offsetY += dy
                                }
                                centroid = newCentroid
                            }
                            1 -> {
                                if (pointers[0].pressed) {
                                    val delta = pointers[0].position - pointers[0].previousPosition
                                    offsetX += delta.x
                                    offsetY += delta.y
                                } else {
                                    centroid = Offset.Zero
                                }
                            }
                        }
                    }
                }
            }

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ReaderSettingsSheet(
    currentMode: ReadingMode,
    onModeChange: (ReadingMode) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Paramètres du lecteur", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                Text("Mode de lecture", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReaderModeChip("Standard", currentMode == ReadingMode.STANDARD) { onModeChange(ReadingMode.STANDARD) }
                    ReaderModeChip("Webtoon", currentMode == ReadingMode.WEBTOON) { onModeChange(ReadingMode.WEBTOON) }
                    ReaderModeChip("Double page", currentMode == ReadingMode.DUAL_PAGE) { onModeChange(ReadingMode.DUAL_PAGE) }
                }
            }
        }
    }
}

@Composable
private fun ReaderModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}
