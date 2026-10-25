package com.example.overlay

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.accessibility.NttAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TriggerApp(
    val packageName: String,
    val appName: String,
    val category: String,
    val isEnabled: Boolean = true,
    val isSystemApp: Boolean = false
)

/**
 * Manages device-detected applications for triggering the floating NTT bar.
 * ZERO MOCK DATA: All apps are detected directly from the user's actual Android device
 * via PackageManager. No hardcoded or fake app entries are ever presented.
 */
object SelectedAppsManager {
    private const val PREFS_NAME = "ntt_selected_apps_prefs"
    private const val KEY_ENABLED_PACKAGES = "enabled_packages_set"
    private const val KEY_HAS_INITIALIZED = "has_initialized_defaults"

    private var sharedPrefs: SharedPreferences? = null
    private var lastForegroundPackage: String? = null

    // Holds only real apps physically installed on the current device
    private val _triggerApps = MutableStateFlow<List<TriggerApp>>(emptyList())
    val triggerApps: StateFlow<List<TriggerApp>> = _triggerApps.asStateFlow()

    // Known launcher packages representing Android Home Screens
    private val KNOWN_LAUNCHER_PACKAGES = setOf(
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.huawei.android.launcher",
        "com.oneplus.launcher",
        "com.motorola.launcher3"
    )

    private val dynamicLauncherPackages = mutableSetOf<String>()

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        if (sharedPrefs == null) {
            sharedPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        detectDynamicLaunchers(appContext)
        refreshInstalledApps(appContext)
    }

    private fun detectDynamicLaunchers(context: Context) {
        dynamicLauncherPackages.clear()
        dynamicLauncherPackages.addAll(KNOWN_LAUNCHER_PACKAGES)
        try {
            val pm = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveList = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (ri in resolveList) {
                val pkg = ri.activityInfo?.packageName
                if (!pkg.isNullOrBlank()) {
                    dynamicLauncherPackages.add(pkg)
                }
            }
        } catch (e: Exception) {
            // fallback to known launchers
        }
    }

    fun isLauncherPackage(pkg: String): Boolean {
        if (pkg.isBlank()) return false
        val lower = pkg.lowercase()
        return dynamicLauncherPackages.any { it.equals(lower, ignoreCase = true) } ||
                lower.contains(".launcher") || lower.endsWith(".home")
    }

    private fun getSavedEnabledPackages(): Set<String> {
        val prefs = sharedPrefs ?: return emptySet()
        return prefs.getStringSet(KEY_ENABLED_PACKAGES, emptySet()) ?: emptySet()
    }

    private fun saveEnabledPackages(enabledSet: Set<String>) {
        val prefs = sharedPrefs ?: return
        prefs.edit()
            .putStringSet(KEY_ENABLED_PACKAGES, enabledSet)
            .putBoolean(KEY_HAS_INITIALIZED, true)
            .apply()
    }

    /**
     * Scans the user's device for all installed launchable applications.
     * Guaranteed 100% real device data: only applications present in the device's
     * PackageManager are returned.
     */
    fun refreshInstalledApps(context: Context) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveList = pm.queryIntentActivities(intent, 0)
        val prefs = sharedPrefs
        val hasInitialized = prefs?.getBoolean(KEY_HAS_INITIALIZED, false) == true
        val savedEnabled = getSavedEnabledPackages()
        val currentEnabled = savedEnabled.toMutableSet()

        val detectedList = mutableListOf<TriggerApp>()

        for (resolveInfo in resolveList) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue

            // Exclude this app itself and launcher / home screen packages
            if (pkg == context.packageName || KNOWN_LAUNCHER_PACKAGES.contains(pkg)) {
                continue
            }

            val appInfo = try {
                resolveInfo.activityInfo.applicationInfo ?: pm.getApplicationInfo(pkg, 0)
            } catch (e: Exception) {
                continue
            }

            val label = resolveInfo.loadLabel(pm).toString().ifBlank { pkg }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val category = detectAppCategory(appInfo, label, pkg)

            // On very first run, enable detected apps that are related to payments/utilities/shopping
            if (!hasInitialized) {
                if (category == "Finance & UPI" || category == "Shopping" || pkg.contains("calc")) {
                    currentEnabled.add(pkg)
                }
            }

            detectedList.add(
                TriggerApp(
                    packageName = pkg,
                    appName = label,
                    category = category,
                    isEnabled = currentEnabled.contains(pkg),
                    isSystemApp = isSystem
                )
            )
        }

        // If it was first run, persist initial selections
        if (!hasInitialized) {
            // If no finance apps were found on first run, enable up to 2 non-system installed apps
            if (currentEnabled.isEmpty()) {
                val candidate = detectedList.firstOrNull { !it.isSystemApp } ?: detectedList.firstOrNull()
                candidate?.let { currentEnabled.add(it.packageName) }
            }
            saveEnabledPackages(currentEnabled)
        }

        // Sort: enabled apps first, then alphabetically by app name
        val sortedList = detectedList
            .distinctBy { it.packageName }
            .map { it.copy(isEnabled = currentEnabled.contains(it.packageName)) }
            .sortedWith(
                compareByDescending<TriggerApp> { it.isEnabled }
                    .thenBy { it.appName.lowercase() }
            )

        _triggerApps.value = sortedList
    }

    private fun detectAppCategory(appInfo: ApplicationInfo, label: String, pkg: String): String {
        val lower = "$label $pkg".lowercase()

        if (lower.contains("pay") || lower.contains("upi") || lower.contains("bank") ||
            lower.contains("money") || lower.contains("wallet") || lower.contains("phonepe") ||
            lower.contains("paisa") || lower.contains("cred")) {
            return "Finance & UPI"
        }
        if (lower.contains("shop") || lower.contains("cart") || lower.contains("amazon") ||
            lower.contains("flipkart") || lower.contains("meesho") || lower.contains("myntra")) {
            return "Shopping"
        }
        if (lower.contains("chat") || lower.contains("whatsapp") || lower.contains("telegram") ||
            lower.contains("message") || lower.contains("signal")) {
            return "Messaging"
        }
        if (lower.contains("calc") || lower.contains("clock") || lower.contains("calendar") ||
            lower.contains("note") || lower.contains("tool") || lower.contains("setting")) {
            return "Utilities"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_AUDIO -> return "Audio & Music"
                ApplicationInfo.CATEGORY_GAME -> return "Games"
                ApplicationInfo.CATEGORY_IMAGE -> return "Photos & Media"
                ApplicationInfo.CATEGORY_MAPS -> return "Maps & Navigation"
                ApplicationInfo.CATEGORY_NEWS -> return "News"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "Productivity"
                ApplicationInfo.CATEGORY_SOCIAL -> return "Social"
                ApplicationInfo.CATEGORY_VIDEO -> return "Video & Streaming"
            }
        }

        return if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) "System" else "Applications"
    }

    fun isAppSelected(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val lower = packageName.lowercase()
        // Never trigger on home screen launcher or system overlays
        if (isIgnoredSystemPackage(lower) || isLauncherPackage(lower)) {
            return false
        }

        val enabledSet = getSavedEnabledPackages()
        return enabledSet.contains(packageName)
    }

    fun getAppName(packageName: String): String {
        return _triggerApps.value.firstOrNull { it.packageName == packageName }?.appName ?: packageName
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        val currentSet = getSavedEnabledPackages().toMutableSet()
        if (enabled) {
            currentSet.add(packageName)
        } else {
            currentSet.remove(packageName)
        }
        saveEnabledPackages(currentSet)

        _triggerApps.value = _triggerApps.value.map {
            if (it.packageName == packageName) it.copy(isEnabled = enabled) else it
        }

        // If currently in this app, update overlay immediately
        if (lastForegroundPackage == packageName) {
            if (enabled) {
                OverlayStateHolder.showForApp(packageName, getAppName(packageName))
            } else {
                OverlayStateHolder.hideForApp(packageName)
            }
        }
    }

    /**
     * Adds an app by package name only if it is genuinely installed on this device.
     * Prevents fake or non-existent packages from being added.
     */
    fun addInstalledAppByPackage(packageName: String, context: Context): Result<TriggerApp> {
        val trimmedPkg = packageName.trim()
        if (trimmedPkg.isBlank()) {
            return Result.failure(IllegalArgumentException("Package name cannot be empty"))
        }

        if (trimmedPkg == context.packageName || isLauncherPackage(trimmedPkg)) {
            return Result.failure(IllegalArgumentException("Cannot add launcher or NTT itself as a trigger app"))
        }

        val pm = context.packageManager
        val appInfo = try {
            pm.getApplicationInfo(trimmedPkg, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return Result.failure(IllegalStateException("App '$trimmedPkg' is NOT installed on this device."))
        }

        val label = pm.getApplicationLabel(appInfo).toString().ifBlank { trimmedPkg }
        val category = detectAppCategory(appInfo, label, trimmedPkg)
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        // Enable in set
        val currentSet = getSavedEnabledPackages().toMutableSet()
        currentSet.add(trimmedPkg)
        saveEnabledPackages(currentSet)

        val newApp = TriggerApp(
            packageName = trimmedPkg,
            appName = label,
            category = category,
            isEnabled = true,
            isSystemApp = isSystem
        )

        val updatedList = (_triggerApps.value.filter { it.packageName != trimmedPkg } + newApp)
            .sortedWith(
                compareByDescending<TriggerApp> { it.isEnabled }
                    .thenBy { it.appName.lowercase() }
            )

        _triggerApps.value = updatedList

        if (lastForegroundPackage == trimmedPkg) {
            OverlayStateHolder.showForApp(trimmedPkg, label)
        }

        return Result.success(newApp)
    }

    private val debounceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pendingHideJob: Job? = null

    fun isIgnoredSystemPackage(pkg: String): Boolean {
        if (pkg.isBlank()) return true
        val lower = pkg.lowercase()
        if (lower == "android" || lower == "com.android.systemui" || lower == "com.example") return true
        if (lower.startsWith("com.google.android.gms") ||
            lower.startsWith("com.google.android.gsf") ||
            lower.startsWith("com.google.android.webview") ||
            lower.startsWith("com.android.webview") ||
            lower.startsWith("com.android.chrome") ||
            lower.contains("trichromelibrary") ||
            lower.startsWith("com.google.android.packageinstaller") ||
            lower.startsWith("com.android.packageinstaller") ||
            lower.startsWith("com.android.vending") ||
            lower.startsWith("com.google.android.feedback") ||
            lower.startsWith("com.google.android.documentsui") ||
            lower.startsWith("com.android.documentsui") ||
            lower.startsWith("com.android.internal")
        ) return true

        if (lower.contains("inputmethod") || lower.contains("keyboard") ||
            lower.contains("honeyboard") || lower.contains("swiftkey") ||
            lower.contains("gboard") || lower.contains("latin") ||
            lower.contains("accessibility") || lower.contains("overlay") ||
            lower.contains("screenshot") || lower.contains("permissioncontroller") ||
            lower.contains("autofill") || lower.contains("credential") ||
            lower.contains("biometric") || lower.contains("clipboard") ||
            lower.contains("keyguard") || lower.contains("chooser") ||
            lower.contains("resolver") || lower.contains("transient") ||
            lower.contains("systemui") || lower.contains("volume") ||
            lower.contains("notification") || lower.contains("security") ||
            lower.contains("settings.intelligence")
        ) {
            return true
        }
        return false
    }

    /**
     * Called whenever a confirmed selected app is active or interacted with.
     * Guarantees continuous floating of the shortcut button in the corner without hiding.
     */
    fun onSelectedAppActive(packageName: String) {
        val trimmed = packageName.trim()
        if (trimmed.isBlank() || isIgnoredSystemPackage(trimmed) || isLauncherPackage(trimmed)) {
            return
        }
        if (isAppSelected(trimmed)) {
            pendingHideJob?.cancel()
            pendingHideJob = null
            lastForegroundPackage = trimmed

            val name = getAppName(trimmed)
            OverlayStateHolder.showForApp(trimmed, name)
        }
    }

    fun handleForegroundPackage(packageName: String) {
        val trimmed = packageName.trim()
        // 1. Critical: System UI, keyboards, autofill, popups NEVER hide the floating bar
        if (trimmed.isBlank() || isIgnoredSystemPackage(trimmed)) {
            return
        }

        // 2. If the event package is an authorized selected app, float continuously!
        if (isAppSelected(trimmed)) {
            pendingHideJob?.cancel()
            pendingHideJob = null
            lastForegroundPackage = trimmed

            val name = getAppName(trimmed)
            OverlayStateHolder.showForApp(trimmed, name)
            NttAccessibilityService.instance?.scanAndHighlightScreenRealtime()
            return
        }

        // 3. What if this package is unselected, BUT the active window is still the selected app?
        // (e.g. an in-app popup, ad banner, dialog, webview fragment, or secondary framework view)
        val activeRootPkg = NttAccessibilityService.instance?.rootInActiveWindow?.packageName?.toString()
        if (activeRootPkg != null && isAppSelected(activeRootPkg)) {
            // User is still looking at the selected app! DO NOT HIDE!
            pendingHideJob?.cancel()
            pendingHideJob = null
            lastForegroundPackage = activeRootPkg
            OverlayStateHolder.showForApp(activeRootPkg, getAppName(activeRootPkg))
            return
        }

        // 4. If we are currently floating in a selected app, verify if the user truly left
        // (e.g., navigated to the Home Screen launcher or another unselected app):
        val isLauncher = isLauncherPackage(trimmed)
        val hideDelayMs = if (isLauncher) 1200L else 3000L

        pendingHideJob?.cancel()
        pendingHideJob = debounceScope.launch {
            delay(hideDelayMs)

            // Double check before hiding: has the user returned to or stayed in the selected app?
            val currentRoot = NttAccessibilityService.instance?.rootInActiveWindow?.packageName?.toString()
            if (currentRoot != null && isAppSelected(currentRoot)) {
                // User is still in the selected app - cancel hide and keep floating!
                lastForegroundPackage = currentRoot
                OverlayStateHolder.showForApp(currentRoot, getAppName(currentRoot))
                return@launch
            }

            // Also check interactive windows collection if available
            val hasSelectedWindow = try {
                NttAccessibilityService.instance?.windows?.any { w ->
                    val wPkg = w.root?.packageName?.toString()
                    wPkg != null && isAppSelected(wPkg)
                } ?: false
            } catch (e: Exception) {
                false
            }

            if (hasSelectedWindow) {
                return@launch
            }

            // Confirmed genuinely outside selected app (e.g. on home screen)
            lastForegroundPackage = trimmed
            OverlayStateHolder.hideForApp(trimmed)
        }
    }
}
