package com.example.projectonhand

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Client API Project On Hand PPIC (Bearer token Sanctum, login otomatis + relogin saat 401). */
object PohApi {
    @Volatile private var token: String? = null
    private const val BASE = "/api/project-on-hand-ppic/project-list"

    private fun http(method: String, url: String, body: String? = null): Pair<Int, String> {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 15_000; c.readTimeout = 30_000
            c.setRequestProperty("Accept", "application/json")
            token?.let { c.setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json")
                c.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            return code to (stream?.bufferedReader()?.use { it.readText() } ?: "")
        } finally { c.disconnect() }
    }

    private fun login() {
        token = null
        val body = JSONObject().put("username", ApiConfig.USERNAME).put("password", ApiConfig.PASSWORD).toString()
        val (code, text) = http("POST", ApiConfig.BASE_URL + "/api/login", body)
        if (code !in 200..299) error("Login gagal (HTTP $code)")
        token = JSONObject(text).optJSONObject("result")?.optString("access_token")?.ifEmpty { null }
            ?: error("Login gagal: token tidak ditemukan")
    }

    private fun query(params: List<Pair<String, String>>) =
        params.joinToString("&") { (k, v) -> URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8") }

    /** GET dengan login otomatis dan satu kali relogin saat 401. */
    private fun getJson(url: String): JSONObject {
        if (token == null) login()
        var res = http("GET", url)
        if (res.first == 401) { login(); res = http("GET", url) }
        if (res.first !in 200..299) error("Server error (HTTP ${res.first})")
        return JSONObject(res.second)
    }

    /** GET /api/project-on-hand-ppic/project-list */
    suspend fun fetchList(params: List<Pair<String, String>>): PohData = withContext(Dispatchers.IO) {
        PohParser.parse(getJson(ApiConfig.BASE_URL + BASE + "?" + query(params)))
    }

    /** GET /api/project-on-hand-ppic/project-list/others (isi modal Others) */
    suspend fun fetchOthers(params: List<Pair<String, String>>): OthersData = withContext(Dispatchers.IO) {
        PohParser.parseOthers(getJson(ApiConfig.BASE_URL + BASE + "/others?" + query(params)))
    }
}