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
        Phrase("мне в кайф", "i'm feeling it", "ich habe bock"),
        Phrase("нет настроения", "not in the mood", "kein bock"),
        Phrase("привет привет", "hello hello", "moin moin"),
        Phrase("привет", "hello", "moin"),
        Phrase("тоска по дальним странам", "wanderlust", "fernweh"),
        Phrase("кавардак", "mess", "kuddelmuddel"),
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
        // German Slang & Colloquial
        "quasi" to mapOf("ru" to "типа", "en" to "quasi"),
        "jepp" to mapOf("ru" to "да", "en" to "yep"),
        "jau" to mapOf("ru" to "ага", "en" to "yeah"),
        "toll" to mapOf("ru" to "круто", "en" to "great"),
        "geil" to mapOf("ru" to "классно", "en" to "cool"),
        "bock" to mapOf("ru" to "кайф", "en" to "desire"),
        "klamotten" to mapOf("ru" to "шмотки", "en" to "clothes"),
        "moin" to mapOf("ru" to "привет", "en" to "hi"),
        "fernweh" to mapOf("ru" to "тяга к путешествиям", "en" to "wanderlust"),
        "kuddelmuddel" to mapOf("ru" to "кавардак", "en" to "mess"),
        "haben" to mapOf("ru" to "иметь", "en" to "have"),
        "sein" to mapOf("ru" to "быть", "en" to "be"),
        "nicht" to mapOf("ru" to "не", "en" to "not"),
        "und" to mapOf("ru" to "и", "en" to "and"),
        "oder" to mapOf("ru" to "или", "en" to "or"),
        "aber" to mapOf("ru" to "но", "en" to "but"),

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
        "viewer" to mapOf("ru" to "просмотрщик", "de" to "betrachter"),
        "installed" to mapOf("ru" to "установленные", "de" to "installiert"),
        "service" to mapOf("ru" to "служба", "de" to "dienst"),
        "accessibility" to mapOf("ru" to "спецвозможности", "de" to "barrierefreiheit"),
        "connection" to mapOf("ru" to "подключение", "de" to "verbindung"),
        "windows" to mapOf("ru" to "windows", "de" to "windows"),

        // Educational & Common English / German
        "educated" to mapOf("ru" to "образованный", "de" to "gebildet"),
        "study" to mapOf("ru" to "учиться", "de" to "lernen"),
        "lot" to mapOf("ru" to "много", "de" to "viel"),
        "need" to mapOf("ru" to "нужно", "de" to "müssen"),
        "gebildet" to mapOf("ru" to "образованный", "en" to "educated"),
        "lernen" to mapOf("ru" to "учиться", "en" to "study"),
        "viel" to mapOf("ru" to "много", "en" to "lot"),
        "muss" to mapOf("ru" to "нужно", "en" to "must"),
        "man" to mapOf("ru" to "нужно", "en" to "one"),
        "zu" to mapOf("ru" to "к", "en" to "to"),
        "um" to mapOf("ru" to "чтобы", "en" to "to"),
        "to" to mapOf("ru" to "чтобы", "de" to "zu"),
        "be" to mapOf("ru" to "быть", "de" to "sein"),
        "you" to mapOf("ru" to "вы", "de" to "du"),

        // UI & System
        "text" to mapOf("ru" to "текст", "de" to "text"),
        "settings" to mapOf("ru" to "настройки", "de" to "einstellungen"),
        "translation" to mapOf("ru" to "перевод", "de" to "übersetzung"),
        "button" to mapOf("ru" to "кнопка", "de" to "schaltfläche"),
        "table" to mapOf("ru" to "таблица", "de" to "tabelle"),
        "article" to mapOf("ru" to "статья", "de" to "artikel"),
        "data" to mapOf("ru" to "данные", "de" to "daten"),
        "message" to mapOf("ru" to "сообщение", "de" to "nachricht"),
        "time" to mapOf("ru" to "время", "de" to "zeit"),
        "sum" to mapOf("ru" to "сумма", "de" to "summe"),
        "status" to mapOf("ru" to "статус", "de" to "status"),
        "offline" to mapOf("ru" to "офлайн", "de" to "offline"),
        "start" to mapOf("ru" to "начать", "de" to "starten"),
        "creating" to mapOf("ru" to "создание", "de" to "erstellen"),
        "media" to mapOf("ru" to "медиа", "de" to "medien"),
        "cancel" to mapOf("ru" to "отмена", "de" to "abbrechen"),
        "acknowledge" to mapOf("ru" to "подтвердить", "de" to "bestätigen")
    )

    /**
     * Cleans OCR character misclassifications at word level.
     * If a word contains Latin letters, any Cyrillic homoglyphs inside it (like 'п' for 'n')
     * are converted to proper Latin.
     */
    fun sanitizeOcrHomoglyphs(str: String): String {
        if (str.isEmpty()) return str

        val tokens = str.split(Regex("(?<=[\\s.,!?:;—–()\"'/])|(?=[\\s.,!?:;—–()\"'/])"))
        val sb = StringBuilder()

        for (t in tokens) {
            val trimmed = t.trim()
            if (trimmed.isEmpty()) {
                sb.append(t)
                continue
            }

            var latin = 0
            var cyrillic = 0
            for (ch in trimmed) {
                val code = ch.code
                if (code in 0x0400..0x04FF) cyrillic++
                else if ((code in 65..90) || (code in 97..122) || (ch in "äöüßÄÖÜ")) latin++
            }

            // Word has Latin characters mixed with Cyrillic lookalikes
            if (latin >= 1 && cyrillic >= 1) {
                if (latin >= cyrillic) {
                    val fixed = trimmed
                        .replace('п', 'n').replace('П', 'N')
                        .replace('т', 't').replace('Т', 'T')
                        .replace('р', 'p').replace('Р', 'P')
                        .replace('с', 'c').replace('С', 'C')
                        .replace('е', 'e').replace('Е', 'E')
                        .replace('а', 'a').replace('А', 'A')
                        .replace('о', 'o').replace('О', 'O')
                        .replace('х', 'x').replace('Х', 'X')
                        .replace('у', 'y').replace('У', 'Y')
                        .replace('і', 'i').replace('І', 'I')
                    sb.append(fixed)
                    continue
                }
            }
            sb.append(t)
        }
        return sb.toString()
    }

    private fun normalizeForMatch(str: String): String {
        return str.lowercase()
            .replace(Regex("[.,!?:;—–\"'()]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun splitCompoundWord(word: String): List<String> {
        val clean = word.trim()
        if (clean.length < 4) return listOf(clean)

        // 1. CamelCase split
        val camelParts = clean.split(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
        if (camelParts.size > 1) {
            return camelParts.filter { it.isNotBlank() }
        }

        // 2. Sub-root split for lowercase compounds
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

    /**
     * Translates phrases, UI tokens, and sentences cleanly into targetLang.
     * NEVER produces broken slash-separated word salads!
     */
    fun translate(text: String, srcLang: String, targetLang: String): String {
        if (text.isBlank()) return text

        val sanitizedText = sanitizeOcrHomoglyphs(text)
        val normalizedInput = normalizeForMatch(sanitizedText)

        // 1. Full phrase match (e.g. "To be educated you need to study a lot", "Um gebildet zu sein...")
        for (p in PHRASES) {
            val srcPhraseNorm = when (srcLang) {
                "en" -> normalizeForMatch(p.en)
                "de" -> normalizeForMatch(p.de)
                else -> normalizeForMatch(p.ru)
            }
            if (normalizedInput == srcPhraseNorm || normalizedInput == normalizeForMatch(p.en) || normalizedInput == normalizeForMatch(p.de)) {
                val res = when (targetLang) {
                    "en" -> p.en
                    "de" -> p.de
                    else -> p.ru
                }
                return res.replaceFirstChar { it.uppercaseChar() }
            }
        }

        // 2. Sub-phrase inline replacement for known idioms (e.g. "ich habe bock", "moin moin")
        var workingText = sanitizedText
        for (p in PHRASES) {
            val phraseToSearch = when {
                targetLang == "ru" -> listOf(p.de, p.en)
                targetLang == "en" -> listOf(p.ru, p.de)
                else -> listOf(p.ru, p.en)
            }
            val replacement = when (targetLang) {
                "en" -> p.en
                "de" -> p.de
                else -> p.ru
            }

            for (phraseCandidate in phraseToSearch) {
                if (phraseCandidate.length >= 5) {
                    val regex = Regex("(?i)\\b${Regex.escape(phraseCandidate)}\\b")
                    if (regex.containsMatchIn(workingText)) {
                        workingText = regex.replace(workingText, replacement)
                    }
                }
            }
        }

        // German Genitive / Preposition phrases like "Akademie der Lügen"
        if (targetLang == "ru") {
            workingText = workingText.replace(Regex("(?i)\\bder\\s+lügen\\b"), "лжи")
            workingText = workingText.replace(Regex("(?i)\\bdes\\s+lebens\\b"), "жизни")
            workingText = workingText.replace(Regex("(?i)\\bder\\s+welt\\b"), "мира")
        }

        // 3. Comprehensive Dictionary & Lemmatizer translation for words and sentences
        val tokens = workingText.split(Regex("(?<=[\\s.,!?:;—–()\"'/])|(?=[\\s.,!?:;—–()\"'/])"))
        val sb = StringBuilder()

        for (token in tokens) {
            val trimmed = token.trim()
            if (trimmed.isEmpty() || !trimmed.any { it.isLetter() }) {
                sb.append(token)
                continue
            }

            // If token is already Cyrillic, keep it
            val hasLatin = trimmed.any { (it in 'a'..'z') || (it in 'A'..'Z') || (it in "äöüßÄÖÜ") }
            if (targetLang == "ru" && !hasLatin) {
                sb.append(token)
                continue
            }

            val lower = trimmed.lowercase()

            // 1. Comprehensive Dictionary lookup (English & German with stemming)
            val compMatch = if (srcLang == "de") {
                ComprehensiveDictionary.lookupGerman(lower) ?: ComprehensiveDictionary.lookupEnglish(lower)
            } else {
                ComprehensiveDictionary.lookupEnglish(lower) ?: ComprehensiveDictionary.lookupGerman(lower)
            }

            if (compMatch != null) {
                val formatted = if (trimmed[0].isUpperCase()) compMatch.replaceFirstChar { it.uppercaseChar() } else compMatch
                sb.append(formatted)
                continue
            }

            // 2. Direct dictionary lookup
            val directMatch = DICTIONARY[lower]?.get(targetLang)
            if (directMatch != null) {
                val formatted = if (trimmed[0].isUpperCase()) directMatch.replaceFirstChar { it.uppercaseChar() } else directMatch
                sb.append(formatted)
                continue
            }

            // 3. Compound / CamelCase lookup (e.g. ScreenTranslator, Skullgirls)
            val subParts = splitCompoundWord(trimmed)
            if (subParts.size > 1) {
                val subTranslations = subParts.map { part ->
                    val pMatch = ComprehensiveDictionary.lookupEnglish(part.lowercase())
                        ?: ComprehensiveDictionary.lookupGerman(part.lowercase())
                        ?: DICTIONARY[part.lowercase()]?.get(targetLang)
                    if (pMatch != null) {
                        if (part[0].isUpperCase()) pMatch.replaceFirstChar { it.uppercaseChar() } else pMatch
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
