package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.data.NttDatabase
import com.example.data.UnderstoodRepository
import com.example.model.PreferencesManager
import com.example.overlay.OverlayStateHolder
import com.example.overlay.SelectedAppsManager
import com.example.speech.NttTtsManager
import com.example.speech.VoiceRecognizerHelper
import com.example.tile.NttTileService
import com.example.ui.MainAppScreen
import com.example.ui.theme.NTTTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: NttDatabase
    private lateinit var repository: UnderstoodRepository
    private lateinit var prefsManager: PreferencesManager
    private lateinit var ttsManager: NttTtsManager
    private var voiceRecognizer: VoiceRecognizerHelper? = null

    private var openMicRequested by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions granted/denied handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = NttDatabase.getDatabase(applicationContext)
        repository = UnderstoodRepository(database.understoodDao())
        prefsManager = PreferencesManager(applicationContext)
        ttsManager = NttTtsManager(applicationContext)
        voiceRecognizer = VoiceRecognizerHelper(applicationContext)
        SelectedAppsManager.initialize(applicationContext)
        OverlayStateHolder.initialize(applicationContext)

        checkPermissions()
        checkOpenMicIntent(intent)

        setContent {
            NTTTheme {
                MainAppScreen(
                    repository = repository,
                    prefsManager = prefsManager,
                    ttsManager = ttsManager,
                    voiceRecognizer = voiceRecognizer,
                    openMicOnStart = openMicRequested,
                    onOpenMicHandled = { openMicRequested = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkOpenMicIntent(intent)
    }

    private fun checkOpenMicIntent(intent: Intent?) {
        if (intent != null) {
            val isMicAction = intent.action == NttTileService.ACTION_OPEN_MIC ||
                    intent.getBooleanExtra(NttTileService.EXTRA_OPEN_MIC, false)
            if (isMicAction) {
                openMicRequested = true
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        voiceRecognizer?.destroy()
    }
}
