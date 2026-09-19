package com.example.poke_android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

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
            q.target.sprites["front_default"] ?: q.target.image,
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
