package com.example.poke_android

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.poke_android.data.*
import com.example.poke_android.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cardOpensDetails() {
        var clicked = false
        compose.setContent {
            MaterialTheme {
                EntryCard(Entry("Pikachu", id = 25, types = listOf("Electric"))) { clicked = true }
            }
        }
        compose.onNodeWithText("Pikachu").performClick()
        assertTrue(clicked)
        compose.onNodeWithText("Eléctrico").assertIsDisplayed()
    }

    @Test
    fun detailAllowsFavoriteAndShowsStatistics() {
        var favorite = false
        compose.setContent {
            MaterialTheme {
                DetailScreen(
                    UiState(detail = Entry("Pikachu", id = 25, stats = listOf(Stat("Hp", 35)))),
                    Catalog.POKEMON,
                    {},
                    { favorite = true },
                )
            }
        }
        compose.onNodeWithText("Guardar favorito").performClick()
        assertTrue(favorite)
        compose.onNodeWithText("Hp").assertExists()
    }

    @Test
    fun sessionRoundTripAndLogout() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionStore(context)
        session.clear()
        session.save("ash", "test-token")
        val restored = SessionStore(context)
        restored.restore()
        assertEquals("ash", restored.username)
        assertEquals("test-token", restored.token)
        restored.clear()
        val cleared = SessionStore(context)
        cleared.restore()
        assertNull(cleared.token)
    }
}
