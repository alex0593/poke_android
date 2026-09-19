package com.example.poke_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.poke_android.data.Catalog
import com.example.poke_android.data.Entry
import com.example.poke_android.data.EvolutionNode

@Composable
fun DetailScreen(
    s: UiState,
    catalog: Catalog,
    retry: () -> Unit,
    favorite: (Entry) -> Unit,
    openPokemon: (String) -> Unit = {},
) {
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
        if (catalog == Catalog.POKEMON && s.evolution != null) {
            Text("Cadena evolutiva", style = MaterialTheme.typography.titleMedium)
            EvolutionNodeView(s.evolution.chain, openPokemon)
        }
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
            e.pokemon.forEach {
                TextButton(onClick = { openPokemon(it.name) }) {
                    Text(it.name.replace('-', ' ') + if (it.is_hidden) " · Oculta" else "")
                }
            }
        }
    }
}

@Composable
private fun EvolutionNodeView(node: EvolutionNode, openPokemon: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { openPokemon(node.species.name) }) {
            Text(node.species.name.replace('-', ' '), style = MaterialTheme.typography.titleSmall)
        }
        node.evolves_to.forEach { next ->
            Row(Modifier.padding(start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("↳", color = MaterialTheme.colorScheme.secondary)
                EvolutionNodeView(next, openPokemon)
            }
        }
    }
}
