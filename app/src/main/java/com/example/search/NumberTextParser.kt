package com.example.search

import android.graphics.Rect
import com.example.language.NumberTranslator
import com.example.language.UnderstoodResult
import java.util.regex.Pattern

data class DetectedItem(
    val label: String,
    val rawText: String,
    val numericValue: Long,
    val currencySymbol: String,
    val understoodResult: UnderstoodResult,
    val visualFontSize: Int = 0
)

data class ScreenSnippet(
    val text: String,
    val visualFontSize: Int = 0,
    val bounds: Rect = Rect()
)

object NumberTextParser {

    // Regex for numbers with optional currency prefix or surrounding context
    private val pricePattern = Pattern.compile(
        """(?:(₹|Rs\.?|INR)\s*)?(\d{1,3}(?:,\d{2,3})*(?:\.\d+)?|\d+)(?:\s*(rupaye|rupees|rs))?""",
        Pattern.CASE_INSENSITIVE
    )
    private val labeledNumberPattern = Pattern.compile(
        """([A-Za-z\s#]+)[:.]?\s*(₹|Rs\.?|INR)?\s*(\d{1,3}(?:,\d{2,3})*(?:\.\d+)?|\d+)""",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Checks if a string originates from Android System UI (Status Bar, Clock, Battery, Indicators).
     * Completely ignores time (e.g. 8:00), battery percentage (e.g. 8%, 85%), battery icons, and system indicators.
     */
    fun isSystemUiText(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return true

        // Clock time (e.g. 8:00, 08:00, 12:45, 9:30 AM, 7:15 PM, 14:30, 8:05)
        if (trimmed.matches(Regex("""^\d{1,2}[:.]\d{2}(?::\d{2})?\s*(?:[AaPp][Mm])?$"""))) return true
        if (trimmed.matches(Regex("""^\d{1,2}\s*[AaPp][Mm]$"""))) return true
        if (trimmed.contains(Regex("""\b\d{1,2}:\d{2}\s*(?:[AaPp][Mm])?\b"""))) return true

        // Battery percentage & battery status (e.g. 8%, 8 %, 85%, 100%, "Battery 8%")
        if (trimmed.matches(Regex("""^\d{1,3}\s*%$"""))) return true
        if (trimmed.contains(Regex("""\b\d{1,3}\s*%\b"""))) return true
        if (trimmed.contains(Regex("""\b\d{1,3}\s*percent\b""", RegexOption.IGNORE_CASE))) return true
        val lower = trimmed.lowercase()
        if (lower.contains("battery") || lower.contains("charging") || lower.contains("charged") || lower.contains("mah")) return true

        // Time elapsed stamps (e.g. "8 min ago", "2h ago", "5 days ago")
        if (lower.contains(Regex("""\b\d+\s*(?:s|sec|secs|m|min|mins|minute|minutes|h|hr|hrs|hour|hours|d|day|days)\s*ago\b"""))) return true

        // Weather temperature indicator (e.g. "8°", "8°C", "24°F")
        if (lower.contains(Regex("""\b\d+°\s*[cf]?\b"""))) return true

        // Date strings (e.g. 08/10/2026, 2026-10-08)
        if (trimmed.matches(Regex("""^\d{1,2}[/-]\d{1,2}[/-]\d{2,4}$""")) ||
            trimmed.matches(Regex("""^\d{4}[/-]\d{1,2}[/-]\d{1,2}$"""))) return true

        // Status bar icons and system telemetry
        if (lower in listOf(
                "5g", "4g", "3g", "2g", "lte", "volte", "wi-fi", "wifi", "vo wifi",
                "bluetooth", "hotspot", "nfc", "gps", "am", "pm",
                "emergency calls only", "no service", "no sim", "airplane mode", "flight mode",
                "silent", "vibrate", "do not disturb", "dnd"
            )
        ) return true

        if (lower.endsWith("kb/s") || lower.endsWith("mb/s") || lower.endsWith("gb/s") ||
            lower.endsWith("b/s") || lower.endsWith("k/s") || lower.endsWith("kbps") || lower.endsWith("mbps")) return true

        return false
    }

    fun extractNumbersAndPrices(
        rawText: String,
        langCode: String = "hi",
        visualFontSize: Int = 0,
        allowPhoneticFallback: Boolean = false
    ): List<DetectedItem> {
        if (isSystemUiText(rawText)) return emptyList()

        val detected = mutableListOf<DetectedItem>()
        val seenValues = mutableSetOf<Long>()

        // 1. Check for labeled phrases like "Total ₹2", "Order #103", "Amount: ₹450", "Pay ₹2"
        val labeledMatcher = labeledNumberPattern.matcher(rawText)
        while (labeledMatcher.find()) {
            val labelGroup = labeledMatcher.group(1)?.trim() ?: ""
            val currGroup = labeledMatcher.group(2) ?: ""
            val numberGroup = labeledMatcher.group(3) ?: ""
            val cleanNumStr = numberGroup.replace(",", "").substringBefore(".").trim()
            val numVal = cleanNumStr.toLongOrNull()

            if (numVal != null && numVal in 0..999999999L && !seenValues.contains(numVal)) {
                val hasCurrency = currGroup.isNotBlank() ||
                        labelGroup.contains("total", ignoreCase = true) ||
                        labelGroup.contains("fare", ignoreCase = true) ||
                        labelGroup.contains("bill", ignoreCase = true) ||
                        labelGroup.contains("price", ignoreCase = true) ||
                        labelGroup.contains("amount", ignoreCase = true) ||
                        labelGroup.contains("pay", ignoreCase = true)
                val curr = if (currGroup.isNotBlank()) "₹" else if (hasCurrency) "₹" else ""
                val label = if (labelGroup.isNotBlank() && labelGroup.length < 30) "$labelGroup $curr$cleanNumStr" else "$curr$cleanNumStr"

                val res = NumberTranslator.translateNumber(numVal, langCode, curr)
                detected.add(
                    DetectedItem(
                        label = label,
                        rawText = labeledMatcher.group(0) ?: "$curr$cleanNumStr",
                        numericValue = numVal,
                        currencySymbol = curr,
                        understoodResult = res,
                        visualFontSize = visualFontSize
                    )
                )
                seenValues.add(numVal)
            }
        }

        // 2. Check general price/number matches (e.g. "₹2", "Rs 450", "200")
        val priceMatcher = pricePattern.matcher(rawText)
        while (priceMatcher.find()) {
            val start = priceMatcher.start(2)
            val end = priceMatcher.end(2)

            // Strictly ignore numbers adjacent to colon (clock time like 8:00 or 8:30) or percent (battery like 8% or 8 %)
            val isPrecededByColon = start > 0 && (rawText[start - 1] == ':' || (start > 1 && rawText[start - 2] == ':' && rawText[start - 1].isWhitespace()))
            val isFollowedByColon = end < rawText.length && (rawText[end] == ':' || (end + 1 < rawText.length && rawText[end].isWhitespace() && rawText[end + 1] == ':'))
            val isFollowedByPercent = end < rawText.length && (rawText[end] == '%' || (end + 1 < rawText.length && rawText[end].isWhitespace() && rawText[end + 1] == '%'))
            val isTimeContext = rawText.contains(Regex("""\b\d{1,2}:\d{2}\b""")) || rawText.contains(Regex("""\b\d{1,2}\s*[AaPp][Mm]\b"""))

            if (isPrecededByColon || isFollowedByColon || isFollowedByPercent || isTimeContext) {
                continue
            }

            val currPrefix = priceMatcher.group(1) ?: ""
            val numberGroup = priceMatcher.group(2) ?: ""
            val currSuffix = priceMatcher.group(3) ?: ""
            val cleanNumStr = numberGroup.replace(",", "").substringBefore(".").trim()
            val numVal = cleanNumStr.toLongOrNull()

            if (numVal != null && numVal in 0..999999999L && !seenValues.contains(numVal)) {
                val hasCurrency = currPrefix.isNotBlank() || currSuffix.isNotBlank()
                val curr = if (hasCurrency) "₹" else ""
                val label = if (curr.isNotBlank()) "$curr$cleanNumStr" else cleanNumStr

                val res = NumberTranslator.translateNumber(numVal, langCode, curr)
                detected.add(
                    DetectedItem(
                        label = label,
                        rawText = priceMatcher.group(0) ?: "$curr$cleanNumStr",
                        numericValue = numVal,
                        currencySymbol = curr,
                        understoodResult = res,
                        visualFontSize = visualFontSize
                    )
                )
                seenValues.add(numVal)
            }
        }

        // 3. Phonetic fallback ONLY when explicitly requested (e.g. for typing/voice queries, NEVER for screen detection)
        if (allowPhoneticFallback && detected.isEmpty()) {
            val phoneticMatches = PhoneticMatcher.search(rawText, langCode)
            if (phoneticMatches.isNotEmpty()) {
                val top = phoneticMatches.first()
                detected.add(
                    DetectedItem(
                        label = top.result.primaryNumber,
                        rawText = rawText,
                        numericValue = top.result.numericValue,
                        currencySymbol = top.result.currencySymbol,
                        understoodResult = top.result,
                        visualFontSize = visualFontSize
                    )
                )
            }
        }

        return detected.take(8)
    }

    /**
     * Extracts numbers with visual font size priority.
     * Prioritizes larger-font text elements (e.g. primary payment amount ₹2) over smaller text elements.
     * Preserves exact text and numbers without alteration.
     */
    fun extractFromScreenSnippetsWithFontSizes(
        snippets: List<ScreenSnippet>,
        langCode: String = "hi"
    ): List<DetectedItem> {
        if (snippets.isEmpty()) return emptyList()

        val results = mutableListOf<DetectedItem>()

        // 1. Process individual snippets
        for (snippet in snippets) {
            if (isSystemUiText(snippet.text)) continue
            val items = extractNumbersAndPrices(
                rawText = snippet.text,
                langCode = langCode,
                visualFontSize = snippet.visualFontSize,
                allowPhoneticFallback = false // Never guess random numbers on screen
            )
            for (item in items) {
                val existingIndex = results.indexOfFirst { it.numericValue == item.numericValue }
                if (existingIndex < 0) {
                    results.add(item)
                } else {
                    // If already present, retain the one with larger visual font size!
                    if (item.visualFontSize > results[existingIndex].visualFontSize) {
                        results[existingIndex] = item
                    }
                }
            }
        }

        // 2. Process adjacent pairs of snippets (common when currency symbol or label is in a sibling view)
        // Verify spatial proximity: only pair if visually on the same line and adjacent
        for (i in 0 until snippets.size - 1) {
            val s1 = snippets[i]
            val s2 = snippets[i + 1]
            if (isSystemUiText(s1.text) || isSystemUiText(s2.text)) continue

            // Spatial check: only combine if horizontally aligned and near each other
            val areSpatiallyAdjacent = if (!s1.bounds.isEmpty && !s2.bounds.isEmpty) {
                val vDiff = Math.abs(s1.bounds.centerY() - s2.bounds.centerY())
                val hDiff = Math.abs(s2.bounds.left - s1.bounds.right)
                vDiff <= maxOf(s1.bounds.height(), s2.bounds.height(), 40) * 1.5 && hDiff < 200
            } else {
                true
            }
            if (!areSpatiallyAdjacent) continue

            val combined = "${s1.text} ${s2.text}"
            val maxFont = maxOf(s1.visualFontSize, s2.visualFontSize)
            val items = extractNumbersAndPrices(
                rawText = combined,
                langCode = langCode,
                visualFontSize = maxFont,
                allowPhoneticFallback = false
            )
            for (item in items) {
                val existingIndex = results.indexOfFirst { it.numericValue == item.numericValue }
                if (existingIndex < 0) {
                    results.add(item)
                } else {
                    if (item.visualFontSize > results[existingIndex].visualFontSize) {
                        results[existingIndex] = item
                    }
                }
            }
        }

        // Font-Size-Based Priority: Give highest detection priority to text in larger fonts on screen.
        // When multiple text elements are present, prioritize the most prominent, larger-font text over smaller text.
        // Use actual visual font size to determine priority, not numerical value or text length.
        results.sortWith(
            compareByDescending<DetectedItem> { it.visualFontSize }
                .thenByDescending { it.currencySymbol.isNotBlank() }
        )

        return results.take(8)
    }

    /**
     * Backward-compatible helper for plain string list.
     */
    fun extractFromScreenSnippets(snippets: List<String>, langCode: String = "hi"): List<DetectedItem> {
        val wrapped = snippets.map { ScreenSnippet(text = it, visualFontSize = 0) }
        return extractFromScreenSnippetsWithFontSizes(wrapped, langCode)
    }
}
