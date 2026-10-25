package com.example.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object SensitiveContentDetector {

    private val sensitiveKeywords = listOf(
        "password",
        "passcode",
        "pin",
        "cvv",
        "cvc",
        "otp",
        "card number",
        "credit card",
        "debit card",
        "expiry",
        "secret",
        "private key",
        "auth token"
    )

    fun isNodeSensitive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        // Check if node is marked as password
        if (node.isPassword) return true

        // Check text and content description
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        for (kw in sensitiveKeywords) {
            if (viewId.contains(kw)) return true
            if (contentDesc.contains(kw)) return true
            if (text.contains(kw)) return true
        }

        return false
    }

    fun sanitizeText(text: String): String {
        var clean = text
        // Mask sequences of 16 digits (likely card numbers)
        clean = clean.replace(Regex("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b"), "[PROTECTED CARD]")
        // Mask 3-4 digit CVV patterns
        clean = clean.replace(Regex("(?i)cvv\\s*[:=]?\\s*\\d{3,4}"), "CVV: [PROTECTED]")
        // Mask OTP codes
        clean = clean.replace(Regex("(?i)otp\\s*[:=]?\\s*\\d{4,8}"), "OTP: [PROTECTED]")
        return clean
    }
}
