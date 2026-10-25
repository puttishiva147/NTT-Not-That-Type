package com.example.language

data class NttLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val scriptName: String,
    val sampleScript: String,
    val samplePhonetic: String,
    val ttsLocaleTag: String
)

object SupportedLanguages {
    val HINDI = NttLanguage(
        code = "hi",
        englishName = "Hindi",
        nativeName = "हिन्दी",
        scriptName = "Devanagari",
        sampleScript = "पचपन",
        samplePhonetic = "Pachpan (55)",
        ttsLocaleTag = "hi-IN"
    )

    val MARATHI = NttLanguage(
        code = "mr",
        englishName = "Marathi",
        nativeName = "मराठी",
        scriptName = "Devanagari",
        sampleScript = "पंचावन्न",
        samplePhonetic = "Panchavanna (55)",
        ttsLocaleTag = "mr-IN"
    )

    val ENGLISH = NttLanguage(
        code = "en",
        englishName = "English",
        nativeName = "English",
        scriptName = "Latin",
        sampleScript = "Fifty-five",
        samplePhonetic = "Fifty-five (55)",
        ttsLocaleTag = "en-IN"
    )

    val GUJARATI = NttLanguage(
        code = "gu",
        englishName = "Gujarati",
        nativeName = "ગુજરાતી",
        scriptName = "Gujarati",
        sampleScript = "પંચાવન",
        samplePhonetic = "Panchavan (55)",
        ttsLocaleTag = "gu-IN"
    )

    val BENGALI = NttLanguage(
        code = "bn",
        englishName = "Bengali",
        nativeName = "বাংলা",
        scriptName = "Bengali",
        sampleScript = "পঞ্চান্ন",
        samplePhonetic = "Ponchanno (55)",
        ttsLocaleTag = "bn-IN"
    )

    val TAMIL = NttLanguage(
        code = "ta",
        englishName = "Tamil",
        nativeName = "தமிழ்",
        scriptName = "Tamil",
        sampleScript = "ஐம்பத்தைந்து",
        samplePhonetic = "Aimbaththeinthu (55)",
        ttsLocaleTag = "ta-IN"
    )

    val TELUGU = NttLanguage(
        code = "te",
        englishName = "Telugu",
        nativeName = "తెలుగు",
        scriptName = "Telugu",
        sampleScript = "యాభై ఐదు",
        samplePhonetic = "Yaabhai Aidu (55)",
        ttsLocaleTag = "te-IN"
    )

    val KANNADA = NttLanguage(
        code = "kn",
        englishName = "Kannada",
        nativeName = "ಕನ್ನಡ",
        scriptName = "Kannada",
        sampleScript = "ಐವತ್ತೈದು",
        samplePhonetic = "Aivattaidu (55)",
        ttsLocaleTag = "kn-IN"
    )

    val MALAYALAM = NttLanguage(
        code = "ml",
        englishName = "Malayalam",
        nativeName = "മലയാളം",
        scriptName = "Malayalam",
        sampleScript = "അമ്പത്തിയഞ്ച്",
        samplePhonetic = "Ambathiyanchu (55)",
        ttsLocaleTag = "ml-IN"
    )

    val PUNJABI = NttLanguage(
        code = "pa",
        englishName = "Punjabi",
        nativeName = "ਪੰਜਾਬੀ",
        scriptName = "Gurmukhi",
        sampleScript = "ਪਚਵੰਜਾ",
        samplePhonetic = "Pachvanja (55)",
        ttsLocaleTag = "pa-IN"
    )

    val ODIA = NttLanguage(
        code = "or",
        englishName = "Odia",
        nativeName = "ଓଡ଼ିଆ",
        scriptName = "Odia",
        sampleScript = "ପଞ୍ଚାବନ",
        samplePhonetic = "Panchabana (55)",
        ttsLocaleTag = "or-IN"
    )

    val ALL: List<NttLanguage> = listOf(
        HINDI,
        MARATHI,
        ENGLISH,
        GUJARATI,
        BENGALI,
        TAMIL,
        TELUGU,
        KANNADA,
        MALAYALAM,
        PUNJABI,
        ODIA
    )

    fun getByCode(code: String): NttLanguage {
        return ALL.find { it.code.equals(code, ignoreCase = true) } ?: HINDI
    }
}
