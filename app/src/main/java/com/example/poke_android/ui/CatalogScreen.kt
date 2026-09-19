package com.example.poke_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poke_android.data.Catalog

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
                    Row {
                        if (s.catalog == Catalog.POKEMON)
                            TextButton(onClick = vm::random, enabled = !s.busy) { Text("Al azar") }
                        TextButton(onClick = { vm.refresh() }) { Text("Actualizar") }
                    }
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
