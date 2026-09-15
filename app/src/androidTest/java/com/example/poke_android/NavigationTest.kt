package com.example.poke_android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun guestCanNavigateToAccountFavoritesAndGame() {
        compose.waitUntil(10_000) {
            compose
                .onAllNodesWithText("Buscar Pokémon por nombre")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Cuenta").performClick()
        compose.onNodeWithText("Hola, entrenador").assertIsDisplayed()
        compose.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        compose.onNodeWithText("Crear una cuenta").performClick()
        compose.onNodeWithText("Únete a la aventura").assertIsDisplayed()
        compose.onNodeWithText("Favoritos").performClick()
        compose.onNodeWithText("Inicia sesión en Cuenta para ver tu colección.").assertIsDisplayed()
        compose.onNodeWithText("Jugar").performClick()
        compose.onNodeWithText("Trivia libre").assertIsDisplayed()
        compose.onNodeWithText("Aventura por regiones").assertIsDisplayed()
        compose.onNodeWithText("Explorar").performClick()
        compose.onNodeWithText("Buscar Pokémon por nombre").assertIsDisplayed()
    }
}
