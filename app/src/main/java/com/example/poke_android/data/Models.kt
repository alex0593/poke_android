package com.example.poke_android.data

import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val count: Int = 0,
    val next: String? = null,
    val results: List<Entry> = emptyList(),
)

@Serializable data class Stat(val name: String, val base_stat: Int)

@Serializable data class RelatedPokemon(val name: String, val is_hidden: Boolean = false)

@Serializable
data class Entry(
    val name: String,
    val id: Int? = null,
    val original_name: String? = null,
    val image: String? = null,
    val description: String = "",
    val types: List<String> = emptyList(),
    val stats: List<Stat> = emptyList(),
    val abilities: List<String> = emptyList(),
    val sprites: Map<String, String?> = emptyMap(),
    val height: Int? = null,
    val weight: Int? = null,
    val base_experience: Int? = null,
    val power: Int? = null,
    val accuracy: Int? = null,
    val pp: Int? = null,
    val type: String? = null,
    val cost: Int? = null,
    val category: String? = null,
    val attributes: List<String> = emptyList(),
    val growth_time: Int? = null,
    val max_harvest: Int? = null,
    val firmness: String? = null,
    val pokemon: List<RelatedPokemon> = emptyList(),
) {
    val key: String
        get() = original_name ?: name.lowercase()
}

@Serializable data class Credentials(val username: String, val password: String)

@Serializable data class Token(val access_token: String, val token_type: String)

@Serializable
data class UserStats(
    val total_answers: Int,
    val correct_answers: Int,
    val high_score: Int,
    val streak: Int,
)

@Serializable data class Achievement(val name: String, val description: String, val icon: String)

@Serializable
data class Profile(
    val id: Int,
    val username: String,
    val avatar_url: String? = null,
    val stats: UserStats? = null,
    val achievements: List<Achievement> = emptyList(),
)

@Serializable
data class Favorite(
    val id: Int = 0,
    val entity_type: String,
    val entity_name: String,
    val entity_id: Int? = null,
)

@Serializable data class Quiz(val target: Entry, val options: List<String>)

@Serializable data class GameResult(val correct: Boolean, val score: Int)

@Serializable
data class StageAnswer(
    val region: String,
    val type_name: String,
    val is_correct: Boolean,
    val answer_id: String,
)

@Serializable
data class Stage(
    val region_name: String,
    val type_name: String,
    val correct_count: Int,
    val total_count: Int,
    val attempts: Int,
    val completed: Boolean,
)

@Serializable
data class Region(val region_name: String, val badge_earned: Boolean, val stages: List<Stage>)

@Serializable
data class StageResult(
    val stage_progress: Stage,
    val attempt_finished: Boolean,
    val attempt_passed: Boolean,
    val attempt_correct_count: Int,
    val region_completed: Boolean,
    val new_achievements: List<String>,
)

@Serializable
data class Rank(
    val position: Int,
    val username: String,
    val avatar_url: String? = null,
    val points: Int,
    val medals: Int,
    val accuracy: Double,
    val attempts: Int,
)

@Serializable
data class Ranking(val leaders: List<Rank>, val current_user: Rank? = null, val total_players: Int)

@Serializable data class Avatar(val id: String, val url: String, val name: String)

@Serializable data class AvatarUpdate(val username: String, val avatar_url: String)

@Serializable data class EvolutionSpecies(val name: String, val url: String = "")

@Serializable
data class EvolutionNode(
    val species: EvolutionSpecies,
    val evolves_to: List<EvolutionNode> = emptyList(),
)

@Serializable
data class EvolutionChain(val id: Int = 0, val chain: EvolutionNode)

@Serializable data class NamedResource(val name: String, val url: String = "")

@Serializable
data class RegionDetails(
    val id: Int = 0,
    val name: String,
    val locations: List<NamedResource> = emptyList(),
    val pokemon_species: List<NamedResource> = emptyList(),
)

enum class Catalog(val path: String, val entity: String, val title: String) {
    POKEMON("pokemon", "pokemon", "Pokédex"),
    MOVES("moves", "move", "Movimientos"),
    ABILITIES("abilities", "ability", "Habilidades"),
    ITEMS("items", "item", "Objetos"),
    BERRIES("berries", "berry", "Bayas");

    companion object {
        fun forEntity(entity: String) = entries.first { it.entity == entity }
    }
}
