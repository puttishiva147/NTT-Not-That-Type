package com.example.model

/**
 * Detailed explanation and translation of a regional word, slang, or number
 * powered by Gemini AI with offline fallback.
 */
data class RegionalWordExplanation(
    val word: String,
    val language: String,
    val phonetic: String,
    val englishTranslation: String,
    val explanation: String,
    val culturalNuances: String = "",
    val exampleSentence: String = "",
    val exampleTranslation: String = "",
    val isAiGenerated: Boolean = true,
    val audioBase64: String? = null
)
