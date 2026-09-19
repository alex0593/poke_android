package com.example.poke_android.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.example.poke_android.data.Catalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokeApp(vm: PokeViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route ?: "dex"
    val snackbar = remember { SnackbarHostState() }
    var previousUser by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(s.user) {
        if (previousUser != null && s.user == null)
            nav.navigate("account") {
                popUpTo("dex")
                launchSingleTop = true
            }
        previousUser = s.user
    }
    LaunchedEffect(s.error) {
        s.error?.let {
            snackbar.showSnackbar(it)
            vm.dismissError()
        }
    }
    LaunchedEffect(s.randomPokemon?.key) {
        s.randomPokemon?.let { pokemon ->
            nav.navigate("detail/${Catalog.POKEMON.name}/${Uri.encode(pokemon.key)}")
            vm.consumeRandom()
        }
    }
    fun open(c: Catalog, key: String) {
        nav.navigate("detail/${c.name}/${Uri.encode(key)}")
    }
    MaterialTheme(colorScheme = colors) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Text("POKÉDEX", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        if (
                            route.startsWith("detail") ||
                                route == "quiz" ||
                                route == "ranking" ||
                                route == "regions" ||
                                route.startsWith("world/")
                        )
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                    },
                    actions = {
                        Text(
                            "PRO MAX",
                            color = red,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(16.dp),
                        )
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf(
                            Triple("dex", "Explorar", Icons.Default.Search),
                            Triple("game", "Jugar", Icons.Default.SportsEsports),
                            Triple("favorites", "Favoritos", Icons.Default.Favorite),
                            Triple("account", "Cuenta", Icons.Default.Person),
                        )
                        .forEach { (destination, title, icon) ->
                            NavigationBarItem(
                                selected = route == destination,
                                onClick = {
                                    nav.navigate(destination) {
                                        popUpTo("dex") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(icon, title) },
                                label = { Text(title) },
                            )
                        }
                }
            },
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (!s.ready) {
                    Loading()
                    return@Column
                }
                NavHost(nav, startDestination = "dex", modifier = Modifier.weight(1f)) {
                    composable("dex") { CatalogScreen(s, vm, ::open) }
                    composable("detail/{catalog}/{key}") { entry ->
                        val catalog = Catalog.valueOf(entry.arguments!!.getString("catalog")!!)
                        val key = entry.arguments!!.getString("key")!!
                        LaunchedEffect(catalog, key) { vm.detail(catalog, key) }
                        DetailScreen(
                            s,
                            catalog,
                            { vm.detail(catalog, key) },
                            { e -> vm.favorite(catalog, e) },
                            { pokemon -> open(Catalog.POKEMON, pokemon) },
                        )
                    }
                    composable("favorites") {
                        LaunchedEffect(s.user) { vm.loadFavorites() }
                        FavoritesScreen(s, vm::loadFavorites, ::open)
                    }
                    composable("account") {
                        LaunchedEffect(s.user) { vm.loadProfile() }
                        AccountScreen(s, vm)
                    }
                    composable("game") {
                        GameScreen(
                            s,
                            onStartTrivia = {
                                vm.startGame()
                                nav.navigate("quiz")
                            },
                            onOpenRegions = { nav.navigate("regions") },
                            onOpenRanking = { nav.navigate("ranking") },
                            onOpenWorld = { nav.navigate("world") },
                        )
                    }
                    composable("quiz") { QuizScreen(s, vm) }
                    composable("regions") {
                        LaunchedEffect(s.user) { vm.loadProgress() }
                        RegionsScreen(s, vm::loadProgress) { region, type ->
                            vm.startGame(region, type)
                            nav.navigate("quiz")
                        }
                    }
                    composable("ranking") {
                        LaunchedEffect(Unit) { vm.loadRanking() }
                        RankingScreen(s, vm::loadRanking)
                    }
                    composable("world") {
                        LaunchedEffect(Unit) { vm.loadWorldRegions() }
                        RegionExplorerScreen(s, vm::loadWorldRegions) { name ->
                            vm.loadWorldRegion(name)
                            nav.navigate("world/$name")
                        }
                    }
                    composable("world/{name}") { entry ->
                        val name = entry.arguments!!.getString("name")!!
                        LaunchedEffect(name) { vm.loadWorldRegion(name) }
                        RegionDetailScreen(s, name, vm::loadWorldRegion)
                    }
                }
            }
        }
    }
}
