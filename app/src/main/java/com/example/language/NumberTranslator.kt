package com.example.language

data class UnderstoodResult(
    val primaryNumber: String,
    val numericValue: Long,
    val currencySymbol: String,
    val languageCode: String,
    val languageName: String,
    val scriptText: String,
    val phoneticText: String,
    val englishMeaning: String,
    val breakdown: String = ""
)

object NumberTranslator {

    fun translateNumber(
        number: Long,
        langCode: String = "hi",
        currencySymbol: String = ""
    ): UnderstoodResult {
        val lang = SupportedLanguages.getByCode(langCode)
        val formattedNum = IndianNumberFormatter.formatIndianNumber(number, currencySymbol)

        val (scriptWords, phoneticWords) = buildRegionalWords(number, lang.code)
        val englishWords = buildEnglishWords(number)

        val currencyWordRegional = LanguageDictionary.getCurrencyWord(lang.code, currencySymbol)
        val currencyWordEnglish = LanguageDictionary.getCurrencyWord("en", currencySymbol)

        val finalScript = if (currencySymbol.isNotBlank() && currencyWordRegional.script.isNotBlank()) {
            "$scriptWords ${currencyWordRegional.script}".trim()
        } else {
            scriptWords
        }

        val finalPhonetic = if (currencySymbol.isNotBlank() && currencyWordRegional.phonetic.isNotBlank()) {
            "$phoneticWords ${currencyWordRegional.phonetic.lowercase()}".trim()
        } else {
            phoneticWords
        }

        val finalEnglish = if (currencySymbol.isNotBlank() && currencyWordEnglish.script.isNotBlank()) {
            "$englishWords ${currencyWordEnglish.script.lowercase()}".trim()
        } else {
            englishWords
        }

        val breakdown = buildBreakdown(number)

        return UnderstoodResult(
            primaryNumber = formattedNum,
            numericValue = number,
            currencySymbol = currencySymbol,
            languageCode = lang.code,
            languageName = lang.englishName,
            scriptText = finalScript,
            phoneticText = finalPhonetic,
            englishMeaning = finalEnglish,
            breakdown = breakdown
        )
    }

    private fun buildRegionalWords(number: Long, langCode: String): Pair<String, String> {
        if (number in 0..100) {
            val direct = LanguageDictionary.getWordData(langCode, number.toInt())
            if (direct != null) {
                return Pair(direct.script, direct.phonetic)
            }
        }

        if (number == 0L) {
            val zero = LanguageDictionary.getWordData(langCode, 0)
            return Pair(zero?.script ?: "शून्य", zero?.phonetic ?: "Shunya")
        }

        var rem = number
        val scriptParts = mutableListOf<String>()
        val phoneticParts = mutableListOf<String>()

        // Crores (1,00,00,000)
        if (rem >= 10000000L) {
            val croreVal = rem / 10000000L
            val (cScript, cPhonetic) = buildRegionalWords(croreVal, langCode)
            val scale = LanguageDictionary.getScaleWord(langCode, 10000000L)
            scriptParts.add("$cScript ${scale.script}".trim())
            phoneticParts.add("$cPhonetic ${scale.phonetic}".trim())
            rem %= 10000000L
        }

        // Lakhs (1,00,000)
        if (rem >= 100000L) {
            val lakhVal = rem / 100000L
            val (lScript, lPhonetic) = buildRegionalWords(lakhVal, langCode)
            val scale = LanguageDictionary.getScaleWord(langCode, 100000L)
            scriptParts.add("$lScript ${scale.script}".trim())
            phoneticParts.add("$lPhonetic ${scale.phonetic}".trim())
            rem %= 100000L
        }

        // Thousands (1,000)
        if (rem >= 1000L) {
            val thousandVal = rem / 1000L
            val (tScript, tPhonetic) = buildRegionalWords(thousandVal, langCode)
            val scale = LanguageDictionary.getScaleWord(langCode, 1000L)
            scriptParts.add("$tScript ${scale.script}".trim())
            phoneticParts.add("$tPhonetic ${scale.phonetic}".trim())
            rem %= 1000L
        }

        // Hundreds (100)
        if (rem >= 100L) {
            val hundredVal = rem / 100L
            val (hScript, hPhonetic) = buildRegionalWords(hundredVal, langCode)
            val scale = LanguageDictionary.getScaleWord(langCode, 100L)
            scriptParts.add("$hScript ${scale.script}".trim())
            phoneticParts.add("$hPhonetic ${scale.phonetic}".trim())
            rem %= 100L
        }

        // Remainder 1-99
        if (rem > 0) {
            val direct = LanguageDictionary.getWordData(langCode, rem.toInt())
            if (direct != null) {
                scriptParts.add(direct.script)
                phoneticParts.add(direct.phonetic)
            } else {
                scriptParts.add(rem.toString())
                phoneticParts.add(rem.toString())
            }
        }

        return Pair(
            scriptParts.joinToString(" "),
            phoneticParts.joinToString(" ")
        )
    }

    private fun buildEnglishWords(number: Long): String {
        if (number in 0..100) {
            return LanguageDictionary.getEnglishWord(number.toInt()).script
        }

        var rem = number
        val parts = mutableListOf<String>()

        if (rem >= 10000000L) {
            val crores = rem / 10000000L
            parts.add("${buildEnglishWords(crores)} Crore")
            rem %= 10000000L
        }
        if (rem >= 100000L) {
            val lakhs = rem / 100000L
            parts.add("${buildEnglishWords(lakhs)} Lakh")
            rem %= 100000L
        }
        if (rem >= 1000L) {
            val thousands = rem / 1000L
            parts.add("${buildEnglishWords(thousands)} Thousand")
            rem %= 1000L
        }
        if (rem >= 100L) {
            val hundreds = rem / 100L
            parts.add("${buildEnglishWords(hundreds)} Hundred")
            rem %= 100L
        }
        if (rem > 0) {
            if (rem <= 100) {
                parts.add(LanguageDictionary.getEnglishWord(rem.toInt()).script)
            }
        }

        return parts.joinToString(" ")
    }

    private fun buildBreakdown(number: Long): String {
        if (number <= 10) return ""
        val parts = mutableListOf<String>()
        var rem = number
        if (rem >= 10000000L) {
            val v = (rem / 10000000L) * 10000000L
            parts.add(IndianNumberFormatter.formatIndianNumber(v))
            rem %= 10000000L
        }
        if (rem >= 100000L) {
            val v = (rem / 100000L) * 100000L
            parts.add(IndianNumberFormatter.formatIndianNumber(v))
            rem %= 100000L
        }
        if (rem >= 1000L) {
            val v = (rem / 1000L) * 1000L
            parts.add(IndianNumberFormatter.formatIndianNumber(v))
            rem %= 1000L
        }
        if (rem >= 100L) {
            val v = (rem / 100L) * 100L
            parts.add(IndianNumberFormatter.formatIndianNumber(v))
            rem %= 100L
        }
        if (rem >= 10L) {
            val tens = (rem / 10L) * 10L
            val units = rem % 10L
            if (units != 0L) {
                parts.add(tens.toString())
                parts.add(units.toString())
            } else {
                parts.add(tens.toString())
            }
        } else if (rem > 0L) {
            parts.add(rem.toString())
        }
        return if (parts.size > 1) parts.joinToString(" + ") else ""
    }
}
