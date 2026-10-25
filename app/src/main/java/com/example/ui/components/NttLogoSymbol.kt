package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Universal NTT Logo Component.
 * Ensures the exact same brand logo appears on the Android home screen,
 * in-app home screen, floating overlay, and throughout the entire application.
 */
@Composable
fun NttLogoSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isCircular: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (isCircular) Modifier.clip(CircleShape)
                else Modifier.clip(RoundedCornerShape(size * 0.26f))
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_ntt_universal_logo),
            contentDescription = "NTT Universal Logo",
            modifier = Modifier.size(size)
        )
    }
}
