package com.example.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnderstoodItem
import com.example.language.NttLanguage
import com.example.language.UnderstoodResult
import com.example.search.PhoneticMatcher
import com.example.search.SearchMatch
import com.example.speech.NttTtsManager
import com.example.speech.VoiceRecognizerHelper
import com.example.speech.VoiceState
import com.example.ui.components.GlassSurface
import com.example.ui.components.ListenSpeakerButton
import com.example.ui.components.ResultCardView
import com.example.ui.components.VoiceRegionalExplainerSheet
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    selectedLanguage: NttLanguage,
    voiceRecognizer: VoiceRecognizerHelper?,
    onSaveToHistory: (UnderstoodItem) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchMatch>>(emptyList()) }
    var activeResult by remember { mutableStateOf<UnderstoodResult?>(null) }
    var isFavorited by remember { mutableStateOf(false) }
    var showVoiceExplainer by remember { mutableStateOf(false) }
    var explainerInitialQuery by remember { mutableStateOf<String?>(null) }

    val voiceState by voiceRecognizer?.voiceState?.collectAsState() ?: remember { mutableStateOf(VoiceState.Idle) }

    // Quick suggestions requested in Section 18
    val exampleChips = listOf(
        "pachpan",
        "sattavan",
        "athasi",
        "ninyanave",
        "dedh sau",
        "dhai hazaar",
        "₹55",
        "103"
    )

    LaunchedEffect(searchQuery, selectedLanguage) {
        if (searchQuery.isNotBlank()) {
            val matches = PhoneticMatcher.search(searchQuery, selectedLanguage.code)
            searchResults = matches
            if (matches.isNotEmpty()) {
                activeResult = matches.first().result
                isFavorited = false
            }
        } else {
            searchResults = emptyList()
            activeResult = null
        }
    }

    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Success) {
            val recognized = (voiceState as VoiceState.Success).recognizedText
            searchQuery = recognized
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Search a number or word",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Text(
                text = "Type how it sounds in English letters, or search any numeral.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Search TextField with Voice Mic Button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_phonetic_search"),
                placeholder = { Text("Type how it sounds... (e.g. pachpan)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = SagePrimary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MutedText
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                explainerInitialQuery = searchQuery.ifBlank { null }
                                showVoiceExplainer = true
                            },
                            modifier = Modifier.testTag("btn_search_gemini_explainer")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Explain with Gemini AI",
                                tint = WarmAmber
                            )
                        }

                        IconButton(
                            onClick = {
                                if (voiceState is VoiceState.Listening) {
                                    voiceRecognizer?.stopListening()
                                } else {
                                    voiceRecognizer?.startListening(selectedLanguage.ttsLocaleTag)
                                }
                            },
                            modifier = Modifier.testTag("btn_voice_search")
                        ) {
                            Icon(
                                imageVector = if (voiceState is VoiceState.Listening) Icons.Default.Hearing else Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = if (voiceState is VoiceState.Listening) WarmAmber else SagePrimary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    val matches = PhoneticMatcher.search(searchQuery, selectedLanguage.code)
                    searchResults = matches
                    if (matches.isNotEmpty()) {
                        activeResult = matches.first().result
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SagePrimary,
                    unfocusedBorderColor = SubtleBorder,
                    focusedContainerColor = IvorySurface,
                    unfocusedContainerColor = IvorySurface
                )
            )

            // Voice Listening / Status Indicator
            AnimatedVisibility(visible = voiceState !is VoiceState.Idle) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (voiceState) {
                        is VoiceState.Listening -> WarmAmberContainer
                        is VoiceState.Processing -> SagePrimaryContainer
                        is VoiceState.Error -> Color(0x1FDD5A5A)
                        else -> SagePrimaryContainer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (voiceState is VoiceState.Listening || voiceState is VoiceState.Processing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (voiceState is VoiceState.Listening) WarmAmber else SagePrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = when (voiceState) {
                                is VoiceState.Listening -> "Listening... Say a number or price aloud."
                                is VoiceState.Processing -> "Processing speech..."
                                is VoiceState.Success -> "Recognized: ${(voiceState as VoiceState.Success).recognizedText}"
                                is VoiceState.Error -> (voiceState as VoiceState.Error).message
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Example Chips
            Text(
                text = "Popular examples:",
                style = MaterialTheme.typography.labelSmall,
                color = MutedText
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                exampleChips.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SagePrimaryContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                        modifier = Modifier.clickable { searchQuery = chipText }
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarmAmberContainer.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmAmber.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            explainerInitialQuery = searchQuery
                            showVoiceExplainer = true
                        }
                        .testTag("banner_explain_with_gemini")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = WarmAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Explain \"$searchQuery\" with Gemini AI",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                        }
                        Text(
                            text = "Explain",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarmAmber
                        )
                    }
                }
            }
        }

        // Active Result Card View
        val currentRes = activeResult
        if (currentRes != null) {
            item {
                Text(
                    text = "Understood Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                Spacer(modifier = Modifier.height(6.dp))

                ResultCardView(
                    result = currentRes,
                    isFavorite = isFavorited,
                    onToggleFavorite = {
                        isFavorited = !isFavorited
                        onSaveToHistory(
                            UnderstoodItem(
                                originalQuery = searchQuery,
                                primaryNumber = currentRes.primaryNumber,
                                numericValue = currentRes.numericValue,
                                currencySymbol = currentRes.currencySymbol,
                                languageCode = currentRes.languageCode,
                                languageName = currentRes.languageName,
                                scriptText = currentRes.scriptText,
                                phoneticText = currentRes.phoneticText,
                                englishMeaning = currentRes.englishMeaning,
                                breakdown = currentRes.breakdown,
                                isFavorite = isFavorited,
                                source = "search"
                            )
                        )
                    },
                    onSpeak = {
                        onSpeak(NttTtsManager.buildIdentifiedNumberSpeech(currentRes))
                    },
                    onExplainWithGemini = { text ->
                        explainerInitialQuery = text
                        showVoiceExplainer = true
                    }
                )
            }
        }

        // "Did you mean?" suggestions
        if (searchResults.size > 1) {
            item {
                Text(
                    text = "Did you mean?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(searchResults.drop(1)) { match ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            activeResult = match.result
                            isFavorited = false
                        }
                        .testTag("card_suggestion_${match.result.numericValue}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IvorySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "${match.result.primaryNumber} — ${match.result.scriptText}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "${match.result.phoneticText} (${match.result.englishMeaning})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ListenSpeakerButton(
                                text = match.result.scriptText,
                                languageTag = match.result.languageCode,
                                size = 36.dp,
                                onSpeakFallback = onSpeak
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SagePrimaryContainer
                            ) {
                                Text(
                                    text = "${(match.confidence * 100).toInt()}% match",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SagePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showVoiceExplainer) {
        VoiceRegionalExplainerSheet(
            onDismiss = { showVoiceExplainer = false },
            onSaveItem = onSaveToHistory,
            onSpeak = onSpeak,
            initialQuery = explainerInitialQuery,
            languageLocaleTag = selectedLanguage.ttsLocaleTag
        )
    }
}
