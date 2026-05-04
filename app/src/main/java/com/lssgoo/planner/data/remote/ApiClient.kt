package com.lssgoo.planner.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ApiResponse<T>(
    val message: String?,
    val status: String?,
    val data: T?,
    val errors: List<Map<String, Any>>?,
    val timestamp: String?
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

class ApiClient(private val context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("planner_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun saveUserInfo(userId: Long, email: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1)

    fun clearAuth() {
        prefs.edit().clear().apply()
    }

    suspend fun <T> get(path: String, typeToken: TypeToken<ApiResponse<T>>): ApiResponse<T>? =
        withContext(Dispatchers.IO) {
            request("GET", path, null, typeToken)
        }

    suspend fun <T> post(path: String, body: Any?, typeToken: TypeToken<ApiResponse<T>>): ApiResponse<T>? =
        withContext(Dispatchers.IO) {
            request("POST", path, body, typeToken)
        }

    suspend fun <T> put(path: String, body: Any?, typeToken: TypeToken<ApiResponse<T>>): ApiResponse<T>? =
        withContext(Dispatchers.IO) {
            request("PUT", path, body, typeToken)
        }

    suspend fun <T> delete(path: String, typeToken: TypeToken<ApiResponse<T>>): ApiResponse<T>? =
        withContext(Dispatchers.IO) {
            request("DELETE", path, null, typeToken)
        }

    private fun <T> request(
        method: String,
        path: String,
        body: Any?,
        typeToken: TypeToken<ApiResponse<T>>
    ): ApiResponse<T>? {
        try {
            val url = URL("${ApiConfig.getBaseUrl()}$path")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "true")
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val token = getAccessToken()
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                connection.doOutput = true
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(gson.toJson(body))
                writer.flush()
                writer.close()
            }

            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseBody = reader.readText()
            reader.close()
            connection.disconnect()

            return gson.fromJson(responseBody, typeToken.type)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
