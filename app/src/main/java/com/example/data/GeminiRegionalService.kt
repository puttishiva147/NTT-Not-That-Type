package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.RegionalWordExplanation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRegionalService {

    private val tag = "GeminiRegionalService"
    // Using free-tier compatible Gemini 2.5 Flash which supports both text & multimodal audio
    private val modelName = "gemini-2.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * Translates and explains a regional word, phrase, or colloquial number from either
     * captured audio base64 or transcribed text using Gemini AI.
     */
    suspend fun explainRegionalWord(
        textQuery: String?,
        audioBase64: String?,
        languageHint: String = "Indian regional languages"
    ): Result<RegionalWordExplanation> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // If API key is not configured yet or empty, provide instant offline regional dictionary resolution
        if (!isApiKeyConfigured()) {
            val fallback = getOfflineExplanation(textQuery ?: "regional term")
            return@withContext Result.success(fallback.copy(isAiGenerated = false))
        }

        val systemPrompt = """
            You are NTT (Not That Type) AI, an expert linguist specializing in Indian regional languages (Hindi, Tamil, Telugu, Marathi, Kannada, Malayalam, Bengali, Gujarati, Punjabi, Hinglish, etc.), local slangs, colloquial numbers, market phrases, and cultural nuances.
            Analyze the user's spoken audio or word/phrase:
            1. Exact English translation & literal meaning.
            2. Language or dialect of origin.
            3. Romanized phonetic pronunciation.
            4. Detailed cultural context: how locals use it (e.g. in bargaining, street markets, friend circles, colloquial numbers like 'dedh' or 'dhai').
            5. Provide a practical example sentence in the regional language and its English translation.
            
            Respond ONLY with a valid, clean JSON object matching this schema:
            {
              "word": "original word in native script",
              "language": "Language/Dialect",
              "phonetic": "Romanized pronunciation guide",
              "englishTranslation": "Direct English translation",
              "explanation": "Clear definition and meaning breakdown",
              "culturalNuances": "How and when locals use it, tone, street/market context",
              "exampleSentence": "Sample regional sentence",
              "exampleTranslation": "English translation of sample sentence"
            }
            Do NOT include markdown formatting or backticks around the JSON.
        """.trimIndent()

        val jsonBody = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        val promptText = if (!textQuery.isNullOrBlank()) {
            "$systemPrompt\n\nTarget word or phrase: \"$textQuery\""
        } else {
            "$systemPrompt\n\nListen carefully to the attached microphone audio clip and explain the spoken regional word, number, or phrase."
        }

        partsArray.put(JSONObject().put("text", promptText))

        // If audio base64 is available, add inlineData part for direct multimodal analysis
        if (!audioBase64.isNullOrBlank()) {
            val inlineData = JSONObject()
                .put("mimeType", "audio/mp4")
                .put("data", audioBase64)
            partsArray.put(JSONObject().put("inlineData", inlineData))
        }

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        jsonBody.put("contents", contentsArray)

        val generationConfig = JSONObject().apply {
            put("temperature", 0.3)
            put("topP", 0.9)
            put("maxOutputTokens", 800)
            put("responseMimeType", "application/json")
        }
        jsonBody.put("generationConfig", generationConfig)

        val requestUrl = "$baseUrl?key=$apiKey"
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val rawResponse = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(tag, "Gemini API error code: ${response.code}, body: $rawResponse")
                // Check if offline fallback can satisfy query
                val fallback = getOfflineExplanation(textQuery ?: "regional term")
                return@withContext Result.success(fallback.copy(isAiGenerated = false))
            }

            val parsedResult = parseGeminiResponse(rawResponse, textQuery)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e(tag, "Gemini network/parsing exception: ${e.message}", e)
            val fallback = getOfflineExplanation(textQuery ?: "regional term")
            Result.success(fallback.copy(isAiGenerated = false))
        }
    }

    private fun parseGeminiResponse(rawJson: String, queryFallback: String?): RegionalWordExplanation {
        try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            // Clean any potential markdown code blocks
            val cleaned = textPart.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            return RegionalWordExplanation(
                word = json.optString("word", queryFallback ?: "Regional Term"),
                language = json.optString("language", "Indian Regional"),
                phonetic = json.optString("phonetic", queryFallback ?: ""),
                englishTranslation = json.optString("englishTranslation", "Understood meaning"),
                explanation = json.optString("explanation", "Spoken regional term understood by locals."),
                culturalNuances = json.optString("culturalNuances", "Commonly used in informal everyday conversations and market interactions."),
                exampleSentence = json.optString("exampleSentence", ""),
                exampleTranslation = json.optString("exampleTranslation", ""),
                isAiGenerated = true
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse JSON directly, attempting robust fallback: ${e.message}")
            return getOfflineExplanation(queryFallback ?: "regional term")
        }
    }

    /**
     * Rich built-in regional dictionary for Indian colloquialisms, slang, numbers, and market terms.
     */
    fun getOfflineExplanation(query: String): RegionalWordExplanation {
        val q = query.trim().lowercase()

        return when {
            q.contains("dedh") || q.contains("derh") -> RegionalWordExplanation(
                word = "डेढ़ (Dedh)",
                language = "Hindi / Urdu",
                phonetic = "Dey-dh",
                englishTranslation = "1.5 (One and a half)",
                explanation = "A fundamental Indian fractional multiplier representing 1½ times any base unit (e.g. 'Dedh sau' = 150, 'Dedh hazar' = 1,500, 'Dedh lakh' = 1,50,000).",
                culturalNuances = "Extremely ubiquitous in retail markets, vegetable vendors, auto-rickshaw fares, and colloquial daily speech across Northern and Central India.",
                exampleSentence = "भैया, डेढ़ सौ रुपये में दे दो। (Bhaiya, dedh sau rupaye mein de do.)",
                exampleTranslation = "Brother, please give it to me for 150 rupees.",
                isAiGenerated = false
            )
            q.contains("dhai") || q.contains("adhaee") -> RegionalWordExplanation(
                word = "ढाई (Dhai)",
                language = "Hindi / Punjabi",
                phonetic = "Dhaa-ee",
                englishTranslation = "2.5 (Two and a half)",
                explanation = "The colloquial regional multiplier for 2½ times a unit (e.g. 'Dhai sau' = 250, 'Dhai hazar' = 2,500, 'Dhai lakh' = 2,50,000).",
                culturalNuances = "Used constantly in street markets, salary negotiations, and time references (e.g. 'dhai baje' = 2:30).",
                exampleSentence = "ढाई हज़ार का बिल बना है। (Dhai hazaar ka bill bana hai.)",
                exampleTranslation = "The bill comes to 2,500 rupees.",
                isAiGenerated = false
            )
            q.contains("paune") || q.contains("paun") -> RegionalWordExplanation(
                word = "पौने (Paune)",
                language = "Hindi / Marathi",
                phonetic = "Pau-ney",
                englishTranslation = "Three-quarters / Minus a quarter (-0.25 of next number)",
                explanation = "Prefix indicating three-quarters or 15 minutes before the hour. 'Paune do' means 1.75 (or 1:45), 'Paune sau' means 75.",
                culturalNuances = "Crucial for everyday bargaining and time keeping. Often confuses non-natives because it refers to the upcoming number minus a quarter.",
                exampleSentence = "गाड़ी पौने तीन बजे आएगी। (Gaadi paune teen baje aayegi.)",
                exampleTranslation = "The train/vehicle will arrive at 2:45.",
                isAiGenerated = false
            )
            q.contains("sawa") -> RegionalWordExplanation(
                word = "सवा (Sawa)",
                language = "Hindi / Gujarati",
                phonetic = "Sa-vaa",
                englishTranslation = "1.25 / Plus a quarter (+0.25 of current number)",
                explanation = "Prefix meaning one and a quarter (1.25), or quarter past the hour. 'Sawa sau' = 125, 'Sawa do' = 2.25.",
                culturalNuances = "Considered auspicious in traditional Indian gifting (e.g., giving ₹1.25, ₹125, or ₹1,250 as 'shagun').",
                exampleSentence = "सवा सौ रुपये का प्रसाद ले लो। (Sawa sau rupaye ka prasad le lo.)",
                exampleTranslation = "Take holy offering worth 125 rupees.",
                isAiGenerated = false
            )
            q.contains("jugaad") || q.contains("jugad") -> RegionalWordExplanation(
                word = "जुगाड़ (Jugaad)",
                language = "Hindi / Punjabi Slang",
                phonetic = "Joo-gaad",
                englishTranslation = "Frugal innovation / Clever workaround",
                explanation = "A flexible, innovative, low-cost fix or clever hack that solves a complex problem with whatever resources are on hand.",
                culturalNuances = "A celebrated pillar of Indian grassroots ingenuity, admired in business, engineering, and daily lifestyle hacks.",
                exampleSentence = "चिंता मत करो, कोई न कोई जुगाड़ निकाल लेंगे।",
                exampleTranslation = "Don't worry, we will figure out a clever solution.",
                isAiGenerated = false
            )
            q.contains("chillar") || q.contains("chutta") -> RegionalWordExplanation(
                word = "चिल्लर / छुट्टा (Chillar / Chutta)",
                language = "Bambaiya Hindi / Marathi",
                phonetic = "Chil-lar / Chut-taa",
                englishTranslation = "Loose change / Small coins",
                explanation = "Denotes coins and small currency denominations below ₹10 or ₹20. Also used colloquially to mean trivial matter or petty people.",
                culturalNuances = "Heard daily in buses, tea stalls ('tapri'), and local train ticket counters: 'Bhaiya chillar nahi hai kya?'",
                exampleSentence = "बस कंडक्टर ने चिल्लर वापस नहीं दिया।",
                exampleTranslation = "The bus conductor didn't give back the loose change.",
                isAiGenerated = false
            )
            q.contains("rokda") || q.contains("rokh") -> RegionalWordExplanation(
                word = "रोकड़ा (Rokda)",
                language = "Mumbai Hindi Slang",
                phonetic = "Roke-daa",
                englishTranslation = "Hard cash / Liquid money",
                explanation = "Slang for physical cash money or immediate cash settlement.",
                culturalNuances = "Widely used in business discussions, wholesale trade, and Bollywood dialogue when insisting on cash over credit.",
                exampleSentence = "पूरा पेमेंट रोकड़े में चाहिए।",
                exampleTranslation = "I need the entire payment upfront in cash.",
                isAiGenerated = false
            )
            q.contains("khokha") -> RegionalWordExplanation(
                word = "खोखा (Khokha)",
                language = "Mumbai Underworld / Street Slang",
                phonetic = "Kho-khaa",
                englishTranslation = "One Crore Rupees (₹1,00,00,000)",
                explanation = "Colloquial Mumbai slang representing ₹1 Crore (10 million INR). Also pairs with 'Peti' (which means ₹1 Lakh).",
                culturalNuances = "Made famous by Indian cinema and street culture to discuss astronomical sums without saying 'crore'.",
                exampleSentence = "उस बंगले की कीमत कम से कम दो खोखा है।",
                exampleTranslation = "That bungalow costs at least two crore rupees.",
                isAiGenerated = false
            )
            q.contains("peti") -> RegionalWordExplanation(
                word = "पेटी (Peti)",
                language = "Mumbai Street Slang",
                phonetic = "Pey-tee",
                englishTranslation = "One Lakh Rupees (₹1,00,000)",
                explanation = "Colloquial term literally meaning 'box' or 'suitcase', representing exactly ₹1,00,000 (One Hundred Thousand INR).",
                culturalNuances = "Standard street shorthand in vehicle sales, real estate earnest money, and informal transactions.",
                exampleSentence = "गाड़ी का सौदा पांच पेटी में तय हुआ।",
                exampleTranslation = "The car deal was finalized for 5 lakh rupees.",
                isAiGenerated = false
            )
            q.contains("bindaas") || q.contains("bindas") -> RegionalWordExplanation(
                word = "बिंदास (Bindaas)",
                language = "Marathi / Bambaiya Hindi",
                phonetic = "Bin-daas",
                englishTranslation = "Carefree / Cool / Bold & Chill",
                explanation = "Describes an attitude of living freely without worries, hesitation, or fear of what others think.",
                culturalNuances = "A warm, positive compliment among youth and close friends across urban India.",
                exampleSentence = "तू बिंदास जा, सब ठीक हो जाएगा।",
                exampleTranslation = "You go ahead boldly with no worries, everything will be fine.",
                isAiGenerated = false
            )
            else -> RegionalWordExplanation(
                word = query.ifBlank { "क्षेत्रीय शब्द" },
                language = "Indian Regional Colloquial",
                phonetic = query.ifBlank { "Regional phrase" },
                englishTranslation = "Colloquial regional expression",
                explanation = "A colloquial phrase or numeral used in informal everyday settings, regional markets, or local dialogue.",
                culturalNuances = "Connect Gemini API key in AI Studio Secrets to unlock deep real-time linguistic explanations for any spoken phrase.",
                exampleSentence = "स्थानीय बोलचाल में इसका प्रयोग आम है।",
                exampleTranslation = "Its usage is common in local regional conversation.",
                isAiGenerated = false
            )
        }
    }
}
