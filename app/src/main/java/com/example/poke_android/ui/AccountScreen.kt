package com.example.poke_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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
