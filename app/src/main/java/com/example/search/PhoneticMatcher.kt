package com.example.search

import com.example.language.LanguageDictionary
import com.example.language.NumberTranslator
import com.example.language.UnderstoodResult
import kotlin.math.max
import kotlin.math.min

data class SearchMatch(
    val result: UnderstoodResult,
    val confidence: Float,
    val matchedTerm: String,
    val isExact: Boolean
)

object PhoneticMatcher {

    // Common Indian phonetic phrases & colloquial terms
    private val colloquialPhrases = mapOf(
        "dedh sau" to 150L,
        "dedhso" to 150L,
        "dedh so" to 150L,
        "dhai sau" to 250L,
        "dhaiso" to 250L,
        "dhai so" to 250L,
        "paune do sau" to 175L,
        "paune teen sau" to 275L,
        "paune chaar sau" to 375L,
        "sawa sau" to 125L,
        "sawa do sau" to 225L,
        "sawa teen sau" to 325L,
        "sadhe teen sau" to 350L,
        "sadhe char sau" to 450L,
        "sadhe chaar sau" to 450L,
        "sadhe panch sau" to 550L,
        "dedh hazar" to 1500L,
        "dedh hazaar" to 1500L,
        "dhai hazar" to 2500L,
        "dhai hazaar" to 2500L,
        "pandrah sau" to 1500L,
        "solah sau" to 1600L,
        "satrah sau" to 1700L,
        "atharah sau" to 1800L,
        "unnees sau" to 1900L,
        "bees sau" to 2000L,
        "pachees sau" to 2500L
    )

    private val allPhoneticEntries: List<Triple<String, Long, String>> by lazy {
        val list = mutableListOf<Triple<String, Long, String>>()

        // Hindi
        LanguageDictionary.hindiWords.forEach { (num, wordData) ->
            list.add(Triple(normalize(wordData.phonetic), num.toLong(), "hi"))
            list.add(Triple(wordData.script, num.toLong(), "hi"))
        }

        // Marathi
        LanguageDictionary.marathiWords.forEach { (num, wordData) ->
            list.add(Triple(normalize(wordData.phonetic), num.toLong(), "mr"))
            list.add(Triple(wordData.script, num.toLong(), "mr"))
        }

        // English
        (0..100).forEach { num ->
            val w = LanguageDictionary.getEnglishWord(num)
            list.add(Triple(normalize(w.phonetic), num.toLong(), "en"))
        }

        // Add colloquial forms
        colloquialPhrases.forEach { (phrase, num) ->
            list.add(Triple(normalize(phrase), num, "hi"))
        }

        list
    }

    fun search(query: String, defaultLangCode: String = "hi"): List<SearchMatch> {
        val clean = query.trim()
        if (clean.isBlank()) return emptyList()

        val normalized = normalize(clean)

        // Check if clean query is a direct number (e.g. "55", "₹55", "103")
        val directNumber = extractDirectNumber(clean)
        if (directNumber != null) {
            val result = NumberTranslator.translateNumber(
                number = directNumber.first,
                langCode = defaultLangCode,
                currencySymbol = directNumber.second
            )
            return listOf(
                SearchMatch(
                    result = result,
                    confidence = 1.0f,
                    matchedTerm = clean,
                    isExact = true
                )
            )
        }

        // Check colloquial phrase match
        for ((phrase, num) in colloquialPhrases) {
            if (normalized.contains(normalize(phrase))) {
                val hasCurrency = hasCurrencyKeyword(clean)
                val curr = if (hasCurrency) "₹" else ""
                val res = NumberTranslator.translateNumber(num, defaultLangCode, curr)
                return listOf(
                    SearchMatch(
                        result = res,
                        confidence = 0.98f,
                        matchedTerm = phrase,
                        isExact = true
                    )
                )
            }
        }

        val hasCurrency = hasCurrencyKeyword(clean)
        val currencySymbol = if (hasCurrency) "₹" else ""

        // Strip currency words for matching
        val searchWord = stripCurrencyWords(normalized)

        val matches = mutableListOf<SearchMatch>()
        val seenNumbers = mutableSetOf<Long>()

        for ((storedTerm, number, lang) in allPhoneticEntries) {
            if (seenNumbers.contains(number)) continue

            val similarity = calculateSimilarity(searchWord, storedTerm)
            if (similarity >= 0.65f || storedTerm.contains(searchWord) || searchWord.contains(storedTerm)) {
                val score = if (storedTerm == searchWord) {
                    1.0f
                } else if (storedTerm.startsWith(searchWord) || searchWord.startsWith(storedTerm)) {
                    0.88f
                } else {
                    similarity
                }

                val res = NumberTranslator.translateNumber(
                    number = number,
                    langCode = if (lang == "en") defaultLangCode else lang,
                    currencySymbol = currencySymbol
                )

                matches.add(
                    SearchMatch(
                        result = res,
                        confidence = score,
                        matchedTerm = storedTerm,
                        isExact = score >= 0.95f
                    )
                )
                seenNumbers.add(number)
            }
        }

        // Sort by confidence descending
        return matches.sortedByDescending { it.confidence }.take(6)
    }

    private fun extractDirectNumber(text: String): Pair<Long, String>? {
        val hasRupee = text.contains("₹") || text.contains("rs", ignoreCase = true) || text.contains("inr", ignoreCase = true)
        val digits = text.filter { it.isDigit() }
        if (digits.isNotBlank() && digits.length <= 15) {
            val num = digits.toLongOrNull()
            if (num != null) {
                return Pair(num, if (hasRupee) "₹" else "")
            }
        }
        return null
    }

    private fun hasCurrencyKeyword(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("₹") || lower.contains("rs") || lower.contains("rupaye") ||
                lower.contains("rupee") || lower.contains("rupiya") || lower.contains("taka") ||
                lower.contains("tanka") || lower.contains("roobai")
    }

    private fun stripCurrencyWords(text: String): String {
        return text.replace("rupaye", "")
            .replace("rupees", "")
            .replace("rupee", "")
            .replace("rupiya", "")
            .replace("taka", "")
            .replace("tanka", "")
            .replace("rs", "")
            .replace("inr", "")
            .trim()
    }

    fun normalize(text: String): String {
        return text.lowercase()
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("sh", "s")
            .replace("w", "v")
            .replace("ph", "f")
            .replace("bh", "b")
            .replace("dh", "d")
            .replace("th", "t")
            .replace("chh", "ch")
            .replace("-", " ")
            .replace(".", "")
            .replace(",", "")
            .trim()
    }

    private fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f

        val dist = levenshteinDistance(s1, s2)
        val maxLen = max(s1.length, s2.length)
        return 1.0f - (dist.toFloat() / maxLen.toFloat())
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}
