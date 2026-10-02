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
                
                // CRITICAL FIX: NEVER merge lines of different languages (e.g. Russian and English table cells)
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
    private val GERMAN_UMLAUTS = Regex("[äöüßÄÖÜ]")
    private val GERMAN_COMMON = setOf(
        "der", "die", "das", "den", "dem", "des", "und", "ist", "sind", "nicht",
        "sie", "wir", "ihr", "für", "ein", "eine", "einer", "einem", "einen",
        "mit", "auf", "nach", "zu", "im", "in", "von", "als", "auch", "es",
        "an", "er", "hat", "haben", "dass", "wenn", "aber", "hier", "bitte",
        "danke", "guten", "tag", "morgen", "abend", "wie", "geht", "alles",
        "sein", "muss", "man", "viel", "lernen", "gebildet"
    )
    private val ENGLISH_COMMON = setOf(
        "the", "and", "of", "to", "in", "is", "are", "that", "this", "was",
        "were", "for", "it", "with", "as", "on", "be", "at", "by", "have",
        "has", "had", "from", "or", "but", "not", "what", "all", "we", "when",
        "your", "you", "can", "could", "there", "their", "which", "do", "how",
        "will", "would", "about", "out", "many", "then", "them", "these", "so",
        "study", "educated", "lot", "need"
    )

    /**
     * Filters out single characters like 'D', 'A', '1', and non-letter noise.
     */
    fun isIgnorableNoise(text: String): Boolean {
        val clean = text.trim()
        if (clean.length <= 1) return true

        val letters = clean.filter { it.isLetter() }
        if (letters.length < 2) return true

        // Ignore pure single-letter tokens (e.g. "D" or "  D  ")
        val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        val meaningfulWords = words.filter { w -> w.any { it.isLetter() } && w.length >= 2 }
        return meaningfulWords.isEmpty()
    }

    /**
     * Determines whether text contains any foreign content that can and should be translated into targetLang.
     * Prevents skipping mixed language blocks!
     */
    fun hasTranslatableContent(text: String, targetLang: String): Boolean {
        val clean = text.trim()
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
            "ru" -> {
                // If translating to Russian, ANY Latin words/letters mean there is translatable content!
                latinCount >= 2 || GERMAN_UMLAUTS.containsMatchIn(clean)
            }
            "en" -> {
                // If translating to English, ANY Cyrillic or German umlauts mean translatable content
                cyrillicCount >= 2 || GERMAN_UMLAUTS.containsMatchIn(clean)
            }
            "de" -> {
                // If translating to German, ANY Cyrillic or non-German content
                cyrillicCount >= 2 || latinCount >= 2
            }
            else -> true
        }
    }

    fun detect(text: String): String {
        if (text.isBlank()) return "und"

        val sanitized = OfflineTranslationEngine.sanitizeOcrHomoglyphs(text)

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

        if (cyrillicCount > 0 && cyrillicCount >= latinCount) {
            return "ru"
        }

        if (GERMAN_UMLAUTS.containsMatchIn(sanitized)) {
            return "de"
        }

        val words = sanitized.lowercase().split(Regex("[^\\p{L}]+")).filter { it.length > 1 }
        if (words.isEmpty()) {
            return if (cyrillicCount > 0) "ru" else if (latinCount > 0) "en" else "und"
        }

        var deScore = 0
        var enScore = 0
        for (w in words) {
            if (GERMAN_COMMON.contains(w)) deScore += 2
            if (ENGLISH_COMMON.contains(w)) enScore += 2
        }

        if (deScore > enScore && deScore > 0) return "de"
        if (enScore >= deScore && enScore > 0) return "en"

        return if (cyrillicCount > 0) "ru" else "en"
    }
}
