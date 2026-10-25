package com.example.ui.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.language.NttLanguage
import com.example.model.BundledScenarios
import com.example.model.TestScenario
import com.example.overlay.OverlayStateHolder
import com.example.search.NumberTextParser
import com.example.ui.components.FloatingNttOverlayComposeContent
import com.example.ui.components.GlassSurface
import com.example.ui.components.ListenSpeakerButton
import com.example.ui.components.NttLogoSymbol
import com.example.ui.components.ResultCardView
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

@Composable
fun TestScenariosScreen(
    selectedLanguage: NttLanguage,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeScenario by remember { mutableStateOf(BundledScenarios.ALL.first()) }
    var isSimulatingOverlay by remember { mutableStateOf(false) }

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
                text = "Practice & Test Scenarios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Text(
                text = "Test NTT against real-life everyday situations in India. Select a scenario below to test.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }

        // Active Simulation Frame
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scenario_simulation_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Category pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SagePrimaryContainer
                    ) {
                        Text(
                            text = activeScenario.contextCategory,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SagePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeScenario.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activeScenario.contextDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mock Screen Display container
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SCREEN DISPLAY SIMULATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MutedText
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            activeScenario.screenElements.forEach { elem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = elem.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CharcoalMedium
                                    )
                                    Text(
                                        text = elem.value,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (elem.isPrimary) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (elem.isPrimary) SagePrimary else CharcoalText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SagePrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Spoken / Heard:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SagePrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "“${activeScenario.spokenOrVisibleText}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                ListenSpeakerButton(
                                    text = activeScenario.spokenOrVisibleText,
                                    languageTag = selectedLanguage.ttsLocaleTag,
                                    size = 32.dp,
                                    onSpeakFallback = onSpeak
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action to trigger NTT overlay
                    Button(
                        onClick = {
                            val detected = NumberTextParser.extractNumbersAndPrices(
                                activeScenario.spokenOrVisibleText + " " + activeScenario.screenElements.joinToString(" ") { "${it.label} ${it.value}" },
                                selectedLanguage.code
                            )
                            OverlayStateHolder.showPanelWithItems(detected)
                            isSimulatingOverlay = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_test_activate_ntt"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        NttLogoSymbol(size = 24.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap NTT to Understand",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Interactive embedded overlay demo
                    AnimatedVisibility(
                        visible = isSimulatingOverlay,
                        enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.90f),
                        exit = fadeOut(animationSpec = tween(280)) + scaleOut(targetScale = 0.90f)
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            FloatingNttOverlayComposeContent(
                                languageCode = selectedLanguage.code,
                                onClose = { isSimulatingOverlay = false },
                                onSpeak = onSpeak
                            )
                        }
                    }
                }
            }
        }

        // List of all Scenarios
        item {
            Text(
                text = "Choose a scenario to practice:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
        }

        items(BundledScenarios.ALL) { scenario ->
            val isSelected = scenario.id == activeScenario.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        activeScenario = scenario
                        isSimulatingOverlay = false
                    }
                    .testTag("card_scenario_${scenario.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SagePrimaryContainer.copy(alpha = 0.6f) else IvorySurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) SagePrimary else SubtleBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SagePrimary else SagePrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${scenario.primaryExpectedNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SagePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scenario.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = scenario.contextDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                            maxLines = 1
                        )
                    }

                    ListenSpeakerButton(
                        text = scenario.spokenOrVisibleText,
                        languageTag = selectedLanguage.ttsLocaleTag,
                        size = 36.dp,
                        onSpeakFallback = onSpeak
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isSelected) SagePrimary else MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
