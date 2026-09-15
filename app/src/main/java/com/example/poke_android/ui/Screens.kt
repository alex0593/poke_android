package com.example.poke_android.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.poke_android.data.*
import kotlinx.coroutines.delay

private val red = Color(0xFFFF3E3E)
private val colors =
    darkColorScheme(
        primary = red,
        secondary = Color(0xFFFFDE00),
        background = Color(0xFF0A0A0A),
        surface = Color(0xFF141414),
        surfaceVariant = Color(0xFF222222),
    )

fun typeColor(type: String) =
    when (type.lowercase()) {
        "fire" -> Color(0xFFFF9553)
        "water" -> Color(0xFF64A6FF)
        "grass" -> Color(0xFF7DCB73)
        "electric" -> Color(0xFFFFDE55)
        "psychic" -> Color(0xFFF783B0)
        "ice" -> Color(0xFF8DDDD8)
        "poison",
        "ghost" -> Color(0xFFB898E9)
        "dragon" -> Color(0xFF9390FF)
        "fairy" -> Color(0xFFF2ABD9)
        else -> Color(0xFFB6B9BB)
    }

fun typeLabel(type: String) =
    mapOf(
        "fire" to "Fuego",
        "water" to "Agua",
        "grass" to "Planta",
        "electric" to "Eléctrico",
        "psychic" to "Psíquico",
        "ghost" to "Fantasma",
        "dragon" to "Dragón",
        "normal" to "Normal",
        "ice" to "Hielo",
        "dark" to "Siniestro",
        "steel" to "Acero",
        "fighting" to "Lucha",
        "ground" to "Tierra",
        "flying" to "Volador",
        "fairy" to "Hada",
        "bug" to "Bicho",
        "poison" to "Veneno",
        "rock" to "Roca",
    )[type.lowercase()] ?: type

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
                                route == "regions"
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
                        )
                    }
                    composable("favorites") {
                        LaunchedEffect(s.user) { vm.loadFavorites() }
                        LazyColumn(
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item { Heading("Tu colección", "Guarda tus descubrimientos favoritos") }
                            if (s.user == null)
                                item { Text("Inicia sesión en Cuenta para ver tu colección.") }
                            else {
                                item {
                                    OutlinedButton(onClick = vm::loadFavorites, enabled = !s.busy) {
                                        Text("Actualizar")
                                    }
                                }
                                if (s.favorites.isEmpty() && !s.busy)
                                    item { Text("Todavía no tienes favoritos.") }
                                items(s.favorites, key = { it.id }) { f ->
                                    Card(
                                        onClick = {
                                            open(Catalog.forEntity(f.entity_type), f.entity_name)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(Modifier.padding(20.dp)) {
                                            Text(
                                                f.entity_name.replace('-', ' '),
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            Text(
                                                Catalog.forEntity(f.entity_type).title,
                                                color = red,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    composable("account") {
                        LaunchedEffect(s.user) { vm.loadProfile() }
                        AccountScreen(s, vm)
                    }
                    composable("game") {
                        Column(
                            Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Heading("¿Quién es ese Pokémon?", "Pon a prueba lo que sabes")
                            MenuCard("Trivia libre", "Descubre la silueta y elige la respuesta") {
                                vm.startGame()
                                nav.navigate("quiz")
                            }
                            MenuCard(
                                "Aventura por regiones",
                                "Completa etapas por tipo y consigue medallas",
                            ) {
                                nav.navigate("regions")
                            }
                            MenuCard("Ranking", "Conoce a los mejores entrenadores") {
                                nav.navigate("ranking")
                            }
                            if (s.user == null)
                                Text(
                                    "Puedes jugar trivia como invitado. Inicia sesión para guardar tus resultados."
                                )
                        }
                    }
                    composable("quiz") { QuizScreen(s, vm) }
                    composable("regions") {
                        LaunchedEffect(s.user) { vm.loadProgress() }
                        LazyColumn(
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item { Heading("Aventura regional", "Etapas, progreso y medallas") }
                            if (s.user == null)
                                item { Text("Inicia sesión en Cuenta para acceder a la aventura.") }
                            else {
                                item {
                                    OutlinedButton(onClick = vm::loadProgress, enabled = !s.busy) {
                                        Text("Actualizar progreso")
                                    }
                                }
                                items(s.progress) { region ->
                                    Card {
                                        Column(
                                            Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            AsyncImage(
                                                "${com.example.poke_android.BuildConfig.API_URL}static/maps/${if (region.region_name == "unova") "teselia" else region.region_name}.png",
                                                "Mapa de ${region.region_name}",
                                                Modifier.fillMaxWidth().height(150.dp),
                                            )
                                            Text(
                                                region.region_name.uppercase() +
                                                    if (region.badge_earned) "  ★" else "",
                                                style = MaterialTheme.typography.titleLarge,
                                            )
                                            region.stages.forEach { stage ->
                                                OutlinedButton(
                                                    onClick = {
                                                        vm.startGame(
                                                            region.region_name,
                                                            stage.type_name,
                                                        )
                                                        nav.navigate("quiz")
                                                    },
                                                    enabled = !s.busy,
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Text(
                                                        "${typeLabel(stage.type_name)} · ${stage.correct_count}/${stage.total_count}" +
                                                            if (stage.completed) " ✓" else ""
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    composable("ranking") {
                        LaunchedEffect(Unit) { vm.loadRanking() }
                        LazyColumn(
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                Heading(
                                    "Liga de entrenadores",
                                    "${s.ranking?.total_players ?: 0} jugadores",
                                )
                            }
                            item {
                                OutlinedButton(onClick = vm::loadRanking, enabled = !s.busy) {
                                    Text("Actualizar")
                                }
                            }
                            s.ranking?.current_user?.let { rank ->
                                item {
                                    Text(
                                        "Tu posición: #${rank.position} · ${rank.points} puntos",
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                            items(s.ranking?.leaders ?: emptyList()) { rank ->
                                Card(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImage(rank.avatar_url, null, Modifier.size(48.dp))
                                        Column(Modifier.padding(start = 12.dp)) {
                                            Text(
                                                "#${rank.position}  ${rank.username}",
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                "${rank.points} puntos · ${rank.medals} medallas · ${rank.accuracy}%"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Heading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Loading() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MenuCard(title: String, subtitle: String, click: () -> Unit) {
    Card(onClick = click, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CatalogScreen(s: UiState, vm: PokeViewModel, open: (Catalog, String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(Catalog.entries) { c ->
                FilterChip(s.catalog == c, { vm.selectCatalog(c) }, label = { Text(c.title) })
            }
        }
        OutlinedTextField(
            s.search,
            vm::search,
            label = {
                Text(
                    if (s.catalog == Catalog.POKEMON) "Buscar Pokémon por nombre"
                    else "Buscar en los resultados cargados"
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        if (s.catalog == Catalog.POKEMON) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(s.types) { t ->
                    FilterChip(
                        t in s.selectedTypes,
                        { vm.toggleType(t) },
                        label = { Text(typeLabel(t), color = typeColor(t)) },
                    )
                }
            }
        }
        LazyVerticalGrid(
            GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(s.catalog.title, style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { vm.refresh() }) { Text("Actualizar") }
                }
            }
            val visibleEntries =
                if (s.catalog == Catalog.POKEMON) s.entries
                else
                    s.entries.filter {
                        it.name.contains(s.search, true) || it.key.contains(s.search, true)
                    }
            items(visibleEntries, key = { it.key }) { e -> EntryCard(e) { open(s.catalog, e.key) } }
            if (s.listBusy) item(span = { GridItemSpan(maxLineSpan) }) { Loading() }
            if (s.listError != null)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(s.listError)
                        Button(onClick = { if (s.entries.isEmpty()) vm.refresh() else vm.more() }) {
                            Text("Reintentar")
                        }
                    }
                }
            if (!s.listBusy && s.listError == null && visibleEntries.isEmpty())
                item(span = { GridItemSpan(maxLineSpan) }) { Text("No se encontraron resultados.") }
            if (s.next != null && !s.listBusy)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedButton(onClick = vm::more, modifier = Modifier.fillMaxWidth()) {
                        Text("Cargar más")
                    }
                }
        }
    }
}

@Composable
fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                entry.id?.let { "#${it.toString().padStart(3, '0')}" } ?: "—",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            if (entry.image != null)
                AsyncImage(entry.image, entry.name, Modifier.fillMaxWidth().height(120.dp))
            Text(
                entry.name.replace('-', ' '),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                entry.types
                    .joinToString(" · ") { typeLabel(it) }
                    .ifBlank { entry.type?.let { typeLabel(it) } ?: "Ver ficha" },
                color = typeColor(entry.types.firstOrNull() ?: entry.type ?: ""),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun DetailScreen(s: UiState, catalog: Catalog, retry: () -> Unit, favorite: (Entry) -> Unit) {
    val e = s.detail
    if (e == null) {
        if (s.busy) Loading()
        else
            Button(onClick = retry, modifier = Modifier.padding(24.dp)) { Text("Reintentar ficha") }
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(e.id?.let { "N.º $it" } ?: catalog.title, color = red)
        Heading(
            e.name.replace('-', ' '),
            e.types
                .joinToString(" · ") { typeLabel(it) }
                .ifBlank { e.type?.let { typeLabel(it) } ?: catalog.title },
        )
        if (e.image != null) AsyncImage(e.image, e.name, Modifier.fillMaxWidth().height(240.dp))
        val saved = s.favorites.any { it.entity_type == catalog.entity && it.entity_name == e.key }
        FilledTonalButton(onClick = { favorite(e) }, enabled = !s.busy) {
            Icon(if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
            Spacer(Modifier.width(8.dp))
            Text(if (saved) "Quitar de favoritos" else "Guardar favorito")
        }
        if (e.description.isNotBlank()) Text(e.description)
        listOf(
                "Altura" to e.height?.let { "${it / 10.0} m" },
                "Peso" to e.weight?.let { "${it / 10.0} kg" },
                "Experiencia base" to e.base_experience,
                "Potencia" to e.power,
                "Precisión" to e.accuracy,
                "PP" to e.pp,
                "Coste" to e.cost,
                "Categoría" to e.category,
                "Crecimiento (horas)" to e.growth_time,
                "Cosecha máxima" to e.max_harvest,
                "Firmeza" to e.firmness,
            )
            .forEach { (label, value) -> if (value != null) Text("$label: $value") }
        e.stats.forEach { stat ->
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stat.name)
                    Text(stat.base_stat.toString())
                }
                LinearProgressIndicator(
                    progress = { (stat.base_stat / 255f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
        if (e.abilities.isNotEmpty())
            Text("Habilidades: ${e.abilities.joinToString { it.replace('-', ' ') }}")
        if (e.attributes.isNotEmpty()) Text("Atributos: ${e.attributes.joinToString()}")
        if (e.sprites.isNotEmpty())
            LazyRow {
                items(e.sprites.filterValues { it != null }.toList()) { (name, url) ->
                    Column {
                        AsyncImage(url, name, Modifier.size(100.dp))
                        Text(name.replace('_', ' '), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        if (e.pokemon.isNotEmpty()) {
            Text("Pokémon que la aprenden", style = MaterialTheme.typography.titleMedium)
            e.pokemon.forEach { Text(it.name + if (it.is_hidden) " · Oculta" else "") }
        }
    }
}

@Composable
fun AccountScreen(s: UiState, vm: PokeViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var register by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (s.user == null) {
            Heading(
                if (register) "Únete a la aventura" else "Hola, entrenador",
                "Tu colección y progreso en todos tus dispositivos",
            )
            OutlinedTextField(
                username,
                { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                password,
                { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { vm.authenticate(username, password, register) },
                enabled = !s.busy && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (register) "Crear cuenta" else "Iniciar sesión")
            }
            TextButton(onClick = { register = !register }) {
                Text(if (register) "Ya tengo cuenta" else "Crear una cuenta")
            }
        } else {
            LaunchedEffect(s.user) { password = "" }
            Heading(s.user, "Perfil de entrenador")
            s.profile?.let { p ->
                AsyncImage(p.avatar_url, "Avatar", Modifier.size(100.dp))
                p.stats?.let {
                    Text(
                        "${it.correct_answers} aciertos de ${it.total_answers}\nRacha: ${it.streak} · Récord: ${it.high_score}"
                    )
                }
                Text("Logros", style = MaterialTheme.typography.titleLarge)
                if (p.achievements.isEmpty()) Text("Juega para conseguir tus primeras medallas.")
                p.achievements.forEach { Text("${it.icon} ${it.name}\n${it.description}") }
                Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.avatars) { avatar ->
                        Card(onClick = { vm.avatar(avatar.url) }, enabled = !s.busy) {
                            Column(Modifier.padding(8.dp)) {
                                AsyncImage(avatar.url, avatar.name, Modifier.size(72.dp))
                                Text(avatar.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = vm::loadProfile, enabled = !s.busy) {
                Text("Actualizar perfil")
            }
            Button(onClick = vm::logout) { Text("Cerrar sesión") }
        }
    }
}

@Composable
fun QuizScreen(s: UiState, vm: PokeViewModel) {
    var seconds by rememberSaveable(s.questionId) { mutableIntStateOf(10) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(s.quiz, s.answered, s.pendingAnswer, s.busy, lifecycle) {
        if (s.quiz != null && s.answered == null && s.pendingAnswer == null && !s.busy) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (seconds > 0) {
                    delay(1000)
                    seconds--
                }
                vm.answer("timeout")
            }
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Heading(
            "¿Quién es ese Pokémon?",
            s.region?.let { "${it.uppercase()} · ${typeLabel(s.stageType ?: "")}" }
                ?: "Trivia libre · Racha ${s.score}",
        )
        val q = s.quiz
        if (q == null) {
            if (!s.busy) Button(onClick = vm::nextQuestion) { Text("Cargar pregunta") }
            return@Column
        }
        if (s.region != null)
            Text("${s.stageTotal}/10 preguntas · ${s.stageCorrect} aciertos · Meta: 7/10")
        if (s.answered == null)
            Text(
                "${seconds}s",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
            )
        AsyncImage(
            q.target.image,
            if (s.answered == null) "Silueta de Pokémon" else q.target.name,
            Modifier.fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFFE4E8F0), RoundedCornerShape(24.dp)),
            colorFilter = if (s.answered == null) ColorFilter.tint(Color.Black) else null,
        )
        q.options.forEach { option ->
            OutlinedButton(
                onClick = { vm.answer(option) },
                enabled = !s.busy && s.answered == null && s.pendingAnswer == null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(option.replace('-', ' '))
            }
        }
        if (s.saveFailed) {
            Text("No se confirmó el guardado de la respuesta.")
            Button(onClick = { vm.answer(s.pendingAnswer ?: "") }, enabled = !s.busy) {
                Text("Reintentar guardado")
            }
        }
        s.gameMessage?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        if (s.stageFinished)
            Button(onClick = { vm.startGame(s.region, s.stageType) }, enabled = !s.busy) {
                Text("Repetir etapa")
            }
        else if (s.answered != null)
            Button(onClick = vm::nextQuestion, enabled = !s.busy) { Text("Siguiente Pokémon") }
    }
}
