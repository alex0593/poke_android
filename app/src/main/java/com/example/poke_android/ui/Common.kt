package com.example.poke_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.poke_android.data.Entry

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

private val TYPE_LABELS_ES =
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
    )

fun typeLabel(type: String) = TYPE_LABELS_ES[type.lowercase()] ?: type

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
fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                entry.id?.let { "#${it.toString().padStart(3, '0')}" } ?: "—",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            val thumbnail = entry.sprites["front_default"] ?: entry.image
            if (thumbnail != null)
                AsyncImage(
                    model = thumbnail,
                    contentDescription = entry.name,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )
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
