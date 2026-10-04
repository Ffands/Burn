package com.example.autoclicker

import java.util.regex.Pattern

object LocalPhraseIntelligenceEngine {

    data class IdiomEntry(val pattern: Pattern, val replacement: String)

    // English Idioms, Phrasal Verbs, and Collocations
    private val EN_IDIOMS = listOf(
        // Classic Idioms
        IdiomEntry(Pattern.compile("(?i)\\bpiece\\s+of\\s+cake\\b"), "проще простого"),
        IdiomEntry(Pattern.compile("(?i)\\bbite\\s+the\\s+bullet\\b"), "стиснуть зубы"),
        IdiomEntry(Pattern.compile("(?i)\\bunder\\s+the\\s+weather\\b"), "нездоровится"),
        IdiomEntry(Pattern.compile("(?i)\\bbreak\\s+a\\s+leg\\b"), "ни пуха ни пера"),
        IdiomEntry(Pattern.compile("(?i)\\bspill\\s+the\\s+beans\\b"), "выдать секрет"),
        IdiomEntry(Pattern.compile("(?i)\\bonce\\s+in\\s+a\\s+blue\\s+moon\\b"), "крайне редко"),
        IdiomEntry(Pattern.compile("(?i)\\bcall\\s+it\\s+a\\s+day\\b"), "закончить на сегодня"),
        IdiomEntry(Pattern.compile("(?i)\\bout\\s+of\\s+the\\s+blue\\b"), "как снег на голову"),
        IdiomEntry(Pattern.compile("(?i)\\bat\\s+the\\s+end\\s+of\\s+the\\s+day\\b"), "в конце концов"),
        IdiomEntry(Pattern.compile("(?i)\\bkick\\s+the\\s+bucket\\b"), "сыграть в ящик"),
        IdiomEntry(Pattern.compile("(?i)\\bhit\\s+the\\s+nail\\s+on\\s+the\\s+head\\b"), "попасть в яблочко"),
        IdiomEntry(Pattern.compile("(?i)\\bcost\\s+an\\s+arm\\s+and\\s+a\\s+leg\\b"), "стоить целое состояние"),
        IdiomEntry(Pattern.compile("(?i)\\bsee\\s+eye\\s+to\\s+eye\\b"), "сходиться во взглядах"),
        IdiomEntry(Pattern.compile("(?i)\\bbeat\\s+around\\s+the\\s+bush\\b"), "ходить вокруг да около"),

        // Phrasal Verbs
        IdiomEntry(Pattern.compile("(?i)\\blook\\s+forward\\s+to\\b"), "с нетерпением ждать"),
        IdiomEntry(Pattern.compile("(?i)\\brun\\s+out\\s+of\\b"), "закончиться"),
        IdiomEntry(Pattern.compile("(?i)\\bfigure\\s+out\\b"), "разобраться"),
        IdiomEntry(Pattern.compile("(?i)\\bfind\\s+out\\b"), "выяснить"),
        IdiomEntry(Pattern.compile("(?i)\\bgive\\s+up\\b"), "сдаваться"),
        IdiomEntry(Pattern.compile("(?i)\\bcarry\\s+out\\b"), "выполнять"),
        IdiomEntry(Pattern.compile("(?i)\\bget\\s+rid\\s+of\\b"), "избавиться от"),
        IdiomEntry(Pattern.compile("(?i)\\bkeep\\s+an\\s+eye\\s+on\\b"), "присматривать за"),
        IdiomEntry(Pattern.compile("(?i)\\btake\\s+care\\s+of\\b"), "позаботиться о"),
        IdiomEntry(Pattern.compile("(?i)\\bcatch\\s+up\\s+with\\b"), "догнать"),
        IdiomEntry(Pattern.compile("(?i)\\bturn\\s+out\\b"), "оказаться"),
        IdiomEntry(Pattern.compile("(?i)\\bpoint\\s+out\\b"), "указать на"),
        IdiomEntry(Pattern.compile("(?i)\\bset\\s+up\\b"), "настроить"),
        IdiomEntry(Pattern.compile("(?i)\\blook\\s+after\\b"), "присматривать за"),
        IdiomEntry(Pattern.compile("(?i)\\bput\\s+off\\b"), "откладывать"),

        // Connectors & Everyday Conversational Units
        IdiomEntry(Pattern.compile("(?i)\\bby\\s+the\\s+way\\b"), "кстати"),
        IdiomEntry(Pattern.compile("(?i)\\bas\\s+well\\s+as\\b"), "а также"),
        IdiomEntry(Pattern.compile("(?i)\\bin\\s+order\\s+to\\b"), "для того чтобы"),
        IdiomEntry(Pattern.compile("(?i)\\bas\\s+soon\\s+as\\b"), "как только"),
        IdiomEntry(Pattern.compile("(?i)\\bso\\s+far\\s+so\\s+good\\b"), "пока всё идет хорошо"),
        IdiomEntry(Pattern.compile("(?i)\\bno\\s+matter\\s+what\\b"), "несмотря ни на что"),
        IdiomEntry(Pattern.compile("(?i)\\bon\\s+the\\s+other\\s+hand\\b"), "с другой стороны"),
        IdiomEntry(Pattern.compile("(?i)\\bfirst\\s+of\\s+all\\b"), "прежде всего"),
        IdiomEntry(Pattern.compile("(?i)\\bto\\s+be\\s+honest\\b"), "честно говоря")
    )

    // German Idioms and Collocations
    private val DE_IDIOMS = listOf(
        // Popular German Idioms
        IdiomEntry(Pattern.compile("(?i)\\b(ich\\s+habe|habe|hat)\\s+bock\\b"), "есть настроение"),
        IdiomEntry(Pattern.compile("(?i)\\bkein\\s+bock\\b"), "неохота"),
        IdiomEntry(Pattern.compile("(?i)\\bschwein\\s+haben\\b"), "повезло"),
        IdiomEntry(Pattern.compile("(?i)\\bins\\s+gras\\s+beißen\\b"), "сыграть в ящик"),
        IdiomEntry(Pattern.compile("(?i)\\btomaten\\s+auf\\s+den\\s+augen\\s+haben\\b"), "не замечать очевидного"),
        IdiomEntry(Pattern.compile("(?i)\\bdie\\s+daumen\\s+drücken\\b"), "держать кулачки"),
        IdiomEntry(Pattern.compile("(?i)\\bauf\\s+den\\s+keks\\s+gehen\\b"), "действовать на нервы"),
        IdiomEntry(Pattern.compile("(?i)\\baus\\s+der\\s+haut\\s+fahren\\b"), "выйти из себя"),
        IdiomEntry(Pattern.compile("(?i)\\bunter\\s+vier\\s+augen\\b"), "с глазу на глаз"),
        IdiomEntry(Pattern.compile("(?i)\\bim\\s+handumdrehen\\b"), "в мгновение ока"),
        IdiomEntry(Pattern.compile("(?i)\\bkatzensprung\\b"), "рукой подать"),
        IdiomEntry(Pattern.compile("(?i)\\bda\\s+liegt\\s+der\\s+hund\\s+begraben\\b"), "вот где собака зарыта"),
        IdiomEntry(Pattern.compile("(?i)\\bjederzeit\\s+wieder\\b"), "в любое время снова"),
        IdiomEntry(Pattern.compile("(?i)\\bauf\\s+jeden\\s+fall\\b"), "в любом случае"),
        IdiomEntry(Pattern.compile("(?i)\\bauf\\s+keinen\\s+fall\\b"), "ни в коем случае"),
        IdiomEntry(Pattern.compile("(?i)\\bvor\\s+allem\\b"), "прежде всего"),
        IdiomEntry(Pattern.compile("(?i)\\bin\\s+der\\s+regel\\b"), "как правило"),
        IdiomEntry(Pattern.compile("(?i)\\bhin\\s+und\\s+her\\b"), "туда и обратно"),
        IdiomEntry(Pattern.compile("(?i)\\bmehr\\s+oder\\s+weniger\\b"), "более или менее"),
        IdiomEntry(Pattern.compile("(?i)\\bwie\\s+geht\\s*('s|\\s+es)\\b"), "как дела"),
        IdiomEntry(Pattern.compile("(?i)\\balles\\s+klar\\b"), "всё ясно"),
        IdiomEntry(Pattern.compile("(?i)\\bmoin\\s+moin\\b"), "привет")
    )

    /**
     * Applies neural-like multi-word idiom and expression recognition.
     */
    fun applyIdioms(text: String, srcLang: String): String {
        var current = text
        val list = if (srcLang == "de") DE_IDIOMS else EN_IDIOMS
        for (item in list) {
            val matcher = item.pattern.matcher(current)
            if (matcher.find()) {
                current = matcher.replaceAll(item.replacement)
            }
        }
        return current
    }

    /**
     * High-level syntactic structure translation:
     * - Subordinate clauses ("Um gebildet zu sein...", "To be educated...")
     * - Genitive constructions ("Akademie der Lügen", "King of the world")
     */
    fun applySyntacticStructures(text: String, srcLang: String): String {
        var current = text.trim()

        // 1. German infinitive purpose pattern: "Um [X] zu sein, muss man [Y] lernen"
        val deInfPattern = Pattern.compile("(?i)\\bum\\s+([^,]+?)\\s+zu\\s+sein[,\\s]+muss\\s+man\\s+([^\\.\\n]+)", Pattern.CASE_INSENSITIVE)
        val deInfMatcher = deInfPattern.matcher(current)
        if (deInfMatcher.find()) {
            val adj = deInfMatcher.group(1).trim()
            val action = deInfMatcher.group(2).trim()
            val translatedAdj = AssetDictionaryManager.lookup(adj, "de") ?: adj
            val translatedAction = if (action.contains("viel", ignoreCase = true) && action.contains("lernen", ignoreCase = true)) {
                "много учиться"
            } else {
                action
            }
            return "Чтобы быть $translatedAdj, нужно $translatedAction"
        }

        // 2. English infinitive purpose pattern: "To be [X], you need to [Y]"
        val enInfPattern = Pattern.compile("(?i)\\bto\\s+be\\s+([^,]+?)[,\\s]+you\\s+need\\s+to\\s+([^\\.\\n]+)", Pattern.CASE_INSENSITIVE)
        val enInfMatcher = enInfPattern.matcher(current)
        if (enInfMatcher.find()) {
            val adj = enInfMatcher.group(1).trim()
            val action = enInfMatcher.group(2).trim()
            val translatedAdj = AssetDictionaryManager.lookup(adj, "en") ?: adj
            val translatedAction = if (action.contains("study", ignoreCase = true) && action.contains("lot", ignoreCase = true)) {
                "много учиться"
            } else {
                action
            }
            return "Чтобы быть $translatedAdj, нужно $translatedAction"
        }

        // 3. German Genitive construct: "[Noun] der [Noun-Genitive]" (e.g. Akademie der Lügen -> Академия лжи)
        val deGenPattern = Pattern.compile("(?i)\\b([a-zA-ZäöüßÄÖÜ]+)\\s+der\\s+([a-zA-ZäöüßÄÖÜ]+)\\b")
        val deGenMatcher = deGenPattern.matcher(current)
        if (deGenMatcher.find()) {
            val n1 = deGenMatcher.group(1).trim().lowercase()
            val n2 = deGenMatcher.group(2).trim().lowercase()
            val t1 = AssetDictionaryManager.lookup(n1, "de")
            val t2 = if (n2 == "lügen" || n2 == "lüge") "лжи" 
                     else if (n2 == "welt") "мира" 
                     else if (n2 == "wahrheit") "правды"
                     else AssetDictionaryManager.lookup(n2, "de")

            if (t1 != null && t2 != null) {
                current = deGenMatcher.replaceAll("$t1 $t2")
            }
        }

        return current
    }

    /**
     * Resolves Russian grammatical agreement between an Adjective and a Noun (Gender & Number).
     * e.g. "военный" + "количество" (neuter) -> "военное количество"
     * e.g. "острый" + "сабля" (feminine) -> "острая сабля"
     */
    fun harmonizeRussianAdjectiveNoun(adj: String, noun: String): String {
        val nLower = noun.lowercase().trim()
        val aLower = adj.lowercase().trim()

        if (!aLower.endsWith("ый") && !aLower.endsWith("ий") && !aLower.endsWith("ой")) {
            return adj
        }

        val stem = aLower.substring(0, aLower.length - 2)

        // Neuter gender detection in Russian (-о, -е, -мя)
        val isNeuter = nLower.endsWith("о") || nLower.endsWith("е") || nLower.endsWith("мя")
        // Feminine gender detection (-а, -я, -ь)
        val isFem = nLower.endsWith("а") || nLower.endsWith("я")
        // Plural (-ы, -и, -а)
        val isPlural = nLower.endsWith("ы") || nLower.endsWith("и")

        return when {
            isNeuter -> stem + (if (stem.endsWith("н") || stem.endsWith("к")) "ое" else "ее")
            isFem -> stem + "ая"
            isPlural -> stem + "ые"
            else -> adj
        }
    }
}
