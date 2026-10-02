package com.example.autoclicker

object OfflineTranslationEngine {

    private data class Phrase(val ru: String, val en: String, val de: String)

    private val PHRASES = listOf(
        Phrase("экранный переводчик", "screen translator", "bildschirm übersetzer"),
        Phrase("экранный переводчик", "screentranslator", "bildschirmübersetzer"),
        Phrase("трансляция экрана", "screen cast", "bildschirmübertragung"),
        Phrase("трансляция экрана", "screencast", "bildschirmübertragung"),
        Phrase("скачанные приложения", "downloaded apps", "heruntergeladene apps"),
        Phrase("чтобы быть образованным нужно много учиться", "to be educated you need to study a lot", "um gebildet zu sein muss man viel lernen"),
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
        Phrase("доставлено", "delivered", "zugestellt"),
        Phrase("начать создание с медиа", "start creating with media", "beginnen sie mit der medienerstellung"),
        Phrase("запрещенная политика использования", "prohibited use policy", "richtlinie für unzulässige nutzung"),
        Phrase("условия использования", "terms of use", "nutzungsbedingungen"),
        Phrase("подтвердить", "acknowledge", "bestätigen"),
        Phrase("отмена", "cancel", "abbrechen")
    )

    private val DICTIONARY = mapOf(
        // Tech & UI Android Apps & Files
        "screen" to mapOf("ru" to "экран", "de" to "bildschirm"),
        "translator" to mapOf("ru" to "переводчик", "de" to "übersetzer"),
        "screentranslator" to mapOf("ru" to "экранный переводчик", "de" to "bildschirmübersetzer"),
        "cast" to mapOf("ru" to "трансляция", "de" to "übertragung"),
        "screencast" to mapOf("ru" to "трансляция экрана", "de" to "bildschirmübertragung"),
        "click" to mapOf("ru" to "клик", "de" to "klick"),
        "upwell" to mapOf("ru" to "апвелл", "de" to "upwell"),
        "upwellclick" to mapOf("ru" to "автокликер Upwell", "de" to "upwell-klicker"),
        "cloner" to mapOf("ru" to "клонер", "de" to "kloner"),
        "app" to mapOf("ru" to "приложение", "de" to "app"),
        "apps" to mapOf("ru" to "приложения", "de" to "apps"),
        "download" to mapOf("ru" to "загрузки", "de" to "downloads"),
        "downloads" to mapOf("ru" to "загрузки", "de" to "downloads"),
        "pictures" to mapOf("ru" to "изображения", "de" to "bilder"),
        "picture" to mapOf("ru" to "изображение", "de" to "bild"),
        "movies" to mapOf("ru" to "видео", "de" to "filme"),
        "movie" to mapOf("ru" to "видео", "de" to "film"),
        "alarms" to mapOf("ru" to "будильники", "de" to "wecker"),
        "alarm" to mapOf("ru" to "будильник", "de" to "wecker"),
        "documents" to mapOf("ru" to "документы", "de" to "dokumente"),
        "document" to mapOf("ru" to "документ", "de" to "dokument"),
        "recycler" to mapOf("ru" to "корзина", "de" to "papierkorb"),
        "manager" to mapOf("ru" to "диспетчер", "de" to "manager"),
        "file" to mapOf("ru" to "файл", "de" to "datei"),
        "files" to mapOf("ru" to "файлы", "de" to "dateien"),
        "installed" to mapOf("ru" to "установленные", "de" to "installiert"),
        "service" to mapOf("ru" to "служба", "de" to "dienst"),
        "accessibility" to mapOf("ru" to "специальные возможности", "de" to "barrierefreiheit"),
        "connection" to mapOf("ru" to "подключение", "de" to "verbindung"),
        "windows" to mapOf("ru" to "windows", "de" to "windows"),

        // Educational & Common
        "educated" to mapOf("ru" to "образованный", "de" to "gebildet"),
        "study" to mapOf("ru" to "учиться", "de" to "lernen"),
        "lot" to mapOf("ru" to "много", "de" to "viel"),
        "need" to mapOf("ru" to "нужно", "de" to "müssen"),
        "gebildet" to mapOf("ru" to "образованный", "en" to "educated"),
        "lernen" to mapOf("ru" to "учиться", "en" to "study"),
        "viel" to mapOf("ru" to "много", "en" to "lot"),
        "muss" to mapOf("ru" to "нужно", "en" to "must"),
        "образованный" to mapOf("en" to "educated", "de" to "gebildet"),
        "учиться" to mapOf("en" to "study", "de" to "lernen"),
        "много" to mapOf("en" to "a lot", "de" to "viel"),
        "нужно" to mapOf("en" to "need", "de" to "muss"),

        // UI & System
        "текст" to mapOf("en" to "text", "de" to "text"),
        "экран" to mapOf("en" to "screen", "de" to "bildschirm"),
        "язык" to mapOf("en" to "language", "de" to "sprache"),
        "настройки" to mapOf("en" to "settings", "de" to "einstellungen"),
        "перевод" to mapOf("en" to "translation", "de" to "übersetzung"),
        "кнопка" to mapOf("en" to "button", "de" to "schaltfläche"),
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
        "language" to mapOf("ru" to "язык", "de" to "sprache"),
        "settings" to mapOf("ru" to "настройки", "de" to "einstellungen"),
        "translation" to mapOf("ru" to "перевод", "de" to "übersetzung"),
        "button" to mapOf("ru" to "кнопка", "de" to "schaltfläche"),
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
        "offline" to mapOf("ru" to "офлайн", "de" to "offline"),
        "generating" to mapOf("ru" to "генерация", "de" to "generieren"),
        "start" to mapOf("ru" to "начать", "de" to "starten"),
        "creating" to mapOf("ru" to "создание", "de" to "erstellen"),
        "media" to mapOf("ru" to "медиа", "de" to "medien"),
        "cancel" to mapOf("ru" to "отмена", "de" to "abbrechen"),
        "acknowledge" to mapOf("ru" to "подтвердить", "de" to "bestätigen"),
        "policy" to mapOf("ru" to "политика", "de" to "richtlinie"),
        "terms" to mapOf("ru" to "условия", "de" to "bedingungen"),
        "rights" to mapOf("ru" to "права", "de" to "rechte"),
        "images" to mapOf("ru" to "изображения", "de" to "bilder"),
        "upload" to mapOf("ru" to "загрузка", "de" to "hochladen"),
        "content" to mapOf("ru" to "контент", "de" to "inhalt")
    )

    fun sanitizeOcrHomoglyphs(str: String): String {
        var cyrillicCount = 0
        var latinCount = 0
        for (ch in str) {
            val code = ch.code
            if (code in 0x0400..0x04FF) cyrillicCount++
            else if ((code in 65..90) || (code in 97..122)) latinCount++
        }

        // If predominantly Latin, replace OCR Cyrillic misreads with Latin equivalents
        if (latinCount > cyrillicCount && latinCount > 0) {
            return str
                .replace('п', 'n')
                .replace('П', 'N')
                .replace('т', 't')
                .replace('Т', 'T')
                .replace('р', 'p')
                .replace('Р', 'P')
                .replace('с', 'c')
                .replace('С', 'C')
                .replace('е', 'e')
                .replace('Е', 'E')
                .replace('а', 'a')
                .replace('А', 'A')
                .replace('о', 'o')
                .replace('О', 'O')
                .replace('х', 'x')
                .replace('Х', 'X')
                .replace('у', 'y')
                .replace('У', 'Y')
                .replace('і', 'i')
                .replace('І', 'I')
        } else if (cyrillicCount > latinCount && cyrillicCount > 0) {
            return str
                .replace('p', 'р')
                .replace('P', 'Р')
                .replace('c', 'с')
                .replace('C', 'С')
                .replace('e', 'е')
                .replace('E', 'Е')
                .replace('a', 'а')
                .replace('A', 'А')
                .replace('o', 'о')
                .replace('O', 'О')
                .replace('x', 'х')
                .replace('X', 'Х')
                .replace('y', 'у')
                .replace('Y', 'У')
        }
        return str
    }

    private fun normalizeForMatch(str: String): String {
        val sanitized = sanitizeOcrHomoglyphs(str)
        return sanitized.lowercase()
            .replace(Regex("[.,!?:;—–\"'()]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Splits CamelCase or compound words (e.g. "ScreenTranslator" -> ["Screen", "Translator"])
     */
    fun splitCompoundWord(word: String): List<String> {
        val clean = word.trim()
        if (clean.length < 4) return listOf(clean)

        // 1. CamelCase split
        val camelParts = clean.split(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
        if (camelParts.size > 1) {
            return camelParts.filter { it.isNotBlank() }
        }

        // 2. Sub-root split for lowercase compounds (e.g. "screentranslator" -> ["screen", "translator"])
        val lower = clean.lowercase()
        for ((key, _) in DICTIONARY) {
            if (key.length >= 3 && lower.startsWith(key) && lower.length > key.length) {
                val remainder = lower.substring(key.length)
                if (DICTIONARY.containsKey(remainder) || remainder.length >= 3) {
                    return listOf(key, remainder)
                }
            }
        }

        return listOf(clean)
    }

    fun translate(text: String, srcLang: String, targetLang: String): String {
        if (text.isBlank() || srcLang == targetLang) return text

        val sanitizedText = sanitizeOcrHomoglyphs(text)
        val normalizedInput = normalizeForMatch(sanitizedText)

        // 1. Exact phrase match
        for (p in PHRASES) {
            val srcPhraseNorm = when (srcLang) {
                "en" -> normalizeForMatch(p.en)
                "de" -> normalizeForMatch(p.de)
                else -> normalizeForMatch(p.ru)
            }
            if (normalizedInput == srcPhraseNorm) {
                val res = when (targetLang) {
                    "en" -> p.en
                    "de" -> p.de
                    else -> p.ru
                }
                return res.replaceFirstChar { it.uppercaseChar() }
            }
        }

        // 2. Sub-phrase match
        for (p in PHRASES) {
            val srcPhraseNorm = when (srcLang) {
                "en" -> normalizeForMatch(p.en)
                "de" -> normalizeForMatch(p.de)
                else -> normalizeForMatch(p.ru)
            }
            if (normalizedInput.contains(srcPhraseNorm)) {
                val res = when (targetLang) {
                    "en" -> p.en
                    "de" -> p.de
                    else -> p.ru
                }
                return res.replaceFirstChar { it.uppercaseChar() }
            }
        }

        // 3. Tokenized word-by-word with CamelCase / compound splitting
        val tokens = sanitizedText.split(Regex("(?<=[\\s.,!?:;—–()\"'/])|(?=[\\s.,!?:;—–()\"'/])"))
        val sb = StringBuilder()

        for (token in tokens) {
            val trimmed = token.trim()
            if (trimmed.isEmpty() || !trimmed.any { it.isLetter() }) {
                sb.append(token)
                continue
            }

            val lower = trimmed.lowercase()

            // Direct dictionary lookup
            val directMatch = DICTIONARY[lower]?.get(targetLang)
            if (directMatch != null) {
                val formatted = if (trimmed[0].isUpperCase()) directMatch.replaceFirstChar { it.uppercaseChar() } else directMatch
                sb.append(formatted)
                continue
            }

            // Compound / CamelCase lookup (e.g. ScreenTranslator, ScreenCast)
            val subParts = splitCompoundWord(trimmed)
            if (subParts.size > 1) {
                val subTranslations = subParts.map { part ->
                    val partMatch = DICTIONARY[part.lowercase()]?.get(targetLang)
                    if (partMatch != null) {
                        if (part[0].isUpperCase()) partMatch.replaceFirstChar { it.uppercaseChar() } else partMatch
                    } else {
                        part
                    }
                }
                sb.append(subTranslations.joinToString(" "))
            } else {
                sb.append(token)
            }
        }

        val result = sb.toString().replace(Regex("\\s+"), " ").trim()
        return if (result.isNotBlank()) result.replaceFirstChar { it.uppercaseChar() } else text
    }
}
