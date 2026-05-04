package com.lssgoo.planner.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
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
        private const val TAG = "PlannerAPI"
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
            // INFO so Logcat shows traffic with default "Info" level (no need to enable Verbose/Debug).
            Log.i(TAG, "$method $path → HTTP $responseCode (auth=${if (token != null) "yes" else "no"})")
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            } ?: return null

            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseBody = reader.readText()
            reader.close()
            connection.disconnect()

            val envelope = parseApiResponse(responseBody, typeToken)
            if (envelope != null) {
                Log.i(TAG, "  envelope: status=${envelope.status} msg=${envelope.message?.take(120)}")
            }
            return envelope
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Backend returns a JSON object; proxies or edge cases may return a JSON string, HTML, or plain text.
     * Gson would throw "Expected BEGIN_OBJECT but was STRING" — handle that here.
     */
    private fun <T> parseApiResponse(raw: String, typeToken: TypeToken<ApiResponse<T>>): ApiResponse<T>? {
        val body = raw.trim()
        if (body.isEmpty()) return null
        val root = try {
            JsonParser.parseString(body)
        } catch (_: JsonSyntaxException) {
            return ApiResponse(
                message = body.take(500),
                status = "INVALID_RESPONSE",
                data = null,
                errors = listOf(mapOf("message" to body.take(500), "raw" to body.take(500))),
                timestamp = null
            )
        }
        if (root.isJsonPrimitive) {
            val p = root.asJsonPrimitive
            val msg = if (p.isString) p.asString else p.toString()
            return ApiResponse(
                message = msg,
                status = "ERROR",
                data = null,
                errors = listOf(mapOf("message" to msg)),
                timestamp = null
            )
        }
        if (!root.isJsonObject) {
            return ApiResponse(
                message = "Unexpected JSON root",
                status = "INVALID_RESPONSE",
                data = null,
                errors = listOf(mapOf("message" to body.take(500))),
                timestamp = null
            )
        }
        return try {
            gson.fromJson(root, typeToken.type)
        } catch (e: JsonSyntaxException) {
            ApiResponse(
                message = e.message,
                status = "PARSE_ERROR",
                data = null,
                errors = listOf(mapOf("message" to (e.message ?: "parse error"))),
                timestamp = null
            )
        }
    }
}
