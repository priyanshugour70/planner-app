package com.lssgoo.planner.data.remote

import android.content.Context
import com.google.gson.reflect.TypeToken

class PlannerApiService(context: Context) {

    private val client = ApiClient(context)

    fun isLoggedIn(): Boolean = client.isLoggedIn()

    fun getClient(): ApiClient = client

    // =================== AUTH ===================

    suspend fun register(
        firstName: String, lastName: String, email: String,
        password: String, gender: String? = null
    ): ApiResponse<Map<String, Any>>? {
        val body = mutableMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "password" to password
        )
        gender?.let { body["gender"] = it }

        val response = client.post(
            "/api/v1/auth/register",
            body,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )

        if (response?.data != null) {
            val data = response.data
            val accessToken = data["accessToken"] as? String
            val refreshToken = data["refreshToken"] as? String
            if (accessToken != null && refreshToken != null) {
                client.saveTokens(accessToken, refreshToken)
                @Suppress("UNCHECKED_CAST")
                val user = data["user"] as? Map<String, Any>
                val userId = (user?.get("id") as? Number)?.toLong() ?: -1
                val userEmail = user?.get("email") as? String ?: email
                client.saveUserInfo(userId, userEmail)
            }
        }
        return response
    }

    suspend fun login(email: String, password: String): ApiResponse<Map<String, Any>>? {
        val body = mapOf("email" to email, "password" to password)
        val response = client.post(
            "/api/v1/auth/login",
            body,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )

        if (response?.data != null) {
            val data = response.data
            val accessToken = data["accessToken"] as? String
            val refreshToken = data["refreshToken"] as? String
            if (accessToken != null && refreshToken != null) {
                client.saveTokens(accessToken, refreshToken)
                @Suppress("UNCHECKED_CAST")
                val user = data["user"] as? Map<String, Any>
                val userId = (user?.get("id") as? Number)?.toLong() ?: -1
                val userEmail = user?.get("email") as? String ?: email
                client.saveUserInfo(userId, userEmail)
            }
        }
        return response
    }

    suspend fun logout(): ApiResponse<Any?>? {
        val response = client.post(
            "/api/v1/auth/logout",
            null,
            object : TypeToken<ApiResponse<Any?>>() {}
        )
        client.clearAuth()
        return response
    }

    suspend fun getCurrentUser(): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/auth/me",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun sendOtp(email: String): ApiResponse<Any?>? {
        val body = mapOf("email" to email)
        return client.post(
            "/api/v1/auth/send-otp",
            body,
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    suspend fun verifyOtp(email: String, code: String, guestToken: String? = null): ApiResponse<Map<String, Any>>? {
        val body = mutableMapOf<String, Any>("email" to email, "code" to code)
        guestToken?.let { body["guestToken"] = it }

        val response = client.post(
            "/api/v1/auth/verify-otp",
            body,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )

        if (response?.data != null) {
            val data = response.data
            val accessToken = data["accessToken"] as? String
            val refreshToken = data["refreshToken"] as? String
            if (accessToken != null && refreshToken != null) {
                client.saveTokens(accessToken, refreshToken)
                @Suppress("UNCHECKED_CAST")
                val user = data["user"] as? Map<String, Any>
                val userId = (user?.get("id") as? Number)?.toLong() ?: -1
                val userEmail = user?.get("email") as? String ?: email
                client.saveUserInfo(userId, userEmail)
            }
        }
        return response
    }

    suspend fun guestLogin(deviceId: String): ApiResponse<Map<String, Any>>? {
        val body = mapOf("deviceId" to deviceId, "deviceInfo" to "Android")
        val response = client.post(
            "/api/v1/auth/guest",
            body,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )

        if (response?.data != null) {
            val data = response.data
            val accessToken = data["accessToken"] as? String
            val refreshToken = data["refreshToken"] as? String
            if (accessToken != null && refreshToken != null) {
                client.saveTokens(accessToken, refreshToken)
                @Suppress("UNCHECKED_CAST")
                val user = data["user"] as? Map<String, Any>
                val userId = (user?.get("id") as? Number)?.toLong() ?: -1
                val userEmail = user?.get("email") as? String ?: ""
                client.saveUserInfo(userId, userEmail)
            }
        }
        return response
    }

    // =================== GOALS ===================

    suspend fun getGoals(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/goals?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createGoal(goal: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/goals",
            goal,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateGoal(uuid: String, goal: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/goals/$uuid",
            goal,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteGoal(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/goals/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    // =================== TASKS ===================

    suspend fun getTasks(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/tasks?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createTask(task: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/tasks",
            task,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateTask(uuid: String, task: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/tasks/$uuid",
            task,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteTask(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/tasks/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    suspend fun completeTask(uuid: String): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/tasks/$uuid/complete",
            null,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== NOTES ===================

    suspend fun getNotes(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/notes?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createNote(note: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/notes",
            note,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateNote(uuid: String, note: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/notes/$uuid",
            note,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteNote(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/notes/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    // =================== HABITS ===================

    suspend fun getHabits(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/habits?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createHabit(habit: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/habits",
            habit,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateHabit(uuid: String, habit: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/habits/$uuid",
            habit,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteHabit(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/habits/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    suspend fun createHabitEntry(entry: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/habits/entries",
            entry,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun logHabitEntry(entry: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/habits/entries",
            entry,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== JOURNAL ===================

    suspend fun getJournalEntries(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/journal?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createJournalEntry(entry: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/journal",
            entry,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteJournalEntry(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/journal/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    // =================== FINANCE ===================

    suspend fun getTransactions(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/finance/transactions?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createTransaction(transaction: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/finance/transactions",
            transaction,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateTransaction(uuid: String, transaction: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/finance/transactions/$uuid",
            transaction,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteTransaction(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/finance/transactions/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    suspend fun getFinanceStats(): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/finance/stats",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== REMINDERS ===================

    suspend fun getReminders(page: Int = 0, size: Int = 20): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/reminders?page=$page&size=$size",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun createReminder(reminder: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/reminders",
            reminder,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun updateReminder(uuid: String, reminder: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/reminders/$uuid",
            reminder,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun deleteReminder(uuid: String): ApiResponse<Any?>? {
        return client.delete(
            "/api/v1/reminders/$uuid",
            object : TypeToken<ApiResponse<Any?>>() {}
        )
    }

    // =================== PROFILE ===================

    suspend fun updateProfile(profile: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.put(
            "/api/v1/auth/profile",
            profile,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== SYNC ===================

    suspend fun syncPull(): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/sync/pull",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    suspend fun syncPush(data: Map<String, Any?>): ApiResponse<Map<String, Any>>? {
        return client.post(
            "/api/v1/sync/push",
            data,
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== ANALYTICS ===================

    suspend fun getDashboardStats(): ApiResponse<Map<String, Any>>? {
        return client.get(
            "/api/v1/analytics/dashboard",
            object : TypeToken<ApiResponse<Map<String, Any>>>() {}
        )
    }

    // =================== SEARCH ===================

    suspend fun search(query: String, types: List<String>? = null): ApiResponse<List<Map<String, Any>>>? {
        val typesParam = types?.joinToString(",")?.let { "&types=$it" } ?: ""
        return client.get(
            "/api/v1/search?query=$query$typesParam",
            object : TypeToken<ApiResponse<List<Map<String, Any>>>>() {}
        )
    }
}
