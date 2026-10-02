package com.example.autoclicker

object OfflineTranslationEngine {

    private data class Phrase(val ru: String, val en: String, val de: String)

    private val PHRASES = listOf(
        Phrase("привет как дела", "hello how are you", "hallo wie geht es dir"),
        Phrase("все отлично спасибо", "everything is great thank you", "alles ist super danke"),
        Phrase("где ты сейчас", "where are you now", "wo bist du jetzt"),
        Phrase("я уже еду на встречу", "i'm already on my way to the meeting", "ich bin schon auf dem weg zum treffen"),
        Phrase("отправь мне документ пожалуйста", "send me the document please", "sende mir bitte das dokument"),
        Phrase("хорошо сейчас скину", "okay will send it right now", "in ordnung ich schicke es gleich"),
        Phrase("договорились жду", "agreed waiting", "abgemacht ich warte"),
        Phrase("доброе утро", "good morning", "guten morgen"),
        Phrase("добрый день", "good afternoon", "guten tag"),
        Phrase("добрый вечер", "good evening", "guten abend"),
        Phrase("до свидания", "goodbye", "auf wiedersehen"),
        Phrase("до скорого", "see you soon", "bis bald"),
        Phrase("автономная работа системы", "autonomous system operation", "autonomer systembetrieb"),
        Phrase("распознавание текста на экране", "screen text recognition", "bildschirm-texterkennung"),
        Phrase("офлайн переводчик без интернета", "offline translator without internet", "offline-übersetzer ohne internet"),
        Phrase("пользовательское соглашение", "user agreement", "nutzervereinbarung"),
        Phrase("политика конфиденциальности", "privacy policy", "datenschutzerklärung"),
        Phrase("технические характеристики устройства", "device technical specifications", "technische daten des geräts"),
        Phrase("встроенный языковой пакет", "bundled language pack", "integriertes sprachpaket"),
        Phrase("геометрическая группировка блоков", "geometric block clustering", "geometrische blockgruppierung"),
        Phrase("нажмите для просмотра полного перевода", "click to view full translation", "klicken sie hier für die vollständige übersetzung"),
        Phrase("наименование товара", "product item name", "artikelbezeichnung"),
        Phrase("количество", "quantity", "menge"),
        Phrase("цена за единицу", "unit price", "einzelpreis"),
        Phrase("итоговая сумма", "total amount", "gesamtbetrag"),
        Phrase("статус заказа", "order status", "bestellstatus"),
        Phrase("оплачено", "paid", "bezahlt"),
        Phrase("в обработке", "processing", "in bearbeitung"),
        Phrase("доставлено", "delivered", "zugestellt")
    )

    private val DICTIONARY = mapOf(
        "текст" to mapOf("en" to "text", "de" to "text"),
        "экран" to mapOf("en" to "screen", "de" to "bildschirm"),
        "язык" to mapOf("en" to "language", "de" to "sprache"),
        "настройки" to mapOf("en" to "settings", "de" to "einstellungen"),
        "перевод" to mapOf("en" to "translation", "de" to "übersetzung"),
        "кнопка" to mapOf("en" to "button", "de" to "schaltfläche"),
        "документ" to mapOf("en" to "document", "de" to "dokument"),
        "таблица" to mapOf("en" to "table", "de" to "tabelle"),
        "статья" to mapOf("en" to "article", "de" to "artikel"),
        "данные" to mapOf("en" to "data", "de" to "daten"),
        "сообщение" to mapOf("en" to "message", "de" to "nachricht"),
        "время" to mapOf("en" to "time", "de" to "zeit"),
        "сумма" to mapOf("en" to "sum", "de" to "summe"),
        "статус" to mapOf("en" to "status", "de" to "status"),
        "офлайн" to mapOf("en" to "offline", "de" to "offline"),
        "быстрый" to mapOf("en" to "fast", "de" to "schnell"),
        "точный" to mapOf("en" to "accurate", "de" to "genau"),

        "text" to mapOf("ru" to "текст", "de" to "text"),
        "screen" to mapOf("ru" to "экран", "de" to "bildschirm"),
        "language" to mapOf("ru" to "язык", "de" to "sprache"),
        "settings" to mapOf("ru" to "настройки", "de" to "einstellungen"),
        "translation" to mapOf("ru" to "перевод", "de" to "übersetzung"),
        "button" to mapOf("ru" to "кнопка", "de" to "schaltfläche"),
        "document" to mapOf("ru" to "документ", "de" to "dokument"),
        "table" to mapOf("ru" to "таблица", "de" to "tabelle"),
        "article" to mapOf("ru" to "статья", "de" to "artikel"),
        "autonomous" to mapOf("ru" to "автономный", "de" to "autonom"),
        "system" to mapOf("ru" to "система", "de" to "system"),
        "operation" to mapOf("ru" to "работа", "de" to "betrieb"),
        "device" to mapOf("ru" to "устройство", "de" to "gerät"),
        "specifications" to mapOf("ru" to "характеристики", "de" to "spezifikationen"),
        "bundled" to mapOf("ru" to "встроенный", "de" to "integriert"),
        "pack" to mapOf("ru" to "пакет", "de" to "paket"),
        "recognition" to mapOf("ru" to "распознавание", "de" to "erkennung"),
        "mode" to mapOf("ru" to "режим", "de" to "modus"),
        "offline" to mapOf("ru" to "офлайн", "de" to "offline")
    )

    fun translate(text: String, srcLang: String, targetLang: String): String {
        if (text.isBlank() || srcLang == targetLang) return text

        val clean = text.trim().lowercase()

        // 1. Exact phrase match
        for (p in PHRASES) {
            val srcPhrase = when (srcLang) {
                "en" -> p.en
                "de" -> p.de
                else -> p.ru
            }
            if (clean == srcPhrase.lowercase()) {
                val res = when (targetLang) {
                    "en" -> p.en
                    "de" -> p.de
                    else -> p.ru
                }
                return res.replaceFirstChar { it.uppercase() }
            }
        }

        // 2. Tokenized dictionary match
        val words = text.split(Regex("(\\s+|[.,!?:;—–])"))
        val translated = words.map { token ->
            val lower = token.lowercase()
            val match = DICTIONARY[lower]?.get(targetLang)
            if (match != null) {
                if (token.isNotEmpty() && token[0].isUpperCase()) match.replaceFirstChar { it.uppercase() } else match
            } else {
                token
            }
        }

        val result = translated.joinToString(" ").replace(Regex("\\s+"), " ").trim()
        return if (result.isNotBlank()) result.replaceFirstChar { it.uppercase() } else text
    }
}
