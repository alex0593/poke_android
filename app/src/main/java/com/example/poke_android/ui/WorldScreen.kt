package com.example.poke_android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegionExplorerScreen(s: UiState, refresh: () -> Unit, open: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Heading("Regiones Pokémon", "Explora el mapa y sus localidades") }
        item { OutlinedButton(onClick = refresh, enabled = !s.busy) { Text("Actualizar") } }
        if (s.busy && s.worldRegions.isEmpty())
            item { Loading() }
        else if (!s.worldRegionsLoaded)
            item { Text("No se pudieron cargar las regiones. Vuelve a intentar.") }
        else if (s.worldRegions.isEmpty())
            item { Text("No hay regiones disponibles.") }
        items(s.worldRegions, key = { it.key }) { region ->
            Card(onClick = { open(region.key) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(region.name.replace('-', ' '), style = MaterialTheme.typography.titleLarge)
                    Text("Ver localidades", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun RegionDetailScreen(s: UiState, name: String, refresh: (String) -> Unit) {
    val region = s.worldRegion
    if (region == null || !region.name.equals(name, ignoreCase = true)) {
        if (s.busy) Loading()
        else OutlinedButton(onClick = { refresh(name) }, modifier = Modifier.padding(20.dp)) {
            Text("Reintentar")
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Heading(
                region.name.replace('-', ' ').replaceFirstChar { it.uppercase() },
                "${region.locations.size} localidades disponibles",
            )
        }
        item { OutlinedButton(onClick = { refresh(name) }, enabled = !s.busy) { Text("Actualizar") } }
        items(region.locations, key = { it.name }) { location ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(location.name.replace('-', ' '), modifier = Modifier.padding(16.dp))
            }
        }
    }
}
