package com.lrewards.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Profile(val id: String, val email: String, val display_name: String, val coins: Int, val created_at: String? = null)

@Serializable
data class Transaction(val id: String, val user_id: String, val type: String, val amount: Int, val created_at: String)

class RewardsRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun signIn(email: String, password: String) = supabase.auth.signInWith(Email) {
        this.email = email
        this.password = password
    }

    suspend fun signUp(email: String, password: String, displayName: String) = supabase.auth.signUpWith(Email) {
        this.email = email
        this.password = password
        data = buildJsonObject { put("display_name", displayName) }
    }

    suspend fun signOut() = supabase.auth.signOut()

    suspend fun currentProfile(): Profile? {
        val id = supabase.auth.currentUserOrNull()?.id ?: return null
        return supabase.from("profiles").select { filter { eq("id", id) } }.decodeSingleOrNull<Profile>()
    }

    suspend fun claimReward(gameType: String, amount: Int): JsonObject = supabase.rpc("claim_reward", buildJsonObject {
        put("p_game_type", gameType)
        put("p_amount", amount)
    }).decodeAs()

    suspend fun transactions(userId: String): List<Transaction> = supabase.from("transactions").select {
        filter { eq("user_id", userId) }
    }.decodeList()
}
