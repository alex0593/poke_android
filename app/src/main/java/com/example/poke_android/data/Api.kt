package com.example.poke_android.data

import kotlinx.serialization.json.JsonObject
import retrofit2.http.*

interface PokeApi {
    @GET("{catalog}/")
    suspend fun list(
        @Path("catalog") catalog: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null,
        @Query("types") types: List<String> = emptyList(),
    ): Page

    @GET("pokemon/batch/details")
    suspend fun pokemonBatch(@Query("names") names: List<String>): List<Entry>

    @GET("{catalog}/batch")
    suspend fun batch(@Path("catalog") catalog: String, @Query("names") names: String): List<Entry>

    @GET("{catalog}/{name}")
    suspend fun detail(@Path("catalog") catalog: String, @Path("name") name: String): Entry

    @GET("types/") suspend fun types(): List<String>

    @GET("pokemon/random") suspend fun random(): Entry

    @POST("users/register") suspend fun register(@Body credentials: Credentials): Profile

    @FormUrlEncoded
    @POST("users/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Token

    @GET("users/profile") suspend fun profile(@Query("username") username: String): Profile

    @GET("users/avatars/{username}")
    suspend fun avatars(@Path("username") username: String): List<Avatar>

    @PATCH("users/profile/avatar") suspend fun avatar(@Body value: AvatarUpdate): JsonObject

    @GET("users/favorites/") suspend fun favorites(): List<Favorite>

    @POST("users/favorites/") suspend fun addFavorite(@Body favorite: Favorite): Favorite

    @DELETE("users/favorites/{type}/{name}")
    suspend fun removeFavorite(@Path("type") type: String, @Path("name") name: String): Unit

    @GET("pokemon/game/quiz")
    suspend fun quiz(
        @Query("region") region: String? = null,
        @Query("type") type: String? = null,
        @Query("exclude") exclude: List<String> = emptyList(),
    ): Quiz

    @POST("game/save-result") suspend fun result(@Body result: GameResult): JsonObject

    @POST("game/stage/answer") suspend fun stageAnswer(@Body answer: StageAnswer): StageResult

    @GET("game/regions/progress") suspend fun progress(): List<Region>

    @GET("game/ranking") suspend fun ranking(): Ranking
}
