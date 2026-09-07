package com.lrewards.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Profile(val id: String, val email: String, val display_name: String, val coins: Int, val created_at: String? = null, val is_admin: Boolean = false, val is_banned: Boolean = false)

@Serializable
data class Transaction(val id: String, val user_id: String, val type: String, val amount: Int, val created_at: String)

@Serializable
data class Redemption(val id: String, val user_id: String, val reward_type: String, val amount: Int, val status: String, val destination: String, val created_at: String)

@Serializable
data class AdminMetrics(val total_users: Int, val total_coins: Int, val pending_withdrawals: Int)

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

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    suspend fun transactions(userId: String): List<Transaction> = supabase.from("transactions").select {
        filter { eq("user_id", userId) }
    }.decodeList()

    suspend fun updateProfile(displayName: String): Profile {
        val id = currentUserId() ?: error("not_authenticated")
        return supabase.from("profiles").update({ set("display_name", displayName.trim()) }) {
            filter { eq("id", id) }
        }.decodeSingle()
    }

    suspend fun requestRedemption(rewardType: String, amount: Int, destination: String): Redemption =
        supabase.rpc("request_redemption", buildJsonObject {
            put("p_reward_type", rewardType)
            put("p_amount", amount)
            put("p_destination", destination)
        }).decodeAs()

    suspend fun redemptions(userId: String): List<Redemption> = supabase.from("redemptions").select {
        filter { eq("user_id", userId) }
    }.decodeList()

    suspend fun adminMetrics(): AdminMetrics = supabase.rpc("admin_metrics").decodeAs()

    suspend fun adminAdjustCoins(userId: String, amount: Int): Profile =
        supabase.rpc("admin_set_user_coins", buildJsonObject {
            put("p_user_id", userId)
            put("p_amount", amount)
        }).decodeAs()

    suspend fun adminSetBanned(userId: String, banned: Boolean): Profile =
        supabase.rpc("admin_set_banned", buildJsonObject {
            put("p_user_id", userId)
            put("p_banned", banned)
        }).decodeAs()

    suspend fun adminSetRedemptionStatus(redemptionId: String, status: String): Redemption =
        supabase.rpc("admin_set_redemption_status", buildJsonObject {
            put("p_redemption_id", redemptionId)
            put("p_status", status)
        }).decodeAs()
}
