package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteAuthResult(
  val success: Boolean,
  val name: String = "",
  val phone: String = "",
  val email: String = "",
  val role: String = "USER",
  val walletBalance: Double = 0.0,
  val errorMessage: String? = null
)

object ServerConfig {
  private const val PREFS_NAME = "pay4u_server_prefs"
  private const val KEY_VPS_URL = "vps_base_url"

  private val client = OkHttpClient.Builder()
    .connectTimeout(4, TimeUnit.SECONDS)
    .readTimeout(4, TimeUnit.SECONDS)
    .build()

  private var prefs: SharedPreferences? = null

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
  }

  fun getVpsUrl(): String {
    return prefs?.getString(KEY_VPS_URL, "") ?: ""
  }

  fun setVpsUrl(url: String) {
    val clean = url.trim().trimEnd('/')
    prefs?.edit()?.putString(KEY_VPS_URL, clean)?.apply()
  }

  suspend fun testConnection(targetUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val clean = targetUrl.trim().trimEnd('/')
    if (clean.isBlank()) return@withContext Pair(false, "URL cannot be empty")
    try {
      val testUrl = if (clean.startsWith("http://") || clean.startsWith("https://")) clean else "http://$clean"
      val request = Request.Builder()
        .url("$testUrl/api/state")
        .get()
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        Pair(true, "Connected successfully to pay4u VPS server!")
      } else {
        Pair(false, "Server responded with HTTP ${response.code}")
      }
    } catch (e: Exception) {
      Pair(false, "Connection error: ${e.message ?: "Unable to reach host"}")
    }
  }

  suspend fun loginRemote(ident: String, pass: String): RemoteAuthResult = withContext(Dispatchers.IO) {
    var rawUrl = getVpsUrl()
    if (rawUrl.isBlank()) {
      return@withContext RemoteAuthResult(false, errorMessage = "No VPS server URL configured")
    }
    if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
      rawUrl = "http://$rawUrl"
    }

    try {
      val json = JSONObject().apply {
        put("identifier", ident.trim())
        put("password", pass.trim())
      }.toString()

      val request = Request.Builder()
        .url("$rawUrl/api/auth/login")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val body = response.body?.string() ?: ""
      if (response.isSuccessful && body.isNotBlank()) {
        val resJson = JSONObject(body)
        if (resJson.optBoolean("success", false)) {
          val userObj = resJson.optJSONObject("user")
          return@withContext RemoteAuthResult(
            success = true,
            name = userObj?.optString("name", "User") ?: "User",
            phone = userObj?.optString("phone", ident) ?: ident,
            email = userObj?.optString("email", "") ?: "",
            role = userObj?.optString("role", "USER") ?: "USER",
            walletBalance = userObj?.optDouble("walletBalance", 1250.0) ?: 1250.0
          )
        } else {
          return@withContext RemoteAuthResult(
            success = false,
            errorMessage = resJson.optString("error", "Invalid credentials")
          )
        }
      } else {
        val errObj = try { JSONObject(body).optString("error") } catch (_: Exception) { null }
        return@withContext RemoteAuthResult(
          success = false,
          errorMessage = errObj ?: "Server returned HTTP ${response.code}"
        )
      }
    } catch (e: Exception) {
      return@withContext RemoteAuthResult(false, errorMessage = e.message ?: "Network error")
    }
  }
}
