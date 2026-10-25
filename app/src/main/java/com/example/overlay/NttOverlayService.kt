package com.example.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.speech.LocalNttTtsManager
import com.example.speech.NttTtsManager
import com.example.ui.components.FloatingNttOverlayComposeContent
import com.example.ui.theme.NTTTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class NttOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayComposeView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var ttsManager: NttTtsManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        ttsManager = NttTtsManager(applicationContext)
        SelectedAppsManager.initialize(applicationContext)
        OverlayStateHolder.initialize(applicationContext)

        startForegroundNotification()
        OverlayStateHolder.updateServiceRunning(true)

        if (Settings.canDrawOverlays(this)) {
            setupOverlayWindow()
        }
    }

    private fun startForegroundNotification() {
        val channelId = "ntt_overlay_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NTT Instant Screen Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows NTT floating accessibility utility above other apps"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NTT is ready")
            .setContentText("Tap to understand numbers and spoken words instantly")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1001, notification)
        }
    }

    private fun setupOverlayWindow() {
        if (overlayComposeView != null) return // Ensure only one floating bar instance exists
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val prefs = getSharedPreferences("ntt_overlay_prefs", Context.MODE_PRIVATE)
        val savedX = prefs.getInt("key_corner_x", 16)
        val savedY = prefs.getInt("key_corner_y", 320)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            // Anchor continuously in the top-right corner of the user's selected app
            gravity = Gravity.TOP or Gravity.END
            x = savedX
            y = savedY
        }

        val composeView = ComposeView(this).apply {
            visibility = View.GONE // Start hidden until a selected app is active
            setViewTreeLifecycleOwner(this@NttOverlayService)
            setViewTreeSavedStateRegistryOwner(this@NttOverlayService)
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    if (OverlayStateHolder.state.value.isPanelExpanded) {
                        OverlayStateHolder.closePanel()
                        true
                    } else false
                } else false
            }
            setContent {
                val tts = ttsManager
                if (tts != null) {
                    CompositionLocalProvider(LocalNttTtsManager provides tts) {
                        NTTTheme {
                            FloatingNttOverlayComposeContent(
                                onDrag = { dx, dy ->
                                    this@NttOverlayService.layoutParams?.let { params ->
                                        // With Gravity.TOP or Gravity.END, horizontal drag adjusts distance from the corner edge
                                        params.x = (params.x - dx.toInt()).coerceAtLeast(0)
                                        params.y = (params.y + dy.toInt()).coerceAtLeast(40)
                                        windowManager?.updateViewLayout(this@apply, params)
                                        val sp = getSharedPreferences("ntt_overlay_prefs", Context.MODE_PRIVATE)
                                        sp.edit().putInt("key_corner_x", params.x).putInt("key_corner_y", params.y).apply()
                                    }
                                },
                                onClose = {
                                    OverlayStateHolder.closePanel()
                                },
                                onSpeak = { text ->
                                    tts.speak(text)
                                }
                            )
                        }
                    }
                } else {
                    NTTTheme {
                        FloatingNttOverlayComposeContent(
                            onDrag = { dx, dy ->
                                this@NttOverlayService.layoutParams?.let { params ->
                                    params.x = (params.x - dx.toInt()).coerceAtLeast(0)
                                    params.y = (params.y + dy.toInt()).coerceAtLeast(40)
                                    windowManager?.updateViewLayout(this@apply, params)
                                    val sp = getSharedPreferences("ntt_overlay_prefs", Context.MODE_PRIVATE)
                                    sp.edit().putInt("key_corner_x", params.x).putInt("key_corner_y", params.y).apply()
                                }
                            },
                            onClose = {
                                OverlayStateHolder.closePanel()
                            }
                        )
                    }
                }
            }
        }

        overlayComposeView = composeView
        try {
            windowManager?.addView(overlayComposeView, layoutParams)
        } catch (e: Exception) {
            // overlay window permission or context edge cases
        }

        // Dynamically adjust visibility and FLAG_NOT_FOCUSABLE
        serviceScope.launch {
            OverlayStateHolder.state.collect { state ->
                val wm = windowManager ?: return@collect
                val view = overlayComposeView ?: return@collect
                val params = layoutParams ?: return@collect

                // The bar appears instantly ONLY when a selected app is in foreground.
                // When leaving the selected app (home screen or unselected app), hide completely.
                val shouldBeVisible = state.isButtonVisible && state.isForegroundAppSelected
                val targetVisibility = if (shouldBeVisible) View.VISIBLE else View.GONE
                if (view.visibility != targetVisibility) {
                    view.visibility = targetVisibility
                }

                val shouldBeFocusable = state.isPanelExpanded
                val isCurrentlyNotFocusable = (params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0

                if (shouldBeFocusable && isCurrentlyNotFocusable) {
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                    try {
                        wm.updateViewLayout(view, params)
                    } catch (e: Exception) {
                        // ignore view update race condition
                    }
                } else if (!shouldBeFocusable && !isCurrentlyNotFocusable) {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    try {
                        wm.updateViewLayout(view, params)
                    } catch (e: Exception) {
                        // ignore view update race condition
                    }
                }
            }
        }

        // Periodic check for user-selected visibility duration (e.g. 5m / 10m / always)
        serviceScope.launch {
            while (isActive) {
                delay(1000L)
                OverlayStateHolder.checkDurationExpiry()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        if (overlayComposeView != null && windowManager != null) {
            try {
                windowManager?.removeView(overlayComposeView)
            } catch (e: Exception) {
                // view cleanup
            }
            overlayComposeView = null
        }
        ttsManager?.shutdown()
        ttsManager = null
        OverlayStateHolder.onAutoPopupCallback = null
        OverlayStateHolder.updateServiceRunning(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
