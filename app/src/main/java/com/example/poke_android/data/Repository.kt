package com.example.poke_android.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val apiJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun createApi(
    url: String,
    token: () -> String?,
    unauthorized: () -> Unit,
    debug: Boolean,
): PokeApi {
    val client =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                val sentToken = token()
                if (sentToken != null) request.header("Authorization", "Bearer $sentToken")
                chain.proceed(request.build()).also {
                    if (it.code == 401 && sentToken != null && sentToken == token()) unauthorized()
                }
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    redactHeader("Authorization")
                    level =
                        if (debug) HttpLoggingInterceptor.Level.BASIC
                        else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    return Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(apiJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PokeApi::class.java)
}

data class CatalogPage(val entries: List<Entry>, val nextOffset: Int?)

class Repository(val api: PokeApi) {
    private val cache =
        object : LinkedHashMap<String, Entry>(128, .75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?) =
                size > 300
        }

    suspend fun page(
        catalog: Catalog,
        offset: Int,
        search: String,
        types: List<String>,
    ): CatalogPage {
        val page =
            api.list(catalog.path, offset = offset, search = search.ifBlank { null }, types = types)
        val names = page.results.map { it.key }
        val details =
            if (names.isEmpty()) emptyList()
            else if (catalog == Catalog.POKEMON) api.pokemonBatch(names)
            else api.batch(catalog.path, names.joinToString(","))
        details.forEach { cache["${catalog.path}/${it.key}"] = it }
        val byName = details.associateBy { it.key }
        return CatalogPage(
            page.results.map { byName[it.key] ?: it },
            if (page.next != null) offset + page.results.size else null,
        )
    }

    suspend fun detail(catalog: Catalog, name: String): Entry =
        cache["${catalog.path}/$name"]
            ?: api.detail(catalog.path, name).also { cache["${catalog.path}/$name"] = it }

    suspend fun evolutionChain(id: Int): EvolutionChain = api.evolutionChain(id)
}

fun Throwable.userMessage(): String =
    when (this) {
        is CancellationException -> throw this
        is HttpException ->
            when (code()) {
                401 -> "La sesión caducó. Inicia sesión de nuevo."
                400 -> "Revisa los datos; el nombre puede estar registrado."
                404 -> "No se encontró el contenido."
                429 -> "Demasiadas solicitudes. Intenta más tarde."
                else -> "El servidor no pudo completar la solicitud (${code()})."
            }
        is java.io.IOException -> "No se pudo conectar. Revisa tu conexión y vuelve a intentar."
        else -> "No se pudo cargar la información. Vuelve a intentar."
    }
