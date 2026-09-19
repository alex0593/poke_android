package com.example.poke_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poke_android.data.*
import java.util.UUID
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PokeViewModel(
    private val repo: Repository,
    private val session: Session,
    unauthorized: ReceiveChannel<Unit>,
) : ViewModel() {
    private val api = repo.api
    private val mutable = MutableStateFlow(UiState())
    val state = mutable.asStateFlow()
    private var listJob: Job? = null
    private val actionMutex = Mutex()
    private val actionJobs = mutableSetOf<Job>()
    private var answerId = UUID.randomUUID().toString()
    private var seen = mutableListOf<String>()
    private var detailRequest = ""

    init {
        viewModelScope.launch {
            // Bucle de recepción sobre el canal confluado: un 401 emitido antes de
            // suscribirse se entrega igualmente al arrancar; la cancelación del scope cierra el bucle.
            while (true) {
                unauthorized.receive()
                logout()
                mutable.update { it.copy(error = "La sesión caducó. Inicia sesión de nuevo.") }
            }
        }
        viewModelScope.launch {
            try {
                session.restore()
                mutable.update { it.copy(user = session.username) }
            } catch (e: Exception) {
                mutable.update { it.copy(error = e.userMessage()) }
            }
            mutable.update { it.copy(ready = true) }
            refresh()
            launch { loadTypes() }
            if (session.token != null) launch { loadInitialFavorites() }
        }
    }

    private suspend fun loadTypes() {
        try {
            val types = api.types()
            mutable.update { it.copy(types = types) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    private suspend fun loadInitialFavorites() {
        try {
            val favs = api.favorites()
            mutable.update { it.copy(favorites = favs) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    private fun action(block: suspend () -> Unit) {
        val job =
            viewModelScope.launch(start = CoroutineStart.LAZY) {
                actionMutex.withLock {
                    mutable.update { it.copy(busy = true, error = null) }
                    try {
                        block()
                    } catch (e: Exception) {
                        mutable.update { it.copy(error = e.userMessage()) }
                    } finally {
                        mutable.update { it.copy(busy = false) }
                    }
                }
            }
        actionJobs.add(job)
        job.invokeOnCompletion { actionJobs.remove(job) }
        job.start()
    }

    fun dismissError() {
        mutable.update { it.copy(error = null) }
    }

    fun selectCatalog(catalog: Catalog) {
        mutable.update { it.copy(catalog = catalog, search = "", selectedTypes = emptyList()) }
        refresh()
    }

    fun search(value: String) {
        mutable.update { it.copy(search = value) }
        if (state.value.catalog == Catalog.POKEMON) refresh(debounce = true)
    }

    fun toggleType(type: String) {
        mutable.update {
            it.copy(
                selectedTypes =
                    if (type in it.selectedTypes) it.selectedTypes - type
                    else it.selectedTypes + type
            )
        }
        refresh()
    }

    fun refresh(debounce: Boolean = false) {
        listJob?.cancel()
        mutable.update {
            it.copy(entries = emptyList(), next = null, listBusy = true, listError = null)
        }
        listJob =
            viewModelScope.launch {
                if (debounce) delay(350)
                loadPage(0)
            }
    }

    fun more() {
        val s = state.value
        if (s.listBusy || s.next == null) return
        listJob = viewModelScope.launch { loadPage(s.next) }
    }

    private suspend fun loadPage(offset: Int) {
        val s = state.value
        mutable.update { it.copy(listBusy = true, listError = null) }
        try {
            val page = repo.page(s.catalog, offset, s.search, s.selectedTypes)
            mutable.update {
                it.copy(
                    entries =
                        (if (offset == 0) page.entries else it.entries + page.entries).distinctBy {
                            e ->
                            e.key
                        },
                    next = page.nextOffset,
                )
            }
        } catch (e: Exception) {
            mutable.update { it.copy(listError = e.userMessage()) }
        } finally {
            if (currentCoroutineContext().isActive) mutable.update { it.copy(listBusy = false) }
        }
    }

    fun detail(catalog: Catalog, name: String) {
        val request = "${catalog.path}/$name"
        detailRequest = request
        mutable.update { it.copy(detail = null, evolution = null) }
        action {
            val e = repo.detail(catalog, name)
            val chain =
                if (catalog == Catalog.POKEMON && e.id != null)
                    try {
                        repo.evolutionChain(e.id)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        null
                    }
                else null
            if (detailRequest == request)
                mutable.update { it.copy(detail = e, evolution = chain) }
        }
    }

    fun random() {
        action {
            val e = api.random()
            mutable.update { it.copy(detail = e, randomPokemon = e) }
        }
    }

    fun consumeRandom() {
        mutable.update { it.copy(randomPokemon = null) }
    }

    fun loadWorldRegions() {
        action {
            val page = api.regions()
            mutable.update { it.copy(worldRegions = page.results, worldRegionsLoaded = true) }
        }
    }

    fun loadWorldRegion(name: String) {
        action {
            val region = api.region(name)
            mutable.update { it.copy(worldRegion = region) }
        }
    }

    fun authenticate(user: String, password: String, register: Boolean) {
        action {
            require(user.isNotBlank() && password.isNotBlank())
            if (register) api.register(Credentials(user.trim(), password))
            val token = api.login(user.trim(), password)
            session.save(user.trim(), token.access_token)
            mutable.update { it.copy(user = user.trim()) }
            val profile = api.profile(user.trim())
            val favorites = api.favorites()
            mutable.update { it.copy(profile = profile, favorites = favorites) }
        }
    }

    fun logout() {
        actionJobs.toList().forEach { it.cancel() }
        viewModelScope.launch {
            session.clear()
            mutable.update {
                it.copy(
                    user = null,
                    favorites = emptyList(),
                    profile = null,
                    progress = emptyList(),
                    avatars = emptyList(),
                    quiz = null,
                    ranking = null,
                    busy = false,
                )
            }
        }
    }

    fun favorite(catalog: Catalog, entry: Entry) {
        action {
            if (state.value.user == null) {
                mutable.update {
                    it.copy(error = "Inicia sesión en Cuenta para guardar favoritos.")
                }
                return@action
            }
            val found =
                state.value.favorites.any {
                    it.entity_type == catalog.entity && it.entity_name == entry.key
                }
            if (found) api.removeFavorite(catalog.entity, entry.key)
            else
                api.addFavorite(
                    Favorite(
                        entity_type = catalog.entity,
                        entity_name = entry.key,
                        entity_id = entry.id,
                    )
                )
            val favorites = api.favorites()
            mutable.update { it.copy(favorites = favorites) }
        }
    }

    fun loadFavorites() {
        if (state.value.user != null)
            action {
                val f = api.favorites()
                mutable.update { it.copy(favorites = f) }
            }
    }

    fun loadProfile() {
        state.value.user?.let { user ->
            action {
                val p = api.profile(user)
                val a = api.avatars(user)
                mutable.update { it.copy(profile = p, avatars = a) }
            }
        }
    }

    fun avatar(url: String) {
        state.value.user?.let { user ->
            action {
                api.avatar(AvatarUpdate(user, url))
                val p = api.profile(user)
                mutable.update { it.copy(profile = p) }
            }
        }
    }

    fun loadRanking() {
        action {
            val r = api.ranking()
            mutable.update { it.copy(ranking = r) }
        }
    }

    fun loadProgress() {
        if (state.value.user != null)
            action {
                val r = api.progress()
                mutable.update { it.copy(progress = r) }
            }
    }

    fun startGame(region: String? = null, type: String? = null) {
        val stage =
            state.value.progress
                .find { it.region_name == region }
                ?.stages
                ?.find { it.type_name == type }
        seen.clear()
        mutable.update {
            it.copy(
                region = region,
                stageType = type,
                score = 0,
                quiz = null,
                answered = null,
                gameMessage = null,
                stageFinished = false,
                stageCorrect = stage?.correct_count ?: 0,
                stageTotal = stage?.total_count ?: 0,
                pendingAnswer = null,
                saveFailed = false,
            )
        }
        nextQuestion()
    }

    fun nextQuestion() {
        action {
            val s = state.value
            val q = api.quiz(s.region, s.stageType, seen.takeLast(50))
            answerId = UUID.randomUUID().toString()
            mutable.update {
                it.copy(
                    quiz = q,
                    questionId = answerId,
                    answered = null,
                    gameMessage = null,
                    pendingAnswer = null,
                    saveFailed = false,
                )
            }
        }
    }

    fun answer(option: String) {
        if (state.value.answered != null) return
        action {
            val s = state.value
            if (s.answered != null) return@action
            val q = s.quiz ?: return@action
            val chosen = s.pendingAnswer ?: option
            val correct = chosen.equals(q.target.key, true) || chosen.equals(q.target.name, true)
            mutable.update { it.copy(pendingAnswer = chosen, saveFailed = false) }
            var message = if (correct) "¡Correcto!" else "Era ${q.target.name}"
            try {
                if (s.user != null) {
                    if (s.region != null && s.stageType != null) {
                        val result =
                            api.stageAnswer(StageAnswer(s.region, s.stageType, correct, answerId))
                        mutable.update {
                            it.copy(
                                stageFinished = result.attempt_finished,
                                stageCorrect =
                                    if (result.attempt_finished) result.attempt_correct_count
                                    else result.stage_progress.correct_count,
                                stageTotal =
                                    if (result.attempt_finished) GameRules.STAGE_QUESTIONS
                                    else result.stage_progress.total_count,
                            )
                        }
                        if (result.attempt_finished)
                            message +=
                                if (result.attempt_passed) " · Etapa superada"
                                else " · Intento finalizado"
                        if (result.region_completed) message += " · ¡Medalla regional!"
                    } else api.result(GameResult(correct, if (correct) s.score + 1 else 0))
                }
            } catch (e: Exception) {
                mutable.update { it.copy(saveFailed = true) }
                throw e
            }
            seen.add(q.target.key)
            mutable.update {
                it.copy(
                    answered = chosen,
                    score = if (correct) it.score + 1 else 0,
                    gameMessage = message,
                    pendingAnswer = null,
                )
            }
        }
    }
}
