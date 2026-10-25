package com.example.overlay

import android.content.Context
import android.content.SharedPreferences
import com.example.accessibility.NttAccessibilityService
import com.example.language.NumberTranslator
import com.example.language.UnderstoodResult
import com.example.search.DetectedItem
import com.example.search.NumberTextParser
import com.example.search.PhoneticMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveScreenHighlight(
    val id: String,
    val rawText: String,
    val numericValue: Long,
    val displayLabel: String,
    val wordsTranslation: String,
    val slangTerm: String? = null,
    val boundsLeft: Int = 0,
    val boundsTop: Int = 0,
    val boundsRight: Int = 0,
    val boundsBottom: Int = 0,
    val visualFontSize: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class OverlayUiState(
    val isOverlayActive: Boolean = true,
    // The bar is hidden by default and only appears when a selected app is active in the foreground
    val isButtonVisible: Boolean = false,
    val isForegroundAppSelected: Boolean = false,
    val currentForegroundApp: String? = null,
    val currentForegroundAppName: String? = null,
    val isPanelExpanded: Boolean = false,
    val isServiceRunning: Boolean = false,
    val isAccessibilityConnected: Boolean = false,
    val isPaused: Boolean = false,
    val detectedItems: List<DetectedItem> = emptyList(),
    val liveHighlights: List<LiveScreenHighlight> = emptyList(),
    val activeResult: UnderstoodResult? = null,
    val selectedItemIndex: Int = 0,
    val currentTypedQuery: String = "",
    val isTypingActive: Boolean = false,
    val isAutoPopupEnabled: Boolean = false,
    val lastAutoPopupValue: Long? = null,
    val statusMessage: String = "Ready",
    val buttonPositionX: Float = 0.85f,
    val buttonPositionY: Float = 0.45f,
    val durationMinutes: Int = 0, // 0 = Always Display (Never Hide), 5, 10, 15, 30
    val sessionExpiryTimestamp: Long = 0L // 0L means never expires
)

object OverlayStateHolder {
    private const val PREFS_NAME = "ntt_overlay_prefs"
    private const val KEY_AUTO_POPUP = "auto_popup_enabled"
    private const val KEY_DURATION_MINUTES = "floating_bar_duration_minutes"

    private val _state = MutableStateFlow(OverlayUiState())
    val state: StateFlow<OverlayUiState> = _state.asStateFlow()

    private var sharedPrefs: SharedPreferences? = null
    private var lastAutoPopupTime: Long = 0L

    var onAutoPopupCallback: ((UnderstoodResult) -> Unit)? = null

    fun initialize(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val duration = sharedPrefs?.getInt(KEY_DURATION_MINUTES, 0) ?: 0
            _state.value = _state.value.copy(
                isAutoPopupEnabled = false, // Never auto popup or auto speak numbers unprompted
                durationMinutes = duration
            )
        }
    }

    fun setDurationMinutes(minutes: Int, context: Context? = null) {
        val expiry = if (minutes > 0) System.currentTimeMillis() + (minutes * 60 * 1000L) else 0L
        _state.value = _state.value.copy(
            durationMinutes = minutes,
            sessionExpiryTimestamp = expiry,
            isButtonVisible = if (_state.value.isForegroundAppSelected) true else _state.value.isButtonVisible
        )
        val prefs = sharedPrefs ?: context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.edit()?.putInt(KEY_DURATION_MINUTES, minutes)?.apply()
    }

    fun checkDurationExpiry() {
        val currentState = _state.value
        if (!currentState.isForegroundAppSelected || !currentState.isButtonVisible) return
        if (currentState.durationMinutes <= 0) return // 0 = Always Display (Never Hide)

        if (currentState.sessionExpiryTimestamp > 0L && System.currentTimeMillis() >= currentState.sessionExpiryTimestamp) {
            _state.value = currentState.copy(
                isButtonVisible = false,
                statusMessage = "Duration ended (${currentState.durationMinutes}m)"
            )
        }
    }

    private fun refreshSessionExpiry() {
        val duration = _state.value.durationMinutes
        val newExpiry = if (duration > 0) System.currentTimeMillis() + (duration * 60 * 1000L) else 0L
        _state.value = _state.value.copy(
            sessionExpiryTimestamp = newExpiry,
            isButtonVisible = if (_state.value.isForegroundAppSelected) true else _state.value.isButtonVisible
        )
    }

    fun setAutoPopupEnabled(enabled: Boolean, context: Context? = null) {
        _state.value = _state.value.copy(isAutoPopupEnabled = enabled)
        val prefs = sharedPrefs ?: context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.edit()?.putBoolean(KEY_AUTO_POPUP, enabled)?.apply()
    }

    /**
     * Triggered instantly when a selected authorized app enters foreground.
     * Starts session duration if user selected 5m/10m, or stays ALWAYS visible if duration is 0.
     */
    fun showForApp(packageName: String, appName: String) {
        if (_state.value.isPaused) return

        val duration = _state.value.durationMinutes
        val expiry = if (duration > 0) System.currentTimeMillis() + (duration * 60 * 1000L) else 0L

        val isNewApp = _state.value.currentForegroundApp != packageName

        _state.value = _state.value.copy(
            isButtonVisible = true,
            isForegroundAppSelected = true,
            currentForegroundApp = packageName,
            currentForegroundAppName = appName,
            liveHighlights = if (isNewApp) emptyList() else _state.value.liveHighlights,
            sessionExpiryTimestamp = expiry,
            statusMessage = "Active in $appName"
        )
    }

    /**
     * Triggered when leaving selected apps.
     */
    fun hideForApp(packageName: String) {
        _state.value = _state.value.copy(
            isButtonVisible = false,
            isForegroundAppSelected = false,
            isPanelExpanded = false,
            currentForegroundApp = packageName,
            currentForegroundAppName = null,
            liveHighlights = emptyList(),
            sessionExpiryTimestamp = 0L,
            statusMessage = "Hidden (Outside selected apps)"
        )
    }

    /**
     * Updates real-time detected on-screen highlights silently in background.
     * Never automatically pops up or auto-speaks numbers. The user taps the floating shortcut
     * bar to view the detected screen numbers and tap to listen.
     */
    fun updateLiveHighlights(highlights: List<LiveScreenHighlight>) {
        if (!_state.value.isForegroundAppSelected) return

        val preparedResult = if (_state.value.activeResult == null && highlights.isNotEmpty()) {
            val top = highlights.first()
            val curr = if (top.rawText.contains("₹") || top.rawText.contains("rs", ignoreCase = true)) "₹" else ""
            NumberTranslator.translateNumber(top.numericValue, "hi", curr)
        } else {
            _state.value.activeResult
        }

        _state.value = _state.value.copy(
            liveHighlights = highlights,
            activeResult = preparedResult
        )
    }

    fun selectHighlight(highlight: LiveScreenHighlight, preferredLanguageCode: String = "hi") {
        val parsed = NumberTranslator.translateNumber(
            highlight.numericValue,
            preferredLanguageCode,
            if (highlight.rawText.contains("₹") || highlight.rawText.contains("rs", ignoreCase = true)) "₹" else ""
        )

        _state.value = _state.value.copy(
            isPanelExpanded = true,
            activeResult = parsed,
            statusMessage = "Inspecting ${highlight.displayLabel}"
        )
    }

    fun onUserInteracted() {
        if (_state.value.isForegroundAppSelected) {
            refreshSessionExpiry()
        }
    }

    fun hideButtonTemporarily() {
        _state.value = _state.value.copy(
            isButtonVisible = false,
            isPanelExpanded = false
        )
    }

    fun showButton() {
        _state.value = _state.value.copy(
            isButtonVisible = true
        )
    }

    fun updateServiceRunning(running: Boolean) {
        _state.value = _state.value.copy(isServiceRunning = running)
    }

    fun updateAccessibilityConnected(connected: Boolean) {
        _state.value = _state.value.copy(isAccessibilityConnected = connected)
    }

    fun setPaused(paused: Boolean) {
        _state.value = _state.value.copy(
            isPaused = paused,
            statusMessage = if (paused) "Paused" else "Ready",
            isButtonVisible = if (paused) false else _state.value.isForegroundAppSelected
        )
    }

    /**
     * When user clicks the button inside a selected app, toggle the panel immediately.
     */
    fun togglePanel(preferredLanguageCode: String = "hi") {
        val willExpand = !_state.value.isPanelExpanded
        if (willExpand) {
            openPanel(preferredLanguageCode)
        } else {
            closePanel()
        }
    }

    /**
     * Instantly opens the panel with zero latency using live highlights,
     * while scheduling a background screen scan without blocking the UI thread.
     */
    fun openPanel(preferredLanguageCode: String = "hi") {
        val currentHighlights = _state.value.liveHighlights
        val immediateDetected = if (currentHighlights.isNotEmpty()) {
            currentHighlights.map { h ->
                val curr = if (h.rawText.contains("₹") || h.rawText.contains("rs", ignoreCase = true)) "₹" else ""
                val res = NumberTranslator.translateNumber(h.numericValue, preferredLanguageCode, curr)
                DetectedItem(
                    label = h.displayLabel,
                    rawText = h.rawText,
                    numericValue = h.numericValue,
                    currencySymbol = curr,
                    understoodResult = res,
                    visualFontSize = h.visualFontSize
                )
            }
        } else {
            _state.value.detectedItems
        }

        val topResult = immediateDetected.firstOrNull()?.understoodResult ?: _state.value.activeResult

        // Immediate UI response: zero opening delay!
        _state.value = _state.value.copy(
            isButtonVisible = true,
            isPanelExpanded = true,
            detectedItems = immediateDetected,
            activeResult = topResult,
            selectedItemIndex = 0,
            statusMessage = if (immediateDetected.isNotEmpty()) "Found ${immediateDetected.size} numbers on screen" else "Ready"
        )

        // Asynchronously update screen numbers in background without freezing the UI
        CoroutineScope(Dispatchers.Main).launch {
            scanActiveScreen(preferredLanguageCode)
        }
    }

    /**
     * Scans the real active screen via NttAccessibilityService.
     */
    fun scanActiveScreen(preferredLanguageCode: String = "hi") {
        val service = NttAccessibilityService.instance
        if (service != null) {
            val detected = service.scanAndExtractNumbers(preferredLanguageCode)
            if (detected.isNotEmpty()) {
                _state.value = _state.value.copy(
                    detectedItems = detected,
                    activeResult = detected.firstOrNull()?.understoodResult,
                    selectedItemIndex = 0,
                    statusMessage = "Found ${detected.size} numbers on screen"
                )
            } else if (_state.value.detectedItems.isEmpty()) {
                _state.value = _state.value.copy(
                    statusMessage = "No numbers on current screen"
                )
            }
        }
    }

    /**
     * Real-time typing for instant zero-latency translation.
     */
    fun onRealtimeQueryChanged(query: String, preferredLanguageCode: String = "hi") {
        val trimmed = query.trim()
        _state.value = _state.value.copy(
            currentTypedQuery = query,
            isTypingActive = true
        )

        if (trimmed.isBlank()) {
            val screenResult = _state.value.detectedItems.firstOrNull()?.understoodResult
            _state.value = _state.value.copy(
                activeResult = screenResult,
                statusMessage = if (_state.value.detectedItems.isNotEmpty()) "Found ${_state.value.detectedItems.size} numbers on screen" else "Ready"
            )
            return
        }

        // 1. Check direct numbers / price strings
        val parsedItems = NumberTextParser.extractNumbersAndPrices(trimmed, preferredLanguageCode)
        if (parsedItems.isNotEmpty()) {
            _state.value = _state.value.copy(
                activeResult = parsedItems.first().understoodResult,
                statusMessage = "Instant translation"
            )
            return
        }

        // 2. Check phonetic matches
        val phoneticMatches = PhoneticMatcher.search(trimmed, preferredLanguageCode)
        if (phoneticMatches.isNotEmpty()) {
            _state.value = _state.value.copy(
                activeResult = phoneticMatches.first().result,
                statusMessage = "Phonetic match"
            )
            return
        }

        // 3. Fallback: Parse digits only
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.isNotBlank()) {
            val num = digitsOnly.toLongOrNull()
            if (num != null) {
                val curr = if (trimmed.contains("₹") || trimmed.contains("rs", ignoreCase = true) || trimmed.contains("inr", ignoreCase = true)) "₹" else ""
                val res = NumberTranslator.translateNumber(num, preferredLanguageCode, curr)
                _state.value = _state.value.copy(
                    activeResult = res,
                    statusMessage = "Instant translation"
                )
                return
            }
        }

        _state.value = _state.value.copy(
            activeResult = null,
            statusMessage = "No number match for \"$trimmed\""
        )
    }

    fun setTypingFocus(isTyping: Boolean) {
        _state.value = _state.value.copy(isTypingActive = isTyping)
    }

    fun clearTypedInput(preferredLanguageCode: String = "hi") {
        _state.value = _state.value.copy(
            currentTypedQuery = "",
            isTypingActive = false,
            activeResult = _state.value.detectedItems.firstOrNull()?.understoodResult
        )
    }

    fun showPanelWithItems(items: List<DetectedItem>) {
        _state.value = _state.value.copy(
            isButtonVisible = true,
            isPanelExpanded = true,
            detectedItems = items,
            activeResult = items.firstOrNull()?.understoodResult ?: _state.value.activeResult,
            selectedItemIndex = 0
        )
    }

    fun showPanelWithResult(result: UnderstoodResult) {
        _state.value = _state.value.copy(
            isButtonVisible = true,
            isPanelExpanded = true,
            activeResult = result,
            detectedItems = emptyList()
        )
    }

    fun selectItem(item: DetectedItem, index: Int) {
        _state.value = _state.value.copy(
            activeResult = item.understoodResult,
            selectedItemIndex = index
        )
    }

    fun closePanel() {
        _state.value = _state.value.copy(isPanelExpanded = false)
    }

    fun updateButtonPosition(x: Float, y: Float) {
        _state.value = _state.value.copy(buttonPositionX = x, buttonPositionY = y)
    }
}
