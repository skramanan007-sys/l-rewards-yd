package com.lrewards.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class RewardsRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val control = supabase.postgrest.from("user_controls").select {
            filter { eq("user_id", supabase.auth.currentUserOrNull()?.id.orEmpty()) }
        }.decodeList<JsonObject>().firstOrNull()
        if (control?.get("banned")?.toString()?.trim('"') == "true") {
            supabase.auth.signOut()
            throw IllegalStateException("This account is banned")
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("display_name", JsonPrimitive(displayName)) }
        }
    }

    suspend fun signOut() = supabase.auth.signOut()

    fun currentEmail(): String? = supabase.auth.currentUserOrNull()?.email

    fun hasSession(): Boolean = supabase.auth.currentSessionOrNull() != null

    suspend fun playGame(gameType: String, proof: String? = null): JsonObject =
        supabase.postgrest.rpc(
            "play_reward_game",
            parameters = buildJsonObject {
                put("p_game_type", JsonPrimitive(gameType))
                proof?.let { put("p_proof", JsonPrimitive(it)) }
            },
        ).decodeAs()

    suspend fun requestRedemption(rewardType: String, cost: Int, destination: String): JsonObject =
        supabase.postgrest.rpc(
            "redeem_reward",
            parameters = buildJsonObject {
                put("p_reward", JsonPrimitive(rewardType))
                put("p_cost", JsonPrimitive(cost))
                put("p_destination", JsonPrimitive(destination))
            },
        ).decodeAs()

    suspend fun wallet(): JsonObject =
        supabase.postgrest.rpc("get_my_wallet").decodeAs()

    suspend fun rewardTransactions(): List<JsonObject> =
        supabase.postgrest.from("transactions").select { order("created_at", Order.DESCENDING) }.decodeList<JsonObject>()

    suspend fun balanceAdjustments(): List<JsonObject> =
        supabase.postgrest.from("balance_adjustments").select { order("created_at", Order.DESCENDING) }.decodeList<JsonObject>()

    suspend fun withdrawalHistory(): List<JsonObject> =
        supabase.postgrest.from("redemptions").select { order("created_at", Order.DESCENDING) }.decodeList<JsonObject>()

    suspend fun rewardCatalog(): List<JsonObject> =
        supabase.postgrest.from("rewards").select { order("type", Order.ASCENDING); order("cost", Order.ASCENDING) }.decodeList<JsonObject>()

}
