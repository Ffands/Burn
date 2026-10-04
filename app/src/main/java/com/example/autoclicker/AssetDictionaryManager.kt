package com.example.autoclicker

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

object AssetDictionaryManager {
    private const val TAG = "AssetDict"

    private val enRuMap = ConcurrentHashMap<String, String>(50000)
    private val deRuMap = ConcurrentHashMap<String, String>(75000)
    
    @Volatile
    var isLoaded = false
        private set

    fun initialize(context: Context) {
        if (isLoaded) return
        Thread {
            try {
                loadTsv(context, "dict_en_ru.tsv.gz", enRuMap)
                loadTsv(context, "dict_de_ru.tsv.gz", deRuMap)
                isLoaded = true
                Log.i(TAG, "Bundled dictionaries loaded: EN=${enRuMap.size}, DE=${deRuMap.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading bundled dictionaries: ${e.message}", e)
            }
        }.start()
    }

    private fun loadTsv(context: Context, filename: String, map: ConcurrentHashMap<String, String>) {
        try {
            val rawStream = context.assets.open(filename)
            val stream = if (filename.endsWith(".gz")) java.util.zip.GZIPInputStream(rawStream) else rawStream
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
                    val tabIdx = trimmed.indexOf('\t')
                    if (tabIdx > 0) {
                        val key = trimmed.substring(0, tabIdx).trim().lowercase()
                        val value = trimmed.substring(tabIdx + 1).trim()
                        if (key.isNotEmpty()) {
                            map[key] = value
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not open asset $filename: ${e.message}")
        }
    }

    fun lookup(word: String, srcLang: String): String? {
        val lower = word.lowercase()
        val primaryMap = if (srcLang == "de") deRuMap else enRuMap
        val secondaryMap = if (srcLang == "de") enRuMap else deRuMap

        // 1. Direct match
        primaryMap[lower]?.let { if (it.isNotEmpty()) return it }
        secondaryMap[lower]?.let { if (it.isNotEmpty()) return it }

        // 2. English stemming
        if (srcLang != "de") {
            // Strip possessive
            if (lower.endsWith("'s") && lower.length > 2) {
                primaryMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
            }
            // Strip plural -ies, -es, -s
            if (lower.endsWith("ies") && lower.length > 4) {
                val base = lower.substring(0, lower.length - 3) + "y"
                primaryMap[base]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("es") && lower.length > 3) {
                primaryMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("s") && lower.length > 2) {
                primaryMap[lower.substring(0, lower.length - 1)]?.let { if (it.isNotEmpty()) return it }
            }
            // Strip -ed
            if (lower.endsWith("ed") && lower.length > 3) {
                primaryMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
                primaryMap[lower.substring(0, lower.length - 1)]?.let { if (it.isNotEmpty()) return it }
            }
            // Strip -ing
            if (lower.endsWith("ing") && lower.length > 4) {
                primaryMap[lower.substring(0, lower.length - 3)]?.let { if (it.isNotEmpty()) return it }
                primaryMap[lower.substring(0, lower.length - 3) + "e"]?.let { if (it.isNotEmpty()) return it }
            }
        }

        // 3. German stemming
        if (srcLang == "de" || srcLang == "mix" || srcLang == "und") {
            if (lower.endsWith("en") && lower.length > 3) {
                deRuMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
                deRuMap[lower.substring(0, lower.length - 2) + "e"]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("n") && lower.length > 2) {
                deRuMap[lower.substring(0, lower.length - 1)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("e") && lower.length > 2) {
                deRuMap[lower.substring(0, lower.length - 1)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("er") && lower.length > 3) {
                deRuMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("es") && lower.length > 3) {
                deRuMap[lower.substring(0, lower.length - 2)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("s") && lower.length > 2) {
                deRuMap[lower.substring(0, lower.length - 1)]?.let { if (it.isNotEmpty()) return it }
            }
            if (lower.endsWith("t") && lower.length > 3) {
                deRuMap[lower.substring(0, lower.length - 1) + "en"]?.let { if (it.isNotEmpty()) return it }
            }
        }

        return null
    }

    /**
     * Splits concatenated compound words like "themilitaryamount" into ["the", "military", "amount"].
     */
    fun splitConcatenatedCompounds(word: String, srcLang: String): List<String> {
        val clean = word.trim().lowercase()
        if (clean.length < 6) return listOf(word)

        val map = if (srcLang == "de") deRuMap else enRuMap
        val results = mutableListOf<String>()
        var start = 0

        while (start < clean.length) {
            var longestMatch = ""
            var longestEnd = start

            // Greedy search for the longest matching dictionary word
            for (end in (start + 2)..clean.length) {
                val sub = clean.substring(start, end)
                if (map.containsKey(sub) || enRuMap.containsKey(sub) || deRuMap.containsKey(sub)) {
                    longestMatch = sub
                    longestEnd = end
                }
            }

            if (longestMatch.isNotEmpty()) {
                results.add(longestMatch)
                start = longestEnd
            } else {
                // No match from this position, break
                return listOf(word)
            }
        }

        return if (results.size > 1) results else listOf(word)
    }
}
