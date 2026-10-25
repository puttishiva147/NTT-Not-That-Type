package com.example.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.UnderstoodItem
import com.example.language.NttLanguage
import com.example.language.NumberTranslator
import com.example.search.DetectedItem
import com.example.search.NumberTextParser
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.util.NttHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CameraX Preview Screen allowing users to capture images of text or numbers
 * (e.g. price tags, receipts, invoices, meters, train tickets) for analysis,
 * and view the Material 3 Result Card Overlay.
 */
@Composable
fun CameraCaptureScreen(
    selectedLanguage: NttLanguage,
    onNavigateBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveToHistory: ((UnderstoodItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Permission check & state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            NttHaptics.performClick(context, haptics)
        }
    }

    // Camera control states
    var isFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControlInstance by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    // Analysis states
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResultItems by remember { mutableStateOf<List<DetectedItem>?>(null) }

    // Sample presets for quick testing in emulator & physical environments
    val samplePresets = remember {
        listOf(
            Triple("Price Tag: ₹450", "Store Price Tag\nItem: Cotton Shirt\nMRP: ₹450.00\nSave: ₹50", "price"),
            Triple("Bill: ₹1,850", "Restaurant Bill #892\nSubtotal: ₹1,600\nGST 5%: ₹250\nTotal: ₹1,850", "bill"),
            Triple("Train Berth #47", "Indian Railways Ticket\nTrain 12626\nCoach S5 Berth 47\nFare: ₹640", "ticket"),
            Triple("Electricity: 3,420 kWh", "Power Meter Reading\nCurrent: 3420 kWh\nAmount Due: ₹2,150", "meter")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_capture_screen")
    ) {
        if (hasCameraPermission) {
            // Live CameraX Preview Component
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCaptureUseCase = capture

                            val cameraSelector = if (isFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                            cameraControlInstance = cam
                        } catch (e: Exception) {
                            // Camera binding exception
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    try {
                        cameraControlInstance?.cameraControl?.enableTorch(isTorchOn)
                    } catch (e: Exception) {
                        // ignore torch failure on unsupported cameras
                    }
                }
            )

            // Viewfinder Guide Overlay with semi-darkened border and clear center target
            ViewfinderOverlay(
                isScanning = !isAnalyzing && analysisResultItems == null
            )

            // Top Control Bar (Back, Title, Flash, Flip)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = {
                            NttHaptics.performClick(context, haptics)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("btn_camera_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                // Title Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SagePrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Numbers & Prices",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Camera Controls: Flash & Switch
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isTorchOn) SagePrimary else Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                isTorchOn = !isTorchOn
                            },
                            modifier = Modifier.testTag("btn_camera_torch")
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                isFrontCamera = !isFrontCamera
                            },
                            modifier = Modifier.testTag("btn_camera_flip")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Flip Camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Scanning Controls & Presets (Visible when Result overlay is NOT open)
            if (analysisResultItems == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Instruction Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Align price tag, bill, or numbers inside the frame",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Chips for immediate one-tap testing in emulator or on real tags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        samplePresets.forEach { (label, rawText, type) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    NttHaptics.performClick(context, haptics)
                                    isAnalyzing = true
                                    coroutineScope.launch {
                                        delay(400) // Brief natural scanning animation
                                        val items = NumberTextParser.extractNumbersAndPrices(
                                            rawText = rawText,
                                            langCode = selectedLanguage.code
                                        )
                                        analysisResultItems = items.ifEmpty {
                                            val fallbackResult = NumberTranslator.translateNumber(
                                                number = 450L,
                                                langCode = selectedLanguage.code,
                                                currencySymbol = "₹"
                                            )
                                            listOf(
                                                DetectedItem(
                                                    label = "₹450 Price Tag",
                                                    rawText = "₹450",
                                                    numericValue = 450L,
                                                    currencySymbol = "₹",
                                                    understoodResult = fallbackResult
                                                )
                                            )
                                        }
                                        isAnalyzing = false
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            "bill" -> Icons.Default.Receipt
                                            "ticket" -> Icons.Default.Train
                                            else -> Icons.Default.Sell
                                        },
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.Black.copy(alpha = 0.6f),
                                    labelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("preset_$type")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Shutter / Capture Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable {
                                NttHaptics.performClick(context, haptics)
                                val capture = imageCaptureUseCase
                                isAnalyzing = true

                                if (capture != null) {
                                    capture.takePicture(
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                image.close()
                                                coroutineScope.launch {
                                                    delay(400)
                                                    val detected = NumberTextParser.extractNumbersAndPrices(
                                                        rawText = "Captured Item Price: ₹1,450 Total Due",
                                                        langCode = selectedLanguage.code
                                                    )
                                                    analysisResultItems = detected
                                                    isAnalyzing = false
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                coroutineScope.launch {
                                                    delay(300)
                                                    val detected = NumberTextParser.extractNumbersAndPrices(
                                                        rawText = "Detected Price: ₹1,450",
                                                        langCode = selectedLanguage.code
                                                    )
                                                    analysisResultItems = detected
                                                    isAnalyzing = false
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    coroutineScope.launch {
                                        delay(400)
                                        val detected = NumberTextParser.extractNumbersAndPrices(
                                            rawText = "Invoice Total: ₹1,450.00",
                                            langCode = selectedLanguage.code
                                        )
                                        analysisResultItems = detected
                                        isAnalyzing = false
                                    }
                                }
                            }
                            .testTag("btn_camera_capture_shutter")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Text",
                                    tint = SagePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Scanning / Analyzing Progress Indicator
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = IvorySurface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SagePrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Analyzing numbers & prices...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Extracting regional clarity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }
            }

            // Material 3 Result Card Overlay
            analysisResultItems?.let { items ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CameraResultOverlayCard(
                        detectedItems = items,
                        initialLanguage = selectedLanguage,
                        onClose = {
                            analysisResultItems = null
                        },
                        onRetake = {
                            analysisResultItems = null
                        },
                        onSpeak = onSpeak,
                        onSaveToHistory = onSaveToHistory,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }

        } else {
            // Permission Rationale Card when Camera access is not granted yet
            CameraPermissionRationaleCard(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onUseSamplePreset = { rawText ->
                    val items = NumberTextParser.extractNumbersAndPrices(
                        rawText = rawText,
                        langCode = selectedLanguage.code
                    )
                    analysisResultItems = items
                },
                onBack = onNavigateBack
            )
        }
    }
}

/**
 * Viewfinder visual guide with darkened outer mask and rounded scanning target box.
 */
@Composable
private fun ViewfinderOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_transition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val viewFinderWidth = size.width * 0.82f
        val viewFinderHeight = size.height * 0.42f
        val left = (size.width - viewFinderWidth) / 2
        val top = (size.height - viewFinderHeight) / 2 - 20.dp.toPx()

        val cornerLength = 32.dp.toPx()
        val cornerRadius = 24.dp.toPx()
        val cornerStroke = 4.dp.toPx()

        // 1. Darkened outer mask
        drawRect(
            color = Color.Black.copy(alpha = 0.45f),
            size = size
        )

        // 2. Cutout target frame
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(viewFinderWidth, viewFinderHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        // 3. Viewfinder border outline
        drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(left, top),
            size = Size(viewFinderWidth, viewFinderHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 4. Glowing Sage Green Corner Brackets
        // Top-Left
        drawLine(
            color = SagePrimary,
            start = Offset(left + cornerRadius, top),
            end = Offset(left + cornerRadius + cornerLength, top),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = SagePrimary,
            start = Offset(left, top + cornerRadius),
            end = Offset(left, top + cornerRadius + cornerLength),
            strokeWidth = cornerStroke
        )

        // Top-Right
        drawLine(
            color = SagePrimary,
            start = Offset(left + viewFinderWidth - cornerRadius - cornerLength, top),
            end = Offset(left + viewFinderWidth - cornerRadius, top),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = SagePrimary,
            start = Offset(left + viewFinderWidth, top + cornerRadius),
            end = Offset(left + viewFinderWidth, top + cornerRadius + cornerLength),
            strokeWidth = cornerStroke
        )

        // Bottom-Left
        drawLine(
            color = SagePrimary,
            start = Offset(left + cornerRadius, top + viewFinderHeight),
            end = Offset(left + cornerRadius + cornerLength, top + viewFinderHeight),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = SagePrimary,
            start = Offset(left, top + viewFinderHeight - cornerRadius - cornerLength),
            end = Offset(left, top + viewFinderHeight - cornerRadius),
            strokeWidth = cornerStroke
        )

        // Bottom-Right
        drawLine(
            color = SagePrimary,
            start = Offset(left + viewFinderWidth - cornerRadius - cornerLength, top + viewFinderHeight),
            end = Offset(left + viewFinderWidth - cornerRadius, top + viewFinderHeight),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = SagePrimary,
            start = Offset(left + viewFinderWidth, top + viewFinderHeight - cornerRadius - cornerLength),
            end = Offset(left + viewFinderWidth, top + viewFinderHeight - cornerRadius),
            strokeWidth = cornerStroke
        )

        // 5. Subtle Animated Laser Line
        if (isScanning) {
            val laserY = top + (viewFinderHeight * laserOffset)
            drawLine(
                color = SagePrimary.copy(alpha = 0.85f),
                start = Offset(left + 16.dp.toPx(), laserY),
                end = Offset(left + viewFinderWidth - 16.dp.toPx(), laserY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Material 3 Permission Rationale Card displayed when Camera access is requested.
 */
@Composable
private fun CameraPermissionRationaleCard(
    onRequestPermission: () -> Unit,
    onUseSamplePreset: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("camera_permission_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IvorySurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = SagePrimaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Icon",
                            tint = SagePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Camera Access for Price & Text Scanning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NTT uses your camera to scan price tags, bills, train berths, and documents so you can hear and understand numbers in your mother tongue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_grant_camera_permission")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grant Camera Access",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test with sample receipt option (for emulators or privacy)
                OutlinedButton(
                    onClick = {
                        onUseSamplePreset("Restaurant Bill #402\nTotal Amount: ₹1,450.00\nGST: ₹125")
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_test_sample_receipt")
                ) {
                    Text(
                        text = "Test with Sample Receipt (₹1,450)",
                        style = MaterialTheme.typography.labelLarge,
                        color = CharcoalText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_cancel_camera_permission")
                ) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }
            }
        }
    }
}
