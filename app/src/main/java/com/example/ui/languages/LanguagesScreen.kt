package com.example.ui.languages

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.language.LanguageDictionary
import com.example.language.NttLanguage
import com.example.language.SupportedLanguages
import com.example.ui.components.GlassSurface
import com.example.ui.components.ListenSpeakerButton
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagesScreen(
    currentLanguage: NttLanguage,
    onSelectLanguage: (NttLanguage) -> Unit,
    onSpeak: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategoryTab by remember { mutableIntStateOf(0) } // 0: 0-100 Table, 1: Scales (Lakh/Crore), 2: Fractions & Currency

    val sampleFractions = listOf(
        Triple("1.5 (One and a half)", "डेढ़", "Dedh"),
        Triple("2.5 (Two and a half)", "ढाई", "Dhai"),
        Triple("1.25 (One and a quarter)", "सवा", "Sawa"),
        Triple("0.75 / -0.25 (Quarter to)", "पौने", "Paune"),
        Triple("+0.5 (Half past)", "साढ़े", "Saadhe")
    )

    val sampleScales = listOf(
        Triple("100", "सौ / Hundred", "Sau"),
        Triple("1,000", "हज़ार / Thousand", "Hazaar"),
        Triple("1,00,000", "लाख / Lakh", "Lakh"),
        Triple("1,00,00,000", "करोड़ / Crore", "Crore")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            if (onNavigateBack != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CharcoalText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Languages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }

            Text(
                text = "11 Indian Languages",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Text(
                text = "Choose your primary language for NTT translations and explore regional number pronunciation charts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Language Selector Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SupportedLanguages.ALL.forEach { lang ->
                    val isSelected = lang.code.equals(currentLanguage.code, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLanguage(lang) },
                        label = {
                            Text(
                                text = "${lang.englishName} (${lang.nativeName})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SagePrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = IvorySurface,
                            labelColor = CharcoalText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SubtleBorder,
                            selectedBorderColor = SagePrimary
                        ),
                        modifier = Modifier.testTag("chip_lang_${lang.code}")
                    )
                }
            }
        }

        // Selected Language Spotlight
        item {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Language",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentLanguage.englishName} • ${currentLanguage.nativeName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = "Script: ${currentLanguage.scriptName} • e.g. ${currentLanguage.sampleScript} (${currentLanguage.samplePhonetic})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SagePrimary
                        )
                    }

                    ListenSpeakerButton(
                        text = currentLanguage.sampleScript,
                        languageTag = currentLanguage.ttsLocaleTag,
                        showLabel = true,
                        labelText = "Listen",
                        onSpeakFallback = onSpeak
                    )
                }
            }
        }

        // Subcategory Tabs (0-100 Table, Scales, Fractions)
        item {
            TabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = IvorySurface,
                contentColor = SagePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                        color = SagePrimary
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedCategoryTab == 0,
                    onClick = { selectedCategoryTab = 0 },
                    text = { Text("0–100 Table", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedCategoryTab == 1,
                    onClick = { selectedCategoryTab = 1 },
                    text = { Text("Scales", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedCategoryTab == 2,
                    onClick = { selectedCategoryTab = 2 },
                    text = { Text("Fractions", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab 0: Numbers Reference Table
        if (selectedCategoryTab == 0) {
            val displayNumbers = (0..100).toList()

            items(displayNumbers) { num ->
                val wordData = LanguageDictionary.getWordData(currentLanguage.code, num)
                val englishWord = LanguageDictionary.getEnglishWord(num)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("row_number_$num"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IvorySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SagePrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$num",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SagePrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = wordData?.script ?: "$num",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "${wordData?.phonetic ?: ""} (${englishWord.script})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MutedText
                                )
                            }
                        }

                        ListenSpeakerButton(
                            text = wordData?.script ?: "$num",
                            languageTag = currentLanguage.ttsLocaleTag,
                            size = 38.dp,
                            onSpeakFallback = onSpeak
                        )
                    }
                }
            }
        } else if (selectedCategoryTab == 1) {
            // Scales Tab
            items(sampleScales) { scale ->
                val scaleWord = when (scale.first) {
                    "100" -> LanguageDictionary.getScaleWord(currentLanguage.code, 100L)
                    "1,000" -> LanguageDictionary.getScaleWord(currentLanguage.code, 1000L)
                    "1,00,000" -> LanguageDictionary.getScaleWord(currentLanguage.code, 100000L)
                    else -> LanguageDictionary.getScaleWord(currentLanguage.code, 10000000L)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IvorySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = scale.first,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                            Text(
                                text = "${scaleWord.script} (${scaleWord.phonetic})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                            Text(
                                text = scale.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ListenSpeakerButton(
                            text = scaleWord.script,
                            languageTag = currentLanguage.ttsLocaleTag,
                            showLabel = true,
                            labelText = "Listen",
                            onSpeakFallback = onSpeak
                        )
                    }
                }
            }
        } else {
            // Fractions & Colloquial Tab
            items(sampleFractions) { frac ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IvorySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "${frac.second} (${frac.third})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = frac.first,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedText
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ListenSpeakerButton(
                            text = frac.second,
                            languageTag = currentLanguage.ttsLocaleTag,
                            showLabel = true,
                            labelText = "Listen",
                            onSpeakFallback = onSpeak
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
