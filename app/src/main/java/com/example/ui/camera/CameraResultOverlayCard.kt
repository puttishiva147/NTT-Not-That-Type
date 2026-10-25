package com.example.ui.camera

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnderstoodItem
import com.example.language.NttLanguage
import com.example.language.NumberTranslator
import com.example.language.SupportedLanguages
import com.example.language.UnderstoodResult
import com.example.search.DetectedItem
import com.example.ui.components.ListenSpeakerButton
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder

/**
 * Material 3 Result Card Overlay that displays text/number analysis results from CameraX capture.
 * Features large detected number, regional script, phonetic pronunciation, mathematical breakdown,
 * TTS speech playback, multi-number carousel, and quick language switching.
 */
@Composable
fun CameraResultOverlayCard(
    detectedItems: List<DetectedItem>,
    initialLanguage: NttLanguage,
    onClose: () -> Unit,
    onRetake: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveToHistory: ((UnderstoodItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedItemIndex by remember { mutableIntStateOf(0) }
    var currentLanguage by remember { mutableStateOf(initialLanguage) }
    var isSaved by remember { mutableStateOf(false) }

    val activeItem = detectedItems.getOrNull(selectedItemIndex)
        ?: detectedItems.firstOrNull()

    // Dynamically translate active item into the selected language
    val activeResult: UnderstoodResult? = remember(activeItem, currentLanguage) {
        activeItem?.let {
            NumberTranslator.translateNumber(
                number = it.numericValue,
                langCode = currentLanguage.code,
                currencySymbol = it.currencySymbol
            )
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("camera_result_overlay_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IvorySurface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 1. Top Header: Camera Badge + Context Tag + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SagePrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera Analysis",
                                    tint = SagePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Analysis Result",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "${detectedItems.size} number(s) detected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_camera_overlay")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close overlay",
                            tint = CharcoalMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. If multiple numbers detected, display horizontal picker chips
                if (detectedItems.size > 1) {
                    Text(
                        text = "Select Detected Item:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        detectedItems.forEachIndexed { index, item ->
                            val isSelected = index == selectedItemIndex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedItemIndex = index
                                    isSaved = false
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SagePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SagePrimaryContainer.copy(alpha = 0.5f),
                                    labelColor = CharcoalText
                                ),
                                modifier = Modifier.testTag("chip_camera_detected_$index")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 3. Highlighted Primary Number Display
                if (activeResult != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = SagePrimaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Primary Number with Currency
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (activeResult.currencySymbol.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SagePrimary,
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Text(
                                            text = activeResult.currencySymbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = activeResult.primaryNumber,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 32.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalText,
                                    modifier = Modifier.testTag("text_camera_primary_number")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Native Script in selected language
                            Text(
                                text = activeResult.scriptText,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("text_camera_script")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Romanized Phonetic Pronunciation
                            Text(
                                text = "“${activeResult.phoneticText}”",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = CharcoalMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("text_camera_phonetic")
                            )

                            // Mathematical Place-Value Breakdown
                            if (activeResult.breakdown.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, SubtleBorder)
                                ) {
                                    Text(
                                        text = "= ${activeResult.breakdown}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Listen Speaker TTS Button + Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick TTS Audio Speaker Button
                        ListenSpeakerButton(
                            text = activeResult.scriptText,
                            languageTag = currentLanguage.ttsLocaleTag,
                            showLabel = true,
                            labelText = "Listen (${currentLanguage.nativeName})",
                            modifier = Modifier.testTag("btn_camera_listen_speaker")
                        )

                        // Language Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SagePrimaryContainer.copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentLanguage.englishName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SagePrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Horizontal Language Switcher Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SupportedLanguages.ALL.take(8).forEach { lang ->
                            val isLangSelected = lang.code == currentLanguage.code
                            FilterChip(
                                selected = isLangSelected,
                                onClick = {
                                    currentLanguage = lang
                                },
                                label = {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isLangSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SagePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = CharcoalMedium
                                ),
                                modifier = Modifier.testTag("chip_camera_lang_${lang.code}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Action Buttons: Save to History, Copy, Retake
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Save Button
                        OutlinedButton(
                            onClick = {
                                val itemToSave = UnderstoodItem(
                                    originalQuery = activeItem?.rawText ?: activeResult.primaryNumber,
                                    primaryNumber = activeResult.primaryNumber,
                                    numericValue = activeResult.numericValue,
                                    currencySymbol = activeResult.currencySymbol,
                                    languageCode = currentLanguage.code,
                                    languageName = currentLanguage.englishName,
                                    scriptText = activeResult.scriptText,
                                    phoneticText = activeResult.phoneticText,
                                    englishMeaning = activeResult.englishMeaning,
                                    breakdown = activeResult.breakdown,
                                    source = "camera"
                                )
                                onSaveToHistory?.invoke(itemToSave)
                                isSaved = true
                                Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSaved) SagePrimaryContainer else Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_camera_save_history")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSaved) "Saved" else "Save",
                                style = MaterialTheme.typography.labelMedium,
                                color = SagePrimary
                            )
                        }

                        // Copy Button
                        OutlinedButton(
                            onClick = {
                                val shareText = "${activeResult.primaryNumber} -> ${activeResult.scriptText} (${activeResult.phoneticText}) [NTT]"
                                clipboardManager.setText(AnnotatedString(shareText))
                                Toast.makeText(context, "Copied breakdown", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_camera_copy")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = CharcoalMedium,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelMedium,
                                color = CharcoalMedium
                            )
                        }

                        // Retake Button
                        Button(
                            onClick = onRetake,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_camera_retake")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Retake",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // No recognized numbers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No distinct numbers or prices recognized in frame.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRetake,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                        ) {
                            Text("Try Scanning Again", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
