package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.example.overlay.LiveScreenHighlight
import com.example.overlay.OverlayStateHolder
import com.example.overlay.SelectedAppsManager
import com.example.search.NumberTextParser
import com.example.search.ScreenSnippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NttAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastScanTime = 0L

    companion object {
        var instance: NttAccessibilityService? = null
            private set

        fun isConnected(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        SelectedAppsManager.initialize(applicationContext)
        OverlayStateHolder.initialize(applicationContext)
        OverlayStateHolder.updateAccessibilityConnected(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        // 1. Continuous Floating Check:
        // If the event or the current root window belongs to an authorized selected app,
        // instantly ensure the shortcut button is floating in the corner and never hide!
        val activeRootPkg = rootInActiveWindow?.packageName?.toString()
        val confirmedPkg = when {
            activeRootPkg != null && SelectedAppsManager.isAppSelected(activeRootPkg) -> activeRootPkg
            SelectedAppsManager.isAppSelected(pkg) -> pkg
            else -> null
        }

        if (confirmedPkg != null) {
            SelectedAppsManager.onSelectedAppActive(confirmedPkg)
        }

        // 2. Never switch or hide the overlay for ignored system packages, keyboards, or NTT itself
        if (SelectedAppsManager.isIgnoredSystemPackage(pkg)) {
            return
        }

        // 3. Only actual window state changes (switching to a different activity / app)
        // should update the foreground package tracking
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val className = event.className?.toString() ?: ""
            // Ignore transient notifications, toasts, or quick alert popups
            if (!className.contains("Toast") && !className.contains("Snackbar") && !className.contains("Dialog")) {
                SelectedAppsManager.handleForegroundPackage(pkg)
            }
        }

        // 4. Real-time on-screen number and word detection:
        // When active inside a selected app, continuously scan content changes to highlight
        // numbers in real-time and provide immediate visual feedback to the user.
        val isSelectedAppActive = OverlayStateHolder.state.value.isForegroundAppSelected ||
                SelectedAppsManager.isAppSelected(pkg) ||
                (activeRootPkg != null && SelectedAppsManager.isAppSelected(activeRootPkg))

        if (isSelectedAppActive) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
            ) {
                scheduleRealtimeScreenScan()
            }
        }
    }

    private fun scheduleRealtimeScreenScan() {
        val now = System.currentTimeMillis()
        // Debounce scan calls to every 300ms so screen responsiveness remains fluid
        if (now - lastScanTime < 300) return
        lastScanTime = now

        serviceScope.launch {
            scanAndHighlightScreenRealtime()
        }
    }

    private fun getStatusBarHeightPx(): Int {
        var height = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        if (height <= 0) {
            height = (32 * resources.displayMetrics.density).toInt()
        }
        return height
    }

    /**
     * Determines whether a node belongs to the Android Status Bar or System UI.
     * Completely ignores time (e.g. 8:00), battery percentage (8%), battery icons, and system indicators.
     */
    private fun isStatusBarOrSystemUiNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return true

        // 1. Package Name check: Android framework, System UI, keyguard, lockscreen
        val pkg = node.packageName?.toString() ?: ""
        if (pkg.contains("systemui", ignoreCase = true) ||
            pkg.contains("keyguard", ignoreCase = true) ||
            pkg.contains("statusbar", ignoreCase = true) ||
            pkg.contains("lockscreen", ignoreCase = true) ||
            pkg == "android"
        ) {
            return true
        }

        // 2. Window Type check: Strictly allow ONLY application windows!
        try {
            val window = node.window
            if (window != null && window.type != AccessibilityWindowInfo.TYPE_APPLICATION &&
                window.type != AccessibilityWindowInfo.TYPE_INPUT_METHOD
            ) {
                return true
            }
        } catch (_: Exception) {}

        // 3. Status Bar & Navigation Bar Geometry check
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val statusBarHeight = maxOf(getStatusBarHeightPx(), (38 * resources.displayMetrics.density).toInt())

        // Top status bar area: completely ignore any element residing within or overlapping the top bar
        if (rect.bottom > 0 && rect.bottom <= statusBarHeight + (12 * resources.displayMetrics.density).toInt()) {
            return true
        }
        if (rect.top >= 0 && rect.top < statusBarHeight && rect.height() <= statusBarHeight * 1.5) {
            return true
        }

        // Bottom system navigation bar area
        val screenHeight = resources.displayMetrics.heightPixels
        val navBarHeight = (48 * resources.displayMetrics.density).toInt()
        if (rect.top >= screenHeight - navBarHeight && rect.bottom >= screenHeight) {
            return true
        }

        // 4. Resource ID check for system widgets and indicators
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains("systemui") ||
            viewId.contains("status_bar") ||
            viewId.contains("statusbar") ||
            viewId.contains("clock") ||
            viewId.contains("time") ||
            viewId.contains("battery") ||
            viewId.contains("signal") ||
            viewId.contains("wifi") ||
            viewId.contains("carrier") ||
            viewId.contains("cellular") ||
            viewId.contains("indicator") ||
            viewId.contains("notification_icon") ||
            viewId.contains("quick_settings") ||
            viewId.contains("navigation_bar") ||
            viewId.contains("navbar")
        ) {
            return true
        }

        // 5. System UI text / Content Description check (clock, battery, 5G, etc.)
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        if (text != null && NumberTextParser.isSystemUiText(text)) return true
        if (desc != null && NumberTextParser.isSystemUiText(desc)) return true

        return false
    }

    /**
     * Traverses the active window, detects numbers and words in real time with their
     * screen coordinates, and sends them to the Compose overlay with font-size based priority.
     */
    fun scanAndHighlightScreenRealtime(langCode: String = "hi") {
        val root = rootInActiveWindow ?: return
        val rootPkg = root.packageName?.toString() ?: ""
        if (SelectedAppsManager.isIgnoredSystemPackage(rootPkg) ||
            rootPkg.contains("systemui", ignoreCase = true) ||
            rootPkg.contains("keyguard", ignoreCase = true) ||
            rootPkg == "android"
        ) {
            return
        }

        val highlights = mutableListOf<LiveScreenHighlight>()
        val seenValues = mutableSetOf<Long>()

        traverseNodeForHighlights(root, langCode, highlights, seenValues)

        // Also check secondary interactive window roots if multi-window is active
        try {
            val windowList = windows
            if (windowList != null && windowList.isNotEmpty()) {
                for (w in windowList) {
                    if (w.type == AccessibilityWindowInfo.TYPE_APPLICATION) {
                        val wRoot = w.root
                        if (wRoot != null && wRoot != root) {
                            traverseNodeForHighlights(wRoot, langCode, highlights, seenValues)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore window query fallback
        }

        // Font-Size-Based Priority: Give higher detection priority to text displayed in larger fonts on the screen.
        // When multiple text elements are present, prioritize the most prominent, larger-font text over smaller text.
        // Use actual visual font size to determine priority, not the numerical value or text length.
        highlights.sortWith(
            compareByDescending<LiveScreenHighlight> { it.visualFontSize }
                .thenByDescending { it.rawText.contains("₹") || it.rawText.contains("rs", ignoreCase = true) || it.rawText.contains("inr", ignoreCase = true) }
        )

        OverlayStateHolder.updateLiveHighlights(highlights)
    }

    private fun traverseNodeForHighlights(
        node: AccessibilityNodeInfo?,
        langCode: String,
        collected: MutableList<LiveScreenHighlight>,
        seenValues: MutableSet<Long>
    ) {
        if (node == null) return

        // 1. Completely ignore Status Bar, System UI, time, battery icons
        if (isStatusBarOrSystemUiNode(node)) {
            return
        }

        // 2. Skip sensitive password, PIN, CVV, OTP fields to protect user security
        if (SensitiveContentDetector.isNodeSensitive(node)) {
            return
        }

        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        val content = when {
            !text.isNullOrBlank() -> text
            !desc.isNullOrBlank() -> desc
            else -> null
        }

        if (!content.isNullOrBlank() && !NumberTextParser.isSystemUiText(content)) {
            val sanitized = SensitiveContentDetector.sanitizeText(content)
            val rect = Rect()
            node.getBoundsInScreen(rect)
            // Visual font size: for leaf text elements, rect.height() is the actual line/font height on display
            val visualFontSize = if (node.childCount == 0) {
                rect.height()
            } else {
                minOf(rect.height(), (48 * resources.displayMetrics.density).toInt())
            }

            // Extract exact numbers from screen content; never guess or fabricate numbers
            val parsed = NumberTextParser.extractNumbersAndPrices(
                rawText = sanitized,
                langCode = langCode,
                visualFontSize = visualFontSize,
                allowPhoneticFallback = false
            )

            if (parsed.isNotEmpty()) {
                for (item in parsed) {
                    if (!seenValues.contains(item.numericValue)) {
                        seenValues.add(item.numericValue)
                        val res = item.understoodResult
                        val slang = if (res.breakdown.isNotBlank()) res.breakdown else null

                        collected.add(
                            LiveScreenHighlight(
                                id = "${item.numericValue}_${rect.left}_${rect.top}",
                                rawText = item.rawText,
                                numericValue = item.numericValue,
                                displayLabel = item.label,
                                wordsTranslation = "${res.phoneticText} • ${res.englishMeaning}",
                                slangTerm = slang,
                                boundsLeft = rect.left,
                                boundsTop = rect.top,
                                boundsRight = rect.right,
                                boundsBottom = rect.bottom,
                                visualFontSize = visualFontSize
                            )
                        )
                    }
                }
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNodeForHighlights(child, langCode, collected, seenValues)
            }
        }
    }

    override fun onInterrupt() {
        if (instance == this) instance = null
        OverlayStateHolder.updateAccessibilityConnected(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (instance == this) instance = null
        OverlayStateHolder.updateAccessibilityConnected(false)
    }

    fun scanAndExtractNumbers(langCode: String = "hi"): List<com.example.search.DetectedItem> {
        val snippets = captureCurrentScreenSnippets()
        if (snippets.isEmpty()) return emptyList()
        return NumberTextParser.extractFromScreenSnippetsWithFontSizes(snippets, langCode)
    }

    fun captureCurrentScreenSnippets(): List<ScreenSnippet> {
        val snippetList = mutableListOf<ScreenSnippet>()
        val root = rootInActiveWindow
        if (root != null) {
            traverseNodeForSnippets(root, snippetList)
        }

        try {
            val windowList = windows
            if (windowList != null && windowList.isNotEmpty()) {
                for (w in windowList) {
                    if (w.type == AccessibilityWindowInfo.TYPE_APPLICATION) {
                        val wRoot = w.root
                        if (wRoot != null && wRoot != root) {
                            traverseNodeForSnippets(wRoot, snippetList)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to single active window
        }

        return snippetList
    }

    private fun traverseNodeForSnippets(node: AccessibilityNodeInfo?, collected: MutableList<ScreenSnippet>) {
        if (node == null) return

        // Completely ignore Status Bar & System UI
        if (isStatusBarOrSystemUiNode(node)) {
            return
        }

        if (SensitiveContentDetector.isNodeSensitive(node)) {
            return
        }

        val rect = Rect()
        node.getBoundsInScreen(rect)
        val visualFontSize = if (node.childCount == 0) {
            rect.height()
        } else {
            minOf(rect.height(), (48 * resources.displayMetrics.density).toInt())
        }

        val text = node.text?.toString()
        if (!text.isNullOrBlank() && !NumberTextParser.isSystemUiText(text)) {
            val sanitized = SensitiveContentDetector.sanitizeText(text)
            collected.add(ScreenSnippet(text = sanitized, visualFontSize = visualFontSize, bounds = rect))
        }

        val contentDesc = node.contentDescription?.toString()
        if (!contentDesc.isNullOrBlank() && contentDesc != text && !NumberTextParser.isSystemUiText(contentDesc)) {
            val sanitized = SensitiveContentDetector.sanitizeText(contentDesc)
            collected.add(ScreenSnippet(text = sanitized, visualFontSize = visualFontSize, bounds = rect))
        }

        for (i in 0 until node.childCount) {
            traverseNodeForSnippets(node.getChild(i), collected)
        }
    }
}
