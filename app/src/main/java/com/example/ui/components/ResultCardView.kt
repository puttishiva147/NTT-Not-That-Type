package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.language.UnderstoodResult
import com.example.speech.NttTtsManager
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer

@Composable
fun ResultCardView(
    result: UnderstoodResult,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onSpeak: (() -> Unit)? = null,
    onExplainWithGemini: ((String) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val fullSpokenText = NttTtsManager.buildIdentifiedNumberSpeech(result)

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_card_${result.numericValue}"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row: Language badge + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SagePrimaryContainer.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SagePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = result.languageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = SagePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Actions row: Speak full read-back, Copy, Favorite
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ListenSpeakerButton(
                        text = result.scriptText,
                        spokenText = fullSpokenText,
                        languageTag = result.languageCode,
                        size = 40.dp,
                        onSpeakFallback = onSpeak?.let { cb -> { _ -> cb() } },
                        modifier = Modifier.testTag("btn_speak_${result.numericValue}")
                    )

                    IconButton(
                        onClick = {
                            val copyText = "${result.primaryNumber} — ${result.scriptText} (${result.phoneticText}) — ${result.englishMeaning}"
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_copy_${result.numericValue}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy understood result",
                            tint = CharcoalMedium,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_fav_${result.numericValue}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Save to favorites",
                                tint = if (isFavorite) WarmAmber else MutedText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (onExplainWithGemini != null) {
                        IconButton(
                            onClick = { onExplainWithGemini(result.scriptText) },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_explain_gemini_${result.numericValue}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Explain with Gemini AI",
                                tint = WarmAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Primary Number (The strongest visual element)
            Text(
                text = result.primaryNumber,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (result.primaryNumber.length > 7) 38.sp else 48.sp,
                    letterSpacing = (-1).sp
                ),
                color = CharcoalText,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("text_primary_number")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Regional Script with dedicated Listen Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.scriptText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp
                    ),
                    color = SagePrimary,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag("text_script_word")
                )
                Spacer(modifier = Modifier.width(8.dp))
                ListenSpeakerButton(
                    text = result.scriptText,
                    spokenText = result.scriptText,
                    languageTag = result.languageCode,
                    showLabel = true,
                    labelText = "Read Script",
                    onSpeakFallback = onSpeak?.let { cb -> { _ -> cb() } }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Phonetic Pronunciation (Italic) with compact listen speaker
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.phoneticText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    ),
                    color = CharcoalMedium,
                    modifier = Modifier.testTag("text_phonetic_pronunciation")
                )
                Spacer(modifier = Modifier.width(4.dp))
                ListenSpeakerButton(
                    text = result.phoneticText,
                    spokenText = result.phoneticText,
                    languageTag = result.languageCode,
                    size = 30.dp,
                    onSpeakFallback = onSpeak?.let { cb -> { _ -> cb() } }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // English Meaning
            Text(
                text = result.englishMeaning,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MutedText,
                modifier = Modifier.testTag("text_english_meaning")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Read-Back Pills Bar: Full Read, Regional Only, English Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListenSpeakerButton(
                    text = "Full",
                    spokenText = fullSpokenText,
                    languageTag = result.languageCode,
                    showLabel = true,
                    labelText = "🔊 Read All",
                    containerColor = SagePrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.testTag("btn_read_all_${result.numericValue}")
                )

                ListenSpeakerButton(
                    text = "English",
                    spokenText = result.englishMeaning,
                    languageTag = "en-IN",
                    showLabel = true,
                    labelText = "🇬🇧 English",
                    containerColor = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.testTag("btn_read_english_${result.numericValue}")
                )
            }

            // Numerical Breakdown if available (e.g. 50 + 5)
            if (result.breakdown.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x124A6353),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Breakdown:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = result.breakdown,
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        ListenSpeakerButton(
                            text = result.breakdown,
                            spokenText = result.breakdown,
                            languageTag = result.languageCode,
                            size = 28.dp,
                            modifier = Modifier.testTag("btn_read_breakdown_${result.numericValue}")
                        )
                    }
                }
            }
        }
    }
}
