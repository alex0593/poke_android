package com.example.poke_android.data

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class RepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var api: PokeApi
    private var token: String? = null
    private var unauthorized = 0

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        api = createApi(server.url("/").toString(), { token }, { unauthorized++ }, false)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    private fun respond(json: String, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(json)
                .setHeader("Content-Type", "application/json")
        )
    }

    @Test
    fun pokemonPageUsesRepeatedNamesAndPreservesPartialResults() = runBlocking {
        respond(
            """{"count":40,"next":"/pokemon/?offset=2","results":[{"name":"pikachu","url":"external"},{"name":"eevee","url":"external"}]}"""
        )
        respond(
            """[{"id":25,"name":"Pikachu","original_name":"pikachu","image":null,"extra":true}]"""
        )
        val page = Repository(api).page(Catalog.POKEMON, 0, "pi", listOf("electric", "normal"))
        assertEquals(listOf("pikachu", "eevee"), page.entries.map { it.key })
        assertEquals(2, page.nextOffset)
        val list = server.takeRequest().requestUrl!!
        assertEquals("pi", list.queryParameter("search"))
        assertEquals(listOf("electric", "normal"), list.queryParameterValues("types"))
        assertEquals(
            listOf("pikachu", "eevee"),
            server.takeRequest().requestUrl!!.queryParameterValues("names"),
        )
    }

    @Test
    fun catalogBatchUsesCommaSeparatedNamesAndCachesDetails() = runBlocking {
        respond("""{"count":1,"next":null,"results":[{"name":"potion"}]}""")
        respond(
            """[{"id":17,"name":"Potion","original_name":"potion","cost":null,"category":"healing"}]"""
        )
        val repo = Repository(api)
        val page = repo.page(Catalog.ITEMS, 0, "", emptyList())
        assertNull(page.nextOffset)
        assertEquals("healing", repo.detail(Catalog.ITEMS, "potion").category)
        server.takeRequest()
        assertEquals("/items/batch?names=potion", server.takeRequest().path)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun loginUsesFormEncoding() = runBlocking {
        respond("""{"access_token":"abc","token_type":"bearer"}""")
        assertEquals("abc", api.login("ash k", "p&ss").access_token)
        val request = server.takeRequest()
        assertTrue(
            request.getHeader("Content-Type")!!.startsWith("application/x-www-form-urlencoded")
        )
        assertEquals("username=ash%20k&password=p%26ss", request.body.readUtf8())
    }

    @Test
    fun authorizationReflectsSessionChangesAndHandles401() = runBlocking {
        token = "first"
        respond("[]")
        api.favorites()
        assertEquals("Bearer first", server.takeRequest().getHeader("Authorization"))
        token = "second"
        respond("{}", 401)
        try {
            api.favorites()
            fail("Expected 401")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
        assertEquals("Bearer second", server.takeRequest().getHeader("Authorization"))
        assertEquals(1, unauthorized)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun anonymous401DoesNotInvalidateSession() = runBlocking {
        respond("{}", 401)
        try {
            api.login("ash", "wrong")
            fail("Expected error")
        } catch (_: HttpException) {}
        assertEquals(0, unauthorized)
    }

    @Test
    fun favoriteDeleteAccepts204WithoutJson() = runBlocking {
        respond("", 204)
        api.removeFavorite("pokemon", "mr-mime")
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/users/favorites/pokemon/mr-mime", request.path)
    }

    @Test
    fun failedWriteIsNotAutomaticallyRetried() = runBlocking {
        respond("{}", 503)
        try {
            api.result(GameResult(true, 1))
            fail("Expected error")
        } catch (_: HttpException) {}
        assertEquals(1, server.requestCount)
    }

    @Test
    fun stageAnswerKeepsIdempotencyKeyOnExplicitRetry() = runBlocking {
        val answer = StageAnswer("kanto", "fire", true, "6eddfc02-65ac-4da3-b4e6-07d586cf3f1b")
        repeat(2) {
            respond("{}", 503)
            try {
                api.stageAnswer(answer)
            } catch (_: HttpException) {}
        }
        assertEquals(server.takeRequest().body.readUtf8(), server.takeRequest().body.readUtf8())
    }

    @Test
    fun transformedCatalogAndNullableSpritesDeserialize() {
        val entry =
            apiJson.decodeFromString<Entry>(
                """{"name":"Potion","original_name":"potion","cost":null,"category":"healing","attributes":["usable"],"sprites":{"front_default":null},"unknown":1}"""
            )
        assertNull(entry.cost)
        assertEquals("potion", entry.key)
        assertNull(entry.sprites["front_default"])
    }

    @Test
    fun evolutionChainLoadsNestedSpecies() = runBlocking {
        respond(
            """{"id":1,"chain":{"species":{"name":"bulbasaur","url":"species/1"},"evolves_to":[{"species":{"name":"ivysaur","url":"species/2"},"evolves_to":[]}]}}"""
        )
        val chain = Repository(api).evolutionChain(1)
        assertEquals("bulbasaur", chain.chain.species.name)
        assertEquals("ivysaur", chain.chain.evolves_to.single().species.name)
        assertEquals("/evolutions/chain/1", server.takeRequest().path)
    }
}
