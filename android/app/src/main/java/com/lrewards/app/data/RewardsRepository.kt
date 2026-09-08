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

    suspend fun playGame(gameType: String, amount: Int): JsonObject =
        supabase.postgrest.rpc(
            "play_reward_game",
            parameters = buildJsonObject {
                put("p_game_type", JsonPrimitive(gameType))
                put("p_amount", JsonPrimitive(amount))
            },
        ).decodeAs()

    suspend fun requestRedemption(rewardType: String, cost: Int): JsonObject =
        supabase.postgrest.rpc(
            "request_redemption",
            parameters = buildJsonObject {
                put("p_reward_type", JsonPrimitive(rewardType))
                put("p_cost", JsonPrimitive(cost))
            },
        ).decodeAs()

    suspend fun rewardTransactions(): List<JsonObject> =
        supabase.postgrest.from("transactions").select { order("created_at", Order.DESCENDING) }.decodeList<JsonObject>()

    suspend fun withdrawalHistory(): List<JsonObject> =
        supabase.postgrest.from("redemptions").select { order("created_at", Order.DESCENDING) }.decodeList<JsonObject>()
}
