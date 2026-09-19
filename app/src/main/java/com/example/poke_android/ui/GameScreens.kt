package com.example.poke_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.poke_android.data.Catalog
import com.example.poke_android.BuildConfig

@Composable
fun FavoritesScreen(s: UiState, refresh: () -> Unit, open: (Catalog, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Heading("Tu colección", "Guarda tus descubrimientos favoritos") }
        if (s.user == null)
            item { Text("Inicia sesión en Cuenta para ver tu colección.") }
        else {
            item {
                OutlinedButton(onClick = refresh, enabled = !s.busy) { Text("Actualizar") }
            }
            if (s.favorites.isEmpty() && !s.busy)
                item { Text("Todavía no tienes favoritos.") }
            items(s.favorites, key = { it.id }) { f ->
                val catalog = Catalog.forEntityOrNull(f.entity_type)
                if (catalog != null)
                    Card(
                        onClick = { open(catalog, f.entity_name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                f.entity_name.replace('-', ' '),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(catalog.title, color = red)
                        }
                    }
            }
        }
    }
}

@Composable
fun GameScreen(
    s: UiState,
    onStartTrivia: () -> Unit,
    onOpenRegions: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenWorld: () -> Unit,
) {
    Column(
        Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Heading("¿Quién es ese Pokémon?", "Pon a prueba lo que sabes")
        MenuCard("Trivia libre", "Descubre la silueta y elige la respuesta", onStartTrivia)
        MenuCard(
            "Aventura por regiones",
            "Completa etapas por tipo y consigue medallas",
            click = onOpenRegions,
        )
        MenuCard("Ranking", "Conoce a los mejores entrenadores", onOpenRanking)
        MenuCard("Explorar regiones", "Consulta localidades y zonas del mundo Pokémon", onOpenWorld)
        if (s.user == null)
            Text(
                "Puedes jugar trivia como invitado. Inicia sesión para guardar tus resultados."
            )
    }
}

@Composable
fun RegionsScreen(s: UiState, refresh: () -> Unit, onPlayStage: (String, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Heading("Aventura regional", "Etapas, progreso y medallas") }
        if (s.user == null)
            item { Text("Inicia sesión en Cuenta para acceder a la aventura.") }
        else {
            item {
                OutlinedButton(onClick = refresh, enabled = !s.busy) {
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
                            "${BuildConfig.API_URL}static/maps/${if (region.region_name == "unova") "teselia" else region.region_name}.png",
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
                                onClick = { onPlayStage(region.region_name, stage.type_name) },
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

@Composable
fun RankingScreen(s: UiState, refresh: () -> Unit) {
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
            OutlinedButton(onClick = refresh, enabled = !s.busy) { Text("Actualizar") }
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
