package com.example

import com.example.search.NumberTextParser
import com.example.search.ScreenSnippet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenDetectionAccuracyUnitTest {

    @Test
    fun testAccurateNumberDetection_DetectsExactValue() {
        // Test exact detection of ₹2
        val items = NumberTextParser.extractNumbersAndPrices("Total Amount: ₹2", "hi", 100)
        assertEquals(1, items.size)
        assertEquals(2L, items[0].numericValue)
        assertEquals("₹", items[0].currencySymbol)
        assertEquals("Total Amount ₹2", items[0].label)

        // Test ₹2 without label
        val plainRupee = NumberTextParser.extractNumbersAndPrices("₹2", "hi", 80)
        assertEquals(1, plainRupee.size)
        assertEquals(2L, plainRupee[0].numericValue)
        assertEquals("₹", plainRupee[0].currencySymbol)
    }

    @Test
    fun testSystemUiIgnored_ClockAndBatteryNotDetected() {
        // Clock times must be completely ignored
        assertTrue(NumberTextParser.isSystemUiText("8:00"))
        assertTrue(NumberTextParser.isSystemUiText("08:00"))
        assertTrue(NumberTextParser.isSystemUiText("12:45 PM"))
        assertTrue(NumberTextParser.isSystemUiText("9:30 AM"))

        val timeItems = NumberTextParser.extractNumbersAndPrices("8:00", "hi", 40)
        assertTrue("Clock time must produce 0 detected numbers", timeItems.isEmpty())

        // Battery percentage must be completely ignored
        assertTrue(NumberTextParser.isSystemUiText("8%"))
        assertTrue(NumberTextParser.isSystemUiText("85%"))
        assertTrue(NumberTextParser.isSystemUiText("100%"))

        val batteryItems = NumberTextParser.extractNumbersAndPrices("8%", "hi", 30)
        assertTrue("Battery percentage must produce 0 detected numbers", batteryItems.isEmpty())

        // System indicators
        assertTrue(NumberTextParser.isSystemUiText("5G"))
        assertTrue(NumberTextParser.isSystemUiText("Wi-Fi"))
        assertTrue(NumberTextParser.isSystemUiText("120 KB/s"))
    }

    @Test
    fun testWordsDoNotFabricateRandomNumbers() {
        // Words like 'at', 'chat', 'data' must NOT match phonetic 'aath' (8) during screen scanning
        val itemsAt = NumberTextParser.extractNumbersAndPrices("Meeting at the office", "hi", 40, allowPhoneticFallback = false)
        assertTrue("Ordinary words must not produce random number 8", itemsAt.isEmpty())

        val itemsChat = NumberTextParser.extractNumbersAndPrices("Tap to chat", "hi", 40, allowPhoneticFallback = false)
        assertTrue(itemsChat.isEmpty())
    }

    @Test
    fun testFontSizeBasedPriority_LargerFontPrioritizedFirst() {
        val snippets = listOf(
            ScreenSnippet(text = "Order #842", visualFontSize = 32),
            ScreenSnippet(text = "₹2", visualFontSize = 140), // Prominent payment amount in huge font
            ScreenSnippet(text = "Delivery fee ₹40", visualFontSize = 45),
            ScreenSnippet(text = "8:00", visualFontSize = 28) // Status bar time
        )

        val detected = NumberTextParser.extractFromScreenSnippetsWithFontSizes(snippets, "hi")

        // 8:00 clock time must be skipped
        assertFalse(detected.any { it.numericValue == 8L && it.rawText.contains(":") })

        // The first and highest priority item MUST be the largest font element: ₹2 (visualFontSize 140)
        assertEquals(2L, detected[0].numericValue)
        assertEquals(140, detected[0].visualFontSize)
        assertEquals("₹2", detected[0].label)

        // Lower font size items come after
        assertTrue(detected[0].visualFontSize > detected[1].visualFontSize)
    }

    @Test
    fun testAmountTwoNeverDetectsEight() {
        // When amount ₹2 is displayed prominently on screen, it must detect exact ₹2, never ₹8
        val screenElements = listOf(
            ScreenSnippet(text = "8:00", visualFontSize = 24), // Status bar clock
            ScreenSnippet(text = "8%", visualFontSize = 24), // Status bar battery
            ScreenSnippet(text = "₹2", visualFontSize = 160), // Prominent payment amount
            ScreenSnippet(text = "Cashback upto ₹8", visualFontSize = 32) // Promo footer in tiny font
        )

        val detected = NumberTextParser.extractFromScreenSnippetsWithFontSizes(screenElements, "hi")
        assertEquals(2, detected.size) // Only ₹2 and ₹8 from promo, status bar time & battery 100% ignored

        // Top detected item must be the exact payment amount ₹2 because of largest visual font size
        assertEquals(2L, detected[0].numericValue)
        assertEquals("₹2", detected[0].label)
        assertEquals(160, detected[0].visualFontSize)
    }

    @Test
    fun testStatusBarIndicators_ComprehensiveFiltering() {
        assertTrue(NumberTextParser.isSystemUiText("8:00 AM"))
        assertTrue(NumberTextParser.isSystemUiText("8:05"))
        assertTrue(NumberTextParser.isSystemUiText("8 %"))
        assertTrue(NumberTextParser.isSystemUiText("Battery 8%"))
        assertTrue(NumberTextParser.isSystemUiText("8m ago"))
        assertTrue(NumberTextParser.isSystemUiText("8°C"))
        assertTrue(NumberTextParser.isSystemUiText("VoLTE"))
        assertTrue(NumberTextParser.isSystemUiText("2.4 KB/s"))
    }
}
