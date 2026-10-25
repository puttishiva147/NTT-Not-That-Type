package com.example.speech

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.language.UnderstoodResult
import com.example.overlay.LiveScreenHighlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

val LocalNttTtsManager = staticCompositionLocalOf<NttTtsManager?> { null }

/**
 * Robust Android TextToSpeech manager designed for reading back identified numbers,
 * currency prices, and regional word meanings to learners and daily users.
 */
class NttTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val GOOGLE_TTS_ENGINE = "com.google.android.tts"

        @Volatile
        private var INSTANCE: NttTtsManager? = null

        fun getInstance(context: Context): NttTtsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NttTtsManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Helper to construct clear, natural spoken text for an identified number or price.
         * Speaks: "[Formatted Number/Price]. [English Meaning]. [Regional Pronunciation]. [Breakdown/Slang]."
         */
        fun buildIdentifiedNumberSpeech(result: UnderstoodResult): String {
            val formatted = result.primaryNumber
            val eng = result.englishMeaning
            val regional = if (result.phoneticText.isNotBlank()) result.phoneticText else result.scriptText

            return buildString {
                append(formatted)
                append(". ")
                append(eng)
                append(". ")
                append(regional)
                if (result.breakdown.isNotBlank()) {
                    append(". ")
                    append(result.breakdown)
                }
            }.trim()
        }

        /**
         * Helper to construct speech for live screen detected numbers.
         */
        fun buildHighlightSpeech(highlight: LiveScreenHighlight): String {
            return buildString {
                append(highlight.displayLabel)
                append(". ")
                append(highlight.wordsTranslation)
                if (!highlight.slangTerm.isNullOrBlank()) {
                    append(". ")
                    append(highlight.slangTerm)
                }
            }.trim()
        }
    }

    private var tts: TextToSpeech? = null
    var isInitialized: Boolean = false
        private set
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _currentlyPlayingText = MutableStateFlow<String?>(null)
    val currentlyPlayingText: StateFlow<String?> = _currentlyPlayingText.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var currentUtteranceId: String? = null
    private var pendingSpeech: Pair<String, String>? = null

    var currentSpeechRate: Float = 0.92f
        private set
    var currentPitch: Float = 1.0f
        private set

    init {
        INSTANCE = this
        initTts()
    }

    private fun initTts() {
        tts = try {
            TextToSpeech(context, this, GOOGLE_TTS_ENGINE)
        } catch (e: Exception) {
            TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupUtteranceListener()
            tts?.language = parseLocaleFromTag("hi-IN")
            tts?.setSpeechRate(currentSpeechRate)
            tts?.setPitch(currentPitch)

            // Execute any pending speech queued while engine was initializing
            pendingSpeech?.let { (text, langTag) ->
                pendingSpeech = null
                speak(text, langTag)
            }
        } else {
            // Fallback to system default TTS engine
            tts = try {
                TextToSpeech(context) { fallbackStatus ->
                    if (fallbackStatus == TextToSpeech.SUCCESS) {
                        isInitialized = true
                        setupUtteranceListener()
                        tts?.language = parseLocaleFromTag("hi-IN")
                        tts?.setSpeechRate(currentSpeechRate)
                        tts?.setPitch(currentPitch)

                        pendingSpeech?.let { (text, langTag) ->
                            pendingSpeech = null
                            speak(text, langTag)
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    _isPlaying.value = true
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (currentUtteranceId == utteranceId) {
                        _currentlyPlayingText.value = null
                        _isPlaying.value = false
                        currentUtteranceId = null
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    if (currentUtteranceId == utteranceId) {
                        _currentlyPlayingText.value = null
                        _isPlaying.value = false
                        currentUtteranceId = null
                    }
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    if (currentUtteranceId == utteranceId) {
                        _currentlyPlayingText.value = null
                        _isPlaying.value = false
                        currentUtteranceId = null
                    }
                }
            }
        })
    }

    /**
     * Reads back an identified number, price, and translation to the user.
     */
    fun speakIdentifiedNumber(result: UnderstoodResult, langTag: String? = null) {
        val targetTag = langTag ?: "${result.languageCode}-IN"
        val speechText = buildIdentifiedNumberSpeech(result)
        speak(speechText, targetTag)
    }

    /**
     * Reads back an identified price to the user in a natural, clear manner.
     */
    fun speakPrice(
        formattedAmount: String,
        englishWords: String,
        regionalWords: String,
        langTag: String = "hi-IN"
    ) {
        val speechText = "$formattedAmount. $englishWords. $regionalWords."
        speak(speechText, langTag)
    }

    /**
     * Reads back the identified regional word or slang and its explanation/meaning.
     */
    fun speakWordMeaning(
        word: String,
        phonetic: String = "",
        englishMeaning: String,
        explanation: String? = null,
        exampleSentence: String? = null,
        langTag: String = "hi-IN"
    ) {
        val parts = mutableListOf<String>()
        parts.add(word)
        if (phonetic.isNotBlank() && !phonetic.equals(word, ignoreCase = true)) {
            parts.add("pronounced $phonetic")
        }
        parts.add("means $englishMeaning")
        if (!explanation.isNullOrBlank()) {
            parts.add(explanation)
        }
        if (!exampleSentence.isNullOrBlank()) {
            parts.add("Example: $exampleSentence")
        }
        val speechText = parts.joinToString(". ")
        speak(speechText, langTag)
    }

    /**
     * Reads back a live on-screen highlight item.
     */
    fun speakHighlight(highlight: LiveScreenHighlight, langTag: String = "hi-IN") {
        val speechText = buildHighlightSpeech(highlight)
        speak(speechText, langTag)
    }

    /**
     * Toggles playback: stops if this exact text is currently playing; otherwise speaks it.
     */
    fun toggleSpeak(text: String, langTag: String = "hi-IN") {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        if (_isPlaying.value && _currentlyPlayingText.value == trimmed) {
            stop()
        } else {
            speak(trimmed, langTag)
        }
    }

    /**
     * Speaks the given text using the Android TextToSpeech engine.
     */
    fun speak(text: String, langTag: String = "hi-IN", speechRate: Float? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        if (!isInitialized || tts == null) {
            pendingSpeech = Pair(trimmed, langTag)
            initTts()
            return
        }

        try {
            stop() // Stop previous speech cleanly

            val locale = parseLocaleFromTag(langTag)
            val langResult = tts?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackLocale = parseLocaleFromTag(locale.language)
                val fallbackResult = tts?.setLanguage(fallbackLocale)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = parseLocaleFromTag("hi-IN")
                }
            }

            selectBestVoiceForLocale(locale)

            val rateToApply = speechRate ?: currentSpeechRate
            tts?.setSpeechRate(rateToApply)
            tts?.setPitch(currentPitch)

            val utteranceId = "NTT_${System.currentTimeMillis()}_${trimmed.hashCode()}"
            currentUtteranceId = utteranceId
            _currentlyPlayingText.value = trimmed
            _isPlaying.value = true

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(trimmed, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            _currentlyPlayingText.value = null
            _isPlaying.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
        try {
            tts?.setSpeechRate(currentSpeechRate)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        try {
            tts?.setPitch(currentPitch)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun selectBestVoiceForLocale(targetLocale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val availableVoices = tts?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val matchingVoice = availableVoices
                        .filter { voice ->
                            voice.locale.language.equals(targetLocale.language, ignoreCase = true)
                        }
                        .maxByOrNull { voice ->
                            var score = 0
                            if (voice.locale.country.equals(targetLocale.country, ignoreCase = true)) score += 5
                            if (voice.name.contains("google", ignoreCase = true) || voice.name.contains("neural", ignoreCase = true)) score += 10
                            if (voice.quality >= Voice.QUALITY_HIGH) score += 4
                            if (!voice.isNetworkConnectionRequired) score += 2
                            score
                        }
                    if (matchingVoice != null) {
                        tts?.voice = matchingVoice
                    }
                }
            } catch (e: Exception) {
                // Ignore and use default voice
            }
        }
    }

    private fun parseLocaleFromTag(langTag: String): Locale {
        return try {
            val tag = langTag.replace("_", "-")
            val parts = tag.split("-")
            when {
                parts.size >= 2 -> Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
                parts.isNotEmpty() -> Locale.Builder().setLanguage(parts[0]).build()
                else -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
            }
        } catch (e: Exception) {
            Locale.Builder().setLanguage("hi").setRegion("IN").build()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // ignore
        }
        _currentlyPlayingText.value = null
        _isPlaying.value = false
        currentUtteranceId = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        if (INSTANCE == this) {
            INSTANCE = null
        }
    }
}
