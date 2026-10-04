package com.example.autoclicker

import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object NetworkTranslationService {

    private const val TAG = "NetworkTranslation"

    /**
     * Translates text using Google Neural Machine Translation (GTX endpoint).
     * Returns null if offline or on network failure, allowing instant fallback to offline dictionary.
     */
    fun translateOnline(text: String, srcLang: String, targetLang: String): String? {
        if (text.isBlank()) return text

        var connection: HttpURLConnection? = null
        return try {
            val encodedQuery = URLEncoder.encode(text, "UTF-8")
            val sl = if (srcLang == "und" || srcLang == "mix") "auto" else srcLang
            val tl = targetLang
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$tl&dt=t&q=$encodedQuery"

            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3500
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val response = reader.use { it.readText() }

                val jsonArray = JSONArray(response)
                val sentencesArray = jsonArray.optJSONArray(0)
                if (sentencesArray != null && sentencesArray.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until sentencesArray.length()) {
                        val part = sentencesArray.optJSONArray(i)
                        if (part != null) {
                            val translatedSegment = part.optString(0, "")
                            if (translatedSegment.isNotEmpty() && translatedSegment != "null") {
                                sb.append(translatedSegment)
                            }
                        }
                    }
                    val result = sb.toString().trim()
                    if (result.isNotEmpty()) {
                        return result
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Online translation failed (will use offline fallback): ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }
}
