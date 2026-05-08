package com.jarvis.launcher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.theme.LocalHudColors

/**
 * Renders text with a glowing bloom effect behind it.
 *
 * A blurred, slightly larger copy of the text is layered behind the main text
 * to create a neon / holographic glow effect consistent with the AEGIS HUD.
 *
 * @param text The text to display.
 * @param modifier Modifier applied to the outer container.
 * @param color Primary text color.
 * @param glowColor Color of the glow layer (typically the same hue at lower alpha).
 * @param fontSize Font size for the text.
 * @param fontWeight Font weight for the text.
 * @param fontFamily Font family for the text.
 * @param letterSpacing Letter spacing.
 * @param textAlign Text alignment.
 * @param glowAlpha Alpha of the glow layer.
 * @param glowScale Scale factor for the glow layer (>1 makes glow extend beyond text).
 * @param style Optional TextStyle override. When provided, fontSize/fontWeight/fontFamily
 *              parameters are ignored in favor of the style's values.
 */
@Composable
fun GlowText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalHudColors.current.accent,
    glowColor: Color = LocalHudColors.current.accent,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Monospace,
    letterSpacing: TextUnit = 1.sp,
    textAlign: TextAlign = TextAlign.Center,
    glowAlpha: Float = 0.4f,
    glowScale: Float = 1.05f,
    style: TextStyle? = null
) {
    val effectiveStyle = style ?: TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        letterSpacing = letterSpacing
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow layer - blurred, slightly larger copy behind the text
        Text(
            text = text,
            style = effectiveStyle,
            color = glowColor,
            textAlign = textAlign,
            modifier = Modifier.graphicsLayer {
                alpha = glowAlpha
                scaleX = glowScale
                scaleY = glowScale
            }
        )

        // Second glow layer - wider spread, lower alpha for bloom
        Text(
            text = text,
            style = effectiveStyle,
            color = glowColor,
            textAlign = textAlign,
            modifier = Modifier.graphicsLayer {
                alpha = glowAlpha * 0.5f
                scaleX = glowScale * 1.08f
                scaleY = glowScale * 1.08f
            }
        )

        // Main crisp text on top
        Text(
            text = text,
            style = effectiveStyle,
            color = color,
            textAlign = textAlign
        )
    }
}
