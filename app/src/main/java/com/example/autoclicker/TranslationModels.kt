package com.example.autoclicker

import android.graphics.Rect
import kotlin.math.sqrt

enum class CornerType {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

data class CornerInfo(
    val corner: CornerType,
    val x: Float,
    val y: Float,
    val distanceToCenter: Float
)

data class RawOcrItem(
    val id: String,
    val text: String,
    val rect: Rect,
    val confidence: Float = 1.0f
)

data class TranslationBlock(
    val id: String,
    val originalText: String,
    val translatedText: String,
    val detectedLang: String, // "ru", "en", "de", "und"
    val targetLang: String,   // "ru", "en", "de"
    val rect: Rect,
    val nearestCorner: CornerInfo,
    val isSkippedSameLang: Boolean
)

data class LanguagePackItem(
    val id: String,
    val name: String,
    val nativeName: String,
    val sizeMb: Int,
    var isEnabled: Boolean = true
)

object GeometryHelper {
    /**
     * Calculates the corner of a bounding box closest to the screen center (W/2, H/2).
     */
    fun calculateNearestCorner(rect: Rect, screenWidth: Int, screenHeight: Int): CornerInfo {
        val cx = screenWidth / 2f
        val cy = screenHeight / 2f

        val corners = listOf(
            Triple(CornerType.TOP_LEFT, rect.left.toFloat(), rect.top.toFloat()),
            Triple(CornerType.TOP_RIGHT, rect.right.toFloat(), rect.top.toFloat()),
            Triple(CornerType.BOTTOM_LEFT, rect.left.toFloat(), rect.bottom.toFloat()),
            Triple(CornerType.BOTTOM_RIGHT, rect.right.toFloat(), rect.bottom.toFloat())
        )

        var minDistance = Float.MAX_VALUE
        var bestCorner = corners[0]

        for (c in corners) {
            val dx = c.second - cx
            val dy = c.third - cy
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist < minDistance) {
                minDistance = dist
                bestCorner = c
            }
        }

        return CornerInfo(
            corner = bestCorner.first,
            x = bestCorner.second,
            y = bestCorner.third,
            distanceToCenter = minDistance
        )
    }

    /**
     * Geometric layout clustering with strict language separation.
     * Prevents merging table rows or dialogue lines with different languages!
     */
    fun clusterBlocksGeometrically(
        items: List<RawOcrItem>,
        lineGapFactor: Float = 0.65f
    ): List<RawOcrItem> {
        if (items.size <= 1) return items

        val sorted = items.sortedWith { a, b ->
            val yDiff = a.rect.top - b.rect.top
            if (Math.abs(yDiff) > 12) yDiff else a.rect.left - b.rect.left
        }

        val clusters = mutableListOf<MutableList<RawOcrItem>>()

        for (item in sorted) {
            val itemLang = LanguageDetectorOffline.detect(item.text)
            var merged = false

            for (cluster in clusters) {
                val clusterFirstLang = LanguageDetectorOffline.detect(cluster[0].text)
                
                // Never merge lines of different languages
                if (itemLang != "und" && clusterFirstLang != "und" && itemLang != clusterFirstLang) {
                    continue
                }

                val clusterLeft = cluster.minOf { it.rect.left }
                val clusterRight = cluster.maxOf { it.rect.right }
                val clusterTop = cluster.minOf { it.rect.top }
                val clusterBottom = cluster.maxOf { it.rect.bottom }

                val avgLineHeight = Math.max(20f, (clusterBottom - clusterTop).toFloat() / cluster.size)
                val verticalGap = item.rect.top - clusterBottom
                val maxAllowedGap = avgLineHeight * lineGapFactor

                val isVerticallyAdjacent = verticalGap >= -avgLineHeight * 0.3f && verticalGap <= maxAllowedGap

                // Overlap check
                val xOverlap = Math.min(clusterRight, item.rect.right) - Math.max(clusterLeft, item.rect.left)
                val minWidth = Math.min(clusterRight - clusterLeft, item.rect.right - item.rect.left)
                val hasOverlapRatio = minWidth > 0 && (xOverlap.toFloat() / minWidth) > 0.35f

                // Table column separation check
                val isSeparateColumn = item.rect.left > clusterRight + 12 || clusterLeft > item.rect.right + 12

                if (isVerticallyAdjacent && hasOverlapRatio && !isSeparateColumn) {
                    cluster.add(item)
                    merged = true
                    break
                }
            }

            if (!merged) {
                clusters.add(mutableListOf(item))
            }
        }

        return clusters.mapIndexed { index, cluster ->
            cluster.sortBy { it.rect.top }
            val combinedText = cluster.joinToString(" ") { it.text.trim() }
            val minLeft = cluster.minOf { it.rect.left }
            val minTop = cluster.minOf { it.rect.top }
            val maxRight = cluster.maxOf { it.rect.right }
            val maxBottom = cluster.maxOf { it.rect.bottom }

            RawOcrItem(
                id = "cluster_$index",
                text = combinedText,
                rect = Rect(minLeft, minTop, maxRight, maxBottom)
            )
        }
    }
}

object LanguageDetectorOffline {
    // 1. Уникальные маркерные буквы / символы
    private val GERMAN_UMLAUTS = Regex("[äöüßÄÖÜ]")

    // 2. Служебные слова / артикли / предлоги
    private val GERMAN_ARTICLES_AND_STOPS = setOf(
        "der", "die", "das", "dem", "den", "des",
        "ein", "eine", "einer", "einem", "einen", "eines",
        "nicht", "und", "oder", "aber", "wenn", "dass",
        "ist", "sind", "war", "waren", "wird", "werden",
        "mit", "auf", "nach", "zu", "im", "in", "von", "für",
        "als", "auch", "es", "an", "er", "hat", "haben",
        "muss", "man", "viel", "wir", "ihr", "sie", "sein"
    )

    private val ENGLISH_ARTICLES_AND_STOPS = setOf(
        "the", "a", "an", "this", "that", "these", "those",
        "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did",
        "with", "from", "for", "at", "by", "to", "in", "on", "of",
        "which", "what", "where", "when", "who", "how",
        "will", "would", "should", "could", "can",
        "not", "and", "or", "but", "you", "your", "they", "their", "we", "our"
    )

    // 3. Словарные корни
    private val GERMAN_ROOTS = setOf(
        "lernen", "gebildet", "einstellungen", "übersetzung", "schaltfläche",
        "dokument", "tabelle", "artikel", "daten", "nachricht", "zeit",
        "summe", "status", "autonom", "betrieb", "spezifikationen", "bezeichnung",
        "menge", "einzelpreis", "gesamtbetrag", "bestellstatus", "bezahlt",
        "bearbeitung", "zugestellt", "abbrechen", "bestätigen", "richtlinie",
        "bedingungen", "rechte", "bilder", "hochladen", "inhalt", "bildschirm"
    )

    private val ENGLISH_ROOTS = setOf(
        "screen", "translator", "cast", "click", "cloner", "app", "apps",
        "download", "downloads", "pictures", "movies", "alarms", "documents",
        "study", "educated", "learn", "lot", "need", "text", "settings",
        "translation", "button", "document", "table", "article", "data",
        "message", "time", "sum", "status", "autonomous", "system", "operation",
        "device", "specifications", "bundled", "pack", "recognition", "mode",
        "offline", "generating", "start", "creating", "media", "cancel",
        "acknowledge", "policy", "terms", "rights", "images", "upload", "content",
        "prohibited", "privacy", "agreement", "service", "accessibility"
    )

    /**
     * Filters out single characters like 'D', 'A', '1', and non-letter noise.
     */
    fun isIgnorableNoise(text: String): Boolean {
        val sanitized = OfflineTranslationEngine.sanitizeOcrHomoglyphs(text)
        val clean = sanitized.trim()
        if (clean.length <= 1) return true

        val letters = clean.filter { it.isLetter() }
        if (letters.length < 2) return true

        val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        val meaningfulWords = words.filter { w -> w.any { it.isLetter() } && w.length >= 2 }
        return meaningfulWords.isEmpty()
    }

    /**
     * Determines whether text contains any foreign content that can and should be translated into targetLang.
     */
    fun hasTranslatableContent(text: String, targetLang: String): Boolean {
        val sanitized = OfflineTranslationEngine.sanitizeOcrHomoglyphs(text)
        val clean = sanitized.trim()
        if (isIgnorableNoise(clean)) return false

        var cyrillicCount = 0
        var latinCount = 0
        for (ch in clean) {
            val code = ch.code
            if (code in 0x0400..0x04FF) {
                cyrillicCount++
            } else if ((code in 65..90) || (code in 97..122)) {
                latinCount++
            }
        }

        return when (targetLang) {
            "ru" -> latinCount >= 2 || GERMAN_UMLAUTS.containsMatchIn(clean)
            "en" -> cyrillicCount >= 2 || GERMAN_UMLAUTS.containsMatchIn(clean)
            "de" -> cyrillicCount >= 2 || latinCount >= 2
            else -> true
        }
    }

    /**
     * 4-ступенчатая система проверки языка:
     * 1. Странные / уникальные буквы (Fast-Path):
     *    - Кириллица (cyrillic >= latin) -> "ru"
     *    - Умлауты / эсцет (ä, ö, ü, ß) -> "de"
     * 2. Артикли и служебные слова:
     *    - der, die, das, ein, ist, sind, mit -> "de"
     *    - the, a, an, this, is, are, with -> "en"
     * 3. Буквосочетания (N-grams):
     *    - sch, ei, ie, tz, pf, ung -> "de"
     *    - th, sh, wh, ee, oo, ea, qu, ing -> "en"
     * 4. Словарь + слитные слова (CamelCase / Compound split):
     *    - ScreenTranslator -> screen + translator -> "en"
     *    - Поиск корней по словарю
     *    - Если латиница -> "en" (НИКОГДА не возвращать "ru" для латиницы!)
     */
    fun detect(text: String): String {
        if (text.isBlank()) return "und"

        val sanitized = OfflineTranslationEngine.sanitizeOcrHomoglyphs(text)

        // ==========================================
        // 1. СТРАННЫЕ / УНИКАЛЬНЫЕ БУКВЫ
        // ==========================================
        var cyrillicCount = 0
        var latinCount = 0
        for (ch in sanitized) {
            val code = ch.code
            if (code in 0x0400..0x04FF) {
                cyrillicCount++
            } else if ((code in 65..90) || (code in 97..122)) {
                latinCount++
            }
        }

        // Чистая кириллица
        if (cyrillicCount >= latinCount && cyrillicCount >= 2) {
            return "ru"
        }

        // Немецкие умлауты или эсцет
        if (GERMAN_UMLAUTS.containsMatchIn(sanitized)) {
            return "de"
        }

        val lowerText = sanitized.lowercase()
        val rawWords = sanitized.split(Regex("[^\\p{L}]+")).filter { it.length > 1 }
        if (rawWords.isEmpty()) {
            return if (cyrillicCount > latinCount) "ru" else if (latinCount > 0) "en" else "und"
        }

        // ==========================================
        // 2. АРТИКЛИ И СЛУЖЕБНЫЕ СЛОВА
        // ==========================================
        var deArticles = 0
        var enArticles = 0
        for (w in rawWords) {
            val lw = w.lowercase()
            if (GERMAN_ARTICLES_AND_STOPS.contains(lw)) deArticles++
            if (ENGLISH_ARTICLES_AND_STOPS.contains(lw)) enArticles++
        }

        if (deArticles > enArticles && deArticles > 0) return "de"
        if (enArticles > deArticles && enArticles > 0) return "en"

        // ==========================================
        // 3. СЛОВАРЬ + СЛИТНЫЕ / СОСТАВНЫЕ СЛОВА
        // ==========================================
        var deDictHits = 0
        var enDictHits = 0

        // Разбиваем слитные слова (например, ScreenTranslator -> Screen + Translator)
        val expandedTokens = mutableListOf<String>()
        for (word in rawWords) {
            expandedTokens.addAll(OfflineTranslationEngine.splitCompoundWord(word))
        }

        for (token in expandedTokens) {
            val lt = token.lowercase()
            if (GERMAN_ROOTS.contains(lt)) deDictHits++
            if (ENGLISH_ROOTS.contains(lt)) enDictHits++
        }

        if (deDictHits > enDictHits) return "de"
        if (enDictHits > deDictHits) return "en"

        // Латиница по умолчанию всегда EN
        return if (latinCount >= 2) "en" else if (cyrillicCount >= 2) "ru" else "en"
    }
}
