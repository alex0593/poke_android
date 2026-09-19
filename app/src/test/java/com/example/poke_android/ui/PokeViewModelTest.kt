package com.example.poke_android.ui

import com.example.poke_android.data.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val events = Channel<Unit>(Channel.CONFLATED)
    private val searches = mutableListOf<String?>()
    private val answers = mutableListOf<StageAnswer>()
    private var failAnswer = false
    private val session =
        object : Session {
            override var token: String? = null
            override var username: String? = null

            override suspend fun restore() {}

            override suspend fun save(user: String, accessToken: String) {
                username = user
                token = accessToken
            }

            override suspend fun clear() {
                username = null
                token = null
            }
        }
    private val api =
        Proxy.newProxyInstance(PokeApi::class.java.classLoader, arrayOf(PokeApi::class.java)) {
            _,
            method,
            args ->
            when (method.name) {
                "types" -> listOf("electric")
                "list" -> {
                    searches.add(args[3] as String?)
                    Page()
                }
                "favorites" -> emptyList<Favorite>()
                "login" -> Token("token", "bearer")
                "profile" -> Profile(1, "ash")
                "quiz" ->
                    Quiz(Entry("Pikachu", original_name = "pikachu"), listOf("Pikachu", "Eevee"))
                "stageAnswer" -> {
                    answers.add(args[0] as StageAnswer)
                    if (failAnswer) throw java.io.IOException("offline")
                    StageResult(
                        Stage("kanto", "electric", 0, 0, 1, true),
                        true,
                        true,
                        8,
                        false,
                        emptyList(),
                    )
                }
                "result" -> kotlinx.serialization.json.JsonObject(emptyMap())
                else -> error("Unexpected API method ${method.name}")
            }
        } as PokeApi

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun model() = PokeViewModel(Repository(api), session, events)

    @Test
    fun searchDebouncesAndCancelsOldQuery() =
        runTest(dispatcher) {
            val vm = model()
            advanceUntilIdle()
            searches.clear()
            vm.search("pi")
            advanceTimeBy(100)
            vm.search("pika")
            advanceUntilIdle()
            assertEquals(listOf("pika"), searches)
            assertFalse(vm.state.value.listBusy)
        }

    @Test
    fun loginAndUnauthorizedClearPrivateState() =
        runTest(dispatcher) {
            val vm = model()
            advanceUntilIdle()
            vm.authenticate("ash", "password", false)
            advanceUntilIdle()
            assertEquals("ash", vm.state.value.user)
            assertNotNull(vm.state.value.profile)
            events.trySend(Unit)
            advanceUntilIdle()
            assertNull(vm.state.value.user)
            assertNull(vm.state.value.profile)
            assertNull(session.token)
        }

    @Test
    fun failedStageSaveLocksAnswerAndReusesUuid() =
        runTest(dispatcher) {
            session.save("ash", "token")
            val vm = model()
            advanceUntilIdle()
            vm.startGame("kanto", "electric")
            advanceUntilIdle()
            failAnswer = true
            vm.answer("Pikachu")
            advanceUntilIdle()
            assertTrue(vm.state.value.saveFailed)
            assertEquals("Pikachu", vm.state.value.pendingAnswer)
            assertNull(vm.state.value.answered)
            failAnswer = false
            vm.answer("Eevee")
            advanceUntilIdle()
            assertEquals(answers[0], answers[1])
            assertTrue(answers[1].is_correct)
            assertTrue(vm.state.value.stageFinished)
            assertEquals(8, vm.state.value.stageCorrect)
            assertEquals("Pikachu", vm.state.value.answered)
        }

    @Test
    fun unauthorizedEmittedBeforeSubscriptionStillLogsOut() =
        runTest(dispatcher) {
            session.save("ash", "token")
            // El 401 llega antes de que exista el ViewModel: el canal confluado lo conserva.
            assertTrue(events.trySend(Unit).isSuccess)
            val vm = model()
            advanceUntilIdle()
            assertNull(vm.state.value.user)
            assertNull(session.token)
            assertEquals("La sesión caducó. Inicia sesión de nuevo.", vm.state.value.error)
        }

    @Test
    fun guestTriviaDoesNotWriteResults() =
        runTest(dispatcher) {
            val vm = model()
            advanceUntilIdle()
            vm.startGame()
            advanceUntilIdle()
            vm.answer("Pikachu")
            advanceUntilIdle()
            assertEquals(1, vm.state.value.score)
            assertTrue(answers.isEmpty())
        }
}
