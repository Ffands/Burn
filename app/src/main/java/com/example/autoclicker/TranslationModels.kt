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
     * Pure geometric layout clustering.
     * Merges lines into coherent paragraphs or blocks without relying on punctuation!
     * Preserves columns (tables, chat bubbles, UI lists).
     */
    fun clusterBlocksGeometrically(
        items: List<RawOcrItem>,
        lineGapFactor: Float = 1.25f
    ): List<RawOcrItem> {
        if (items.size <= 1) return items

        val sorted = items.sortedWith(Comparator { a, b ->
            val yDiff = a.rect.top - b.rect.top
            if (Math.abs(yDiff) > 8) yDiff else a.rect.left - b.rect.left
        })

        val clusters = mutableListOf<MutableList<RawOcrItem>>()

        for (item in sorted) {
            var merged = false
            for (cluster in clusters) {
                val clusterLeft = cluster.minOf { it.rect.left }
                val clusterRight = cluster.maxOf { it.rect.right }
                val clusterTop = cluster.minOf { it.rect.top }
                val clusterBottom = cluster.maxOf { it.rect.bottom }

                val avgLineHeight = Math.max(16f, (clusterBottom - clusterTop).toFloat() / cluster.size)
                val verticalGap = item.rect.top - clusterBottom
                val maxAllowedGap = avgLineHeight * lineGapFactor

                val isVerticallyAdjacent = verticalGap >= -avgLineHeight * 0.5f && verticalGap <= maxAllowedGap

                // Horizontal overlap or alignment check
                val xOverlap = Math.min(clusterRight, item.rect.right) - Math.max(clusterLeft, item.rect.left)
                val isHorizontallyAligned = xOverlap > 5 ||
                        Math.abs(clusterLeft - item.rect.left) < 24 ||
                        Math.abs(clusterRight - item.rect.right) < 24

                // Gutter / column separation (e.g. distinct table column)
                val isSeparateColumn = item.rect.left > clusterRight + 16 || clusterLeft > item.rect.right + 16

                if (isVerticallyAdjacent && isHorizontallyAligned && !isSeparateColumn) {
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
        "danke", "guten", "tag", "morgen", "abend", "wie", "geht", "alles"
    )
    private val ENGLISH_COMMON = setOf(
        "the", "and", "of", "to", "in", "is", "are", "that", "this", "was",
        "were", "for", "it", "with", "as", "on", "be", "at", "by", "have",
        "has", "had", "from", "or", "but", "not", "what", "all", "we", "when",
        "your", "you", "can", "could", "there", "their", "which", "do", "how",
        "will", "would", "about", "out", "many", "then", "them", "these", "so"
    )

    fun detect(text: String): String {
        if (text.isBlank()) return "und"

        var cyrillicCount = 0
        var latinCount = 0
        for (ch in text) {
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

        if (GERMAN_UMLAUTS.containsMatchIn(text)) {
            return "de"
        }

        val words = text.lowercase().split(Regex("[^\\p{L}]+")).filter { it.length > 1 }
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
