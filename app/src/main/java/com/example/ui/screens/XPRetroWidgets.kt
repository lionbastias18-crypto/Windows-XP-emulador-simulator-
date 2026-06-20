package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.roundToInt

// Retro 3D borders for classic Windows styling
fun Modifier.xpBevel(isInset: Boolean = false): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    val lightColor = if (isInset) Color(0xFF808080) else Color(0xFFFFFFFF)
    val extraLightColor = if (isInset) Color(0xFF404040) else Color(0xFFECE9D8)
    val darkColor = if (isInset) Color(0xFFECE9D8) else Color(0xFF808080)
    val extraDarkColor = if (isInset) Color(0xFFFFFFFF) else Color(0xFF404040)

    // Inner bevel depth 2px
    // Top & Left (Light outer, extraLight inner)
    drawLine(color = lightColor, start = Offset(0f, 0f), end = Offset(w, 0f), strokeWidth = 2f)
    drawLine(color = lightColor, start = Offset(0f, 0f), end = Offset(0f, h), strokeWidth = 2f)

    if (!isInset) {
        drawLine(color = extraLightColor, start = Offset(2f, 2f), end = Offset(w - 2f, 2f), strokeWidth = 2f)
        drawLine(color = extraLightColor, start = Offset(2f, 2f), end = Offset(2f, h - 2f), strokeWidth = 2f)
    }

    // Bottom & Right (Dark inner, extraDark outer)
    drawLine(color = extraDarkColor, start = Offset(w, 0f), end = Offset(w, h), strokeWidth = 2f)
    drawLine(color = extraDarkColor, start = Offset(0f, h), end = Offset(w, h), strokeWidth = 2f)

    drawLine(color = darkColor, start = Offset(w - 2f, 2f), end = Offset(w - 2f, h - 2f), strokeWidth = 2f)
    drawLine(color = darkColor, start = Offset(2f, h - 2f), end = Offset(w - 2f, h - 2f), strokeWidth = 2f)
}

@Composable
fun XpSolidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .background(if (enabled) XpWindowBg else Color(0xFFD2CFBF))
            .xpBevel(isInset = false)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) XpTextDark else Color(0xFF808080),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

// Draggable and Resizable Windows XP Styled Window Frame
@Composable
fun XpWindowFrame(
    title: String,
    isFocused: Boolean,
    isMaximized: Boolean,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onFocus: () -> Unit,
    initialX: Float,
    initialY: Float,
    widthDp: Int,
    heightDp: Int,
    content: @Composable BoxScope.() -> Unit
) {
    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }

    val baseModifier = if (isMaximized) {
        Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp) // Leave taskbar space
    } else {
        Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(widthDp.dp)
            .height(heightDp.dp)
    }

    Box(
        modifier = baseModifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .border(3.dp, if (isFocused) XpLunaBlue else XpBorderShadow, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(XpWindowBg)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onFocus() },
                    onDrag = { change, dragAmount ->
                        if (!isMaximized) {
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                )
            }
            .clickable(onClick = onFocus)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title Bar
            val titleGradient = if (isFocused) {
                Brush.horizontalGradient(listOf(XpLunaBlueDark, XpLunaBlue, XpLunaBlueLight))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF7490C4), Color(0xFF94AFDF)))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(titleGradient)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Window Title
                Text(
                    text = title,
                    color = XpTextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Window Control Buttons (Minimize, Maximize, Close)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF3843C4), RoundedCornerShape(2.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                            .clickable { onMinimize() },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .size(10.dp, 2.dp)
                                .background(Color.White)
                        )
                    }

                    // Maximize
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF3843C4), RoundedCornerShape(2.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                            .clickable { onMaximize() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .border(1.5.dp, Color.White)
                        )
                    }

                    // Close (Shiny Red Button)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFF7E6B), Color(0xFFE04324), Color(0xFFB51E06))
                                ), RoundedCornerShape(2.dp)
                            )
                            .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Window Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                content()
            }
        }
    }
}
