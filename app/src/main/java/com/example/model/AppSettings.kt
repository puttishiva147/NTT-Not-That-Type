package com.example.model

import android.content.Context
import android.content.SharedPreferences

data class AppSettings(
    val defaultLanguageCode: String = "hi",
    val isFloatingButtonEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val isHistoryEnabled: Boolean = true,
    val understandNumbers: Boolean = true,
    val understandCurrency: Boolean = true,
    val understandVoice: Boolean = true,
    val understandOcr: Boolean = true,
    val sensitiveProtectionEnabled: Boolean = true,
    val glassIntensity: Float = 0.85f,
    val floatingBarDurationMinutes: Int = 0 // 0 = Always Display (Never Hide), 5, 10, 15, 30
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ntt_preferences", Context.MODE_PRIVATE)

    fun getSettings(): AppSettings {
        return AppSettings(
            defaultLanguageCode = prefs.getString("default_lang", "hi") ?: "hi",
            isFloatingButtonEnabled = prefs.getBoolean("floating_btn_enabled", true),
            isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            isHistoryEnabled = prefs.getBoolean("history_enabled", true),
            understandNumbers = prefs.getBoolean("understand_numbers", true),
            understandCurrency = prefs.getBoolean("understand_currency", true),
            understandVoice = prefs.getBoolean("understand_voice", true),
            understandOcr = prefs.getBoolean("understand_ocr", true),
            sensitiveProtectionEnabled = prefs.getBoolean("sensitive_protection", true),
            glassIntensity = prefs.getFloat("glass_intensity", 0.85f),
            floatingBarDurationMinutes = prefs.getInt("floating_bar_duration_minutes", 0)
        )
    }

    fun setFloatingBarDurationMinutes(minutes: Int) {
        prefs.edit().putInt("floating_bar_duration_minutes", minutes).apply()
    }

    fun setDefaultLanguage(langCode: String) {
        prefs.edit().putString("default_lang", langCode).apply()
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("floating_btn_enabled", enabled).apply()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun setHistoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("history_enabled", enabled).apply()
    }

    fun updateToggle(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
