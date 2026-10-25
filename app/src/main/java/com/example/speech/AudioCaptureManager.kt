package com.example.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

sealed class AudioCaptureState {
    object Idle : AudioCaptureState()
    data class Recording(
        val durationSeconds: Int,
        val amplitude: Float,
        val partialTranscript: String = ""
    ) : AudioCaptureState()
    data class Captured(
        val audioFile: File?,
        val audioBase64: String?,
        val recognizedText: String?
    ) : AudioCaptureState()
    data class Processing(val message: String) : AudioCaptureState()
    data class Error(val message: String) : AudioCaptureState()
}

class AudioCaptureManager(private val context: Context) {

    private val tag = "AudioCaptureManager"
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var amplitudeJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _captureState = MutableStateFlow<AudioCaptureState>(AudioCaptureState.Idle)
    val captureState: StateFlow<AudioCaptureState> = _captureState.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private var recognizedSpeechText: String? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startCapture(languageCode: String = "hi-IN") {
        if (!hasPermission()) {
            _captureState.value = AudioCaptureState.Error("Microphone permission is required to capture voice.")
            return
        }

        stopCaptureInternal()
        recognizedSpeechText = null

        val outputFile = try {
            val cacheDir = context.cacheDir
            File(cacheDir, "voice_regional_${System.currentTimeMillis()}.mp4").apply {
                if (exists()) delete()
            }
        } catch (e: Exception) {
            _captureState.value = AudioCaptureState.Error("Unable to create audio cache file: ${e.message}")
            return
        }

        currentOutputFile = outputFile

        // 1. Initialize MediaRecorder for direct Gemini Multimodal Audio
        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
        } catch (e: Exception) {
            Log.w(tag, "MediaRecorder failed, falling back to speech recognition: ${e.message}")
            mediaRecorder = null
        }

        // 2. Start SpeechRecognizer for immediate live transcription
        startSpeechRecognition(languageCode)

        // 3. Launch amplitude polling & duration ticker
        startAmplitudeTracking()
    }

    private fun startSpeechRecognition(languageCode: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {}
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {
                                // If mediaRecorder didn't initialize, use RMS dB for amplitude
                                if (mediaRecorder == null) {
                                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                                    _currentAmplitude.value = normalized
                                }
                            }
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {}
                            override fun onError(error: Int) {
                                Log.d(tag, "SpeechRecognizer error code: $error")
                                // Non-fatal for audio recording; audio file is still preserved
                            }
                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()
                                if (!text.isNullOrBlank()) {
                                    recognizedSpeechText = text
                                }
                            }
                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val partial = matches?.firstOrNull()
                                if (!partial.isNullOrBlank()) {
                                    val currentState = _captureState.value
                                    if (currentState is AudioCaptureState.Recording) {
                                        _captureState.value = currentState.copy(partialTranscript = partial)
                                    }
                                }
                            }
                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    }
                    speechRecognizer?.startListening(intent)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to start speech recognizer: ${e.message}")
            }
        }
    }

    private fun startAmplitudeTracking() {
        amplitudeJob?.cancel()
        amplitudeJob = mainScope.launch {
            var elapsedMs = 0
            val maxDurationMs = 12000 // 12 seconds max per spoken phrase

            _captureState.value = AudioCaptureState.Recording(0, 0.1f)

            while (isActive && elapsedMs < maxDurationMs) {
                delay(80)
                elapsedMs += 80

                val ampNorm = try {
                    val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                    (maxAmp / 32767f).coerceIn(0f, 1f)
                } catch (e: Exception) {
                    0.15f
                }

                _currentAmplitude.value = ampNorm

                val seconds = elapsedMs / 1000
                val partial = (_captureState.value as? AudioCaptureState.Recording)?.partialTranscript ?: ""
                _captureState.value = AudioCaptureState.Recording(
                    durationSeconds = seconds,
                    amplitude = ampNorm,
                    partialTranscript = partial
                )
            }

            if (elapsedMs >= maxDurationMs) {
                stopCapture()
            }
        }
    }

    fun stopCapture() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _currentAmplitude.value = 0f

        _captureState.value = AudioCaptureState.Processing("Processing captured voice...")

        var base64String: String? = null
        val recordedFile = currentOutputFile

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w(tag, "MediaRecorder stop exception: ${e.message}")
                }
                release()
            }
            mediaRecorder = null

            if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
                val bytes = recordedFile.readBytes()
                base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing recorded audio file: ${e.message}")
        }

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }

        // Allow speech recognizer 300ms to dispatch final results
        mainScope.launch {
            delay(350)
            if (base64String != null || !recognizedSpeechText.isNullOrBlank()) {
                _captureState.value = AudioCaptureState.Captured(
                    audioFile = recordedFile,
                    audioBase64 = base64String,
                    recognizedText = recognizedSpeechText
                )
            } else {
                _captureState.value = AudioCaptureState.Error("No clear audio captured. Please try speaking again.")
            }
        }
    }

    fun reset() {
        stopCaptureInternal()
        _captureState.value = AudioCaptureState.Idle
        _currentAmplitude.value = 0f
        recognizedSpeechText = null
    }

    private fun stopCaptureInternal() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null

        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun destroy() {
        stopCaptureInternal()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
