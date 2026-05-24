package com.yomi.mangaflow.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yomi.mangaflow.R
import com.yomi.mangaflow.ui.screens.AboutScreen
import com.yomi.mangaflow.ui.screens.DownloadsScreen
import com.yomi.mangaflow.ui.screens.FilterScreen
import com.yomi.mangaflow.ui.screens.HistoryScreen
import com.yomi.mangaflow.ui.screens.HomeScreen
import com.yomi.mangaflow.ui.screens.LibraryScreen
import com.yomi.mangaflow.ui.screens.MangaDetailScreen
import com.yomi.mangaflow.ui.screens.ReaderScreen
import com.yomi.mangaflow.ui.screens.SearchScreen
import com.yomi.mangaflow.ui.screens.SettingsScreen
import com.yomi.mangaflow.ui.screens.SourcesScreen
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem("home", "Découvrir", Icons.Filled.Home)
    data object Library : BottomNavItem("library", "Bibliothèque", Icons.Filled.Bookmark)
    data object History : BottomNavItem("history", "Historique", Icons.Filled.History)
    data object Downloads : BottomNavItem("downloads", "Téléchargements", Icons.Filled.Download)
    data object Settings : BottomNavItem("settings", "Paramètres", Icons.Filled.Settings)
}

sealed class DrawerItem(val route: String, val title: String, val icon: ImageVector) {
    data object Sources : DrawerItem("sources", "Sources", Icons.AutoMirrored.Filled.MenuBook)
    data object Filters : DrawerItem("filters", "Filtres", Icons.Filled.FilterList)
    data object About : DrawerItem("about", "À propos", Icons.Filled.Info)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        BottomNavItem.Home, BottomNavItem.Library, BottomNavItem.History,
        BottomNavItem.Downloads, BottomNavItem.Settings
    )
    val drawerItems = listOf(DrawerItem.Sources, DrawerItem.Filters, DrawerItem.About)

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Yomi Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Yomi", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Manga Flow par Okitakoy Corp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        gesturesEnabled = showBottomBar
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(navController = navController, onOpenDrawer = { scope.launch { drawerState.open() } })
                }
                composable("library") { LibraryScreen(navController = navController) }
                composable("history") { HistoryScreen(navController = navController) }
                composable("downloads") { DownloadsScreen(navController = navController) }
                composable("settings") { SettingsScreen(navController = navController) }
                composable("sources") { SourcesScreen(navController = navController) }
                composable("filters") { FilterScreen(navController = navController) }
                composable("about") { AboutScreen(navController = navController) }
                composable("search") { SearchScreen(navController = navController) }
                composable("manga_detail/{mangaId}") { backStackEntry ->
                    val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                    MangaDetailScreen(navController = navController, mangaId = mangaId)
                }
                composable("reader/{mangaId}/{chapterId}") { backStackEntry ->
                    val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                    val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                    ReaderScreen(navController = navController, mangaId = mangaId, chapterId = chapterId)
                }
            }
        }
    }
}
