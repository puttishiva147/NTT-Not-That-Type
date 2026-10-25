package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiRegionalService
import com.example.data.UnderstoodItem
import com.example.model.RegionalWordExplanation
import com.example.speech.AudioCaptureManager
import com.example.speech.AudioCaptureState
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer
import com.example.ui.util.NttHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceRegionalExplainerSheet(
    onDismiss: () -> Unit,
    onSaveItem: ((UnderstoodItem) -> Unit)? = null,
    onSpeak: ((String) -> Unit)? = null,
    initialQuery: String? = null,
    languageLocaleTag: String = "hi-IN",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val audioManager = remember { AudioCaptureManager(context) }
    val geminiService = remember { GeminiRegionalService() }

    val captureState by audioManager.captureState.collectAsState()
    val amplitude by audioManager.currentAmplitude.collectAsState()

    var isAnalyzing by remember { mutableStateOf(false) }
    var explanationResult by remember { mutableStateOf<RegionalWordExplanation?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf(initialQuery ?: "") }
    var showManualInput by remember { mutableStateOf(false) }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedMessage = null
            audioManager.startCapture(languageLocaleTag)
        } else {
            permissionDeniedMessage = "Microphone access is needed to hear spoken regional words."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }

    // Auto-analyze when audio or speech is captured
    LaunchedEffect(captureState) {
        val state = captureState
        if (state is AudioCaptureState.Captured) {
            isAnalyzing = true
            coroutineScope.launch {
                val queryText = state.recognizedText ?: manualInputText.ifBlank { null }
                val result = geminiService.explainRegionalWord(
                    textQuery = queryText,
                    audioBase64 = state.audioBase64,
                    languageHint = languageLocaleTag
                )
                isAnalyzing = false
                result.onSuccess { explanation ->
                    explanationResult = explanation
                    isSaved = false
                }
            }
        }
    }

    // If an initial query was passed in, analyze it immediately
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            isAnalyzing = true
            val result = geminiService.explainRegionalWord(
                textQuery = initialQuery,
                audioBase64 = null,
                languageHint = languageLocaleTag
            )
            isAnalyzing = false
            result.onSuccess { explanation ->
                explanationResult = explanation
                isSaved = false
            }
        }
    }

    val quickSampleChips = listOf(
        "Dedh sau",
        "Dhai hazar",
        "Jugaad",
        "Chillar",
        "Bindaas",
        "Paune teen",
        "Sawa sau",
        "Rokda"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IvoryBackground,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                color = SubtleBorder,
                shape = RoundedCornerShape(4.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 40.dp, height = 4.dp))
            }
        },
        modifier = modifier.testTag("modal_voice_gemini_explainer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Gemini AI Badge + Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SagePrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gemini AI Free Tier",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        NttHaptics.performTick(context, haptics)
                        onDismiss()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CharcoalMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Voice Regional Explainer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CharcoalText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Capture spoken audio from your microphone to translate and understand slang, numbers, and regional phrases.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Microphone Capture Centerpiece
            val isRecording = captureState is AudioCaptureState.Recording

            val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.25f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            val smoothAmp by animateFloatAsState(
                targetValue = amplitude.coerceIn(0.1f, 1f),
                label = "amplitude_wave"
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing animated halo while recording
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(WarmAmber.copy(alpha = 0.22f))
                    )
                    Box(
                        modifier = Modifier
                            .size((95 + (smoothAmp * 25)).dp)
                            .clip(CircleShape)
                            .background(SagePrimary.copy(alpha = 0.35f))
                    )
                }

                // Core Microphone Button
                Surface(
                    shape = CircleShape,
                    color = if (isRecording) WarmAmber else SagePrimary,
                    shadowElevation = if (isRecording) 12.dp else 6.dp,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable {
                            NttHaptics.performClick(context, haptics)
                            if (isRecording) {
                                audioManager.stopCapture()
                            } else {
                                if (audioManager.hasPermission()) {
                                    audioManager.startCapture(languageLocaleTag)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("btn_mic_capture_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop listening" else "Start recording voice",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            // Live status & amplitude waveform feedback
            if (isRecording) {
                val recState = captureState as AudioCaptureState.Recording
                Text(
                    text = "Listening... (${recState.durationSeconds}s / 12s)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmAmber
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive amplitude waveform visualizer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(26.dp)
                ) {
                    val bars = 9
                    for (i in 0 until bars) {
                        val factor = (Math.sin((i.toDouble() / bars) * Math.PI) * smoothAmp).toFloat()
                        val barHeight = (6 + (factor * 20)).dp
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SagePrimary)
                        )
                    }
                }

                if (recState.partialTranscript.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${recState.partialTranscript}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = CharcoalText,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        NttHaptics.performClick(context, haptics)
                        audioManager.stopCapture()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tap when done speaking", color = CharcoalText)
                }
            } else if (isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SagePrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gemini is translating and extracting nuances...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SagePrimary
                    )
                }
            } else {
                Text(
                    text = "Tap the microphone to speak",
                    style = MaterialTheme.typography.labelLarge,
                    color = MutedText
                )
            }

            permissionDeniedMessage?.let { errorMsg ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0x1FDD5A5A),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC0392B),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Gemini Result Card View
            AnimatedVisibility(
                visible = explanationResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                explanationResult?.let { exp ->
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_gemini_explanation_result"),
                        shape = RoundedCornerShape(20.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Top Row: Language pill + Gemini status badge + Listen button + Save
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SagePrimaryContainer.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = exp.language,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SagePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fullMeaningSpeech = buildString {
                                        append(exp.word)
                                        if (exp.phonetic.isNotBlank()) append(". Pronounced ${exp.phonetic}")
                                        append(". Means ${exp.englishTranslation}")
                                        if (exp.explanation.isNotBlank()) append(". ${exp.explanation}")
                                    }

                                    // 🔊 Listen Speaker Button: Reads word and full meaning
                                    ListenSpeakerButton(
                                        text = exp.word,
                                        spokenText = fullMeaningSpeech,
                                        size = 36.dp,
                                        onSpeakFallback = onSpeak
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Bookmark/Save button
                                    if (onSaveItem != null) {
                                        IconButton(
                                            onClick = {
                                                NttHaptics.performClick(context, haptics)
                                                isSaved = !isSaved
                                                if (isSaved) {
                                                    val understoodItem = UnderstoodItem(
                                                        originalQuery = exp.word,
                                                        primaryNumber = exp.word,
                                                        numericValue = 0L,
                                                        languageCode = "hi",
                                                        languageName = exp.language,
                                                        scriptText = exp.word,
                                                        phoneticText = exp.phonetic,
                                                        englishMeaning = "${exp.englishTranslation} — ${exp.explanation}",
                                                        breakdown = exp.culturalNuances,
                                                        source = "gemini_voice",
                                                        isFavorite = true
                                                    )
                                                    onSaveItem(understoodItem)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Save to favorites",
                                                tint = if (isSaved) WarmAmber else MutedText,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Word in native script
                            Text(
                                text = exp.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )

                            // Romanized phonetic guide
                            if (exp.phonetic.isNotBlank()) {
                                Text(
                                    text = "Sounds like: ${exp.phonetic}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SagePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // English Translation
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IvorySurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "English Translation",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText
                                    )
                                    Text(
                                        text = exp.englishTranslation,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // In-depth explanation
                            Text(
                                text = "Meaning & Context",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalMedium
                            )
                            Text(
                                text = exp.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalText,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            // Cultural nuances & slang usage
                            if (exp.culturalNuances.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Cultural Nuances & Slang Usage",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmAmber
                                )
                                Text(
                                    text = exp.culturalNuances,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CharcoalText,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Practical conversation example
                            if (exp.exampleSentence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SagePrimaryContainer.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Example Usage:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SagePrimary
                                            )
                                            val exampleSpeech = buildString {
                                                append(exp.exampleSentence)
                                                if (exp.exampleTranslation.isNotBlank()) {
                                                    append(". Means: ${exp.exampleTranslation}")
                                                }
                                            }
                                            ListenSpeakerButton(
                                                text = exp.exampleSentence,
                                                spokenText = exampleSpeech,
                                                size = 30.dp,
                                                onSpeakFallback = onSpeak
                                            )
                                        }
                                        Text(
                                            text = exp.exampleSentence,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CharcoalText
                                        )
                                        if (exp.exampleTranslation.isNotBlank()) {
                                            Text(
                                                text = "\"${exp.exampleTranslation}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontStyle = FontStyle.Italic,
                                                color = MutedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Quick sample chips to tap & explore
            Text(
                text = "Or try speaking one of these:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MutedText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickSampleChips.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GlassCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                        modifier = Modifier
                            .clickable {
                                NttHaptics.performTick(context, haptics)
                                manualInputText = chipText
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val res = geminiService.explainRegionalWord(
                                        textQuery = chipText,
                                        audioBase64 = null,
                                        languageHint = languageLocaleTag
                                    )
                                    isAnalyzing = false
                                    res.onSuccess { explanation ->
                                        explanationResult = explanation
                                        isSaved = false
                                    }
                                }
                            }
                            .testTag("chip_sample_$chipText")
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Toggle manual text input field
            if (!showManualInput) {
                OutlinedButton(
                    onClick = { showManualInput = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Type a word or phrase instead", color = CharcoalText)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        placeholder = { Text("e.g. kadak, jugaad, dedh sau...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_manual_regional_word"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SagePrimary,
                            unfocusedBorderColor = SubtleBorder,
                            focusedContainerColor = IvorySurface,
                            unfocusedContainerColor = IvorySurface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (manualInputText.isNotBlank()) {
                                NttHaptics.performClick(context, haptics)
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val res = geminiService.explainRegionalWord(
                                        textQuery = manualInputText,
                                        audioBase64 = null,
                                        languageHint = languageLocaleTag
                                    )
                                    isAnalyzing = false
                                    res.onSuccess { explanation ->
                                        explanationResult = explanation
                                        isSaved = false
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                        modifier = Modifier.testTag("btn_submit_manual_explain")
                    ) {
                        Text("Explain", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
