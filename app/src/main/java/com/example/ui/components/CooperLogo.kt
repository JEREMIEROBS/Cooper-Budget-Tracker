package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LedgerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CooperMood {
    HAPPY,      // Under budget, cheerful wag
    ALERT,      // Nearing budget limit (80%+)
    WATCHFUL,   // Over budget, loyal comfort
    CHILL       // Well balanced
}

/**
 * Unique Cooper Dog Logo Icon.
 * Features distinct Light Mode and Dark Mode styling, gentle idle breathing,
 * and joyful tap-to-pet feedback.
 */
@Composable
fun CooperDogIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    mood: CooperMood = CooperMood.HAPPY,
    isDarkMode: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "cooper_dog_icon"
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val bounceScale = remember { Animatable(1f) }
    val rotateAngle = remember { Animatable(0f) }

    // Subtle breathing animation for a lively companion mascot feel
    val infiniteTransition = rememberInfiniteTransition(label = "cooper_idle_motion")
    val idleFloat by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cooper_breathe_float"
    )

    // Palette tokens tailored for Light Mode vs Dark Mode
    val containerBg = if (isDarkMode) Color(0xFF141F1A) else Color(0xFFF7F5F0)
    val outerRingColor = if (isDarkMode) Color(0xFF5FB596) else Color(0xFF1F5E4B)
    val borderColor = if (isDarkMode) Color(0xFF283A31) else Color(0xFFE7E3DA)
    val coinGold = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFF59E0B)

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .clip(CircleShape)
            .background(containerBg)
            .border(1.5.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                coroutineScope.launch {
                    bounceScale.animateTo(1.15f, tween(90, easing = FastOutSlowInEasing))
                    rotateAngle.animateTo(8f, tween(70, easing = FastOutSlowInEasing))
                    rotateAngle.animateTo(-8f, tween(70, easing = FastOutSlowInEasing))
                    rotateAngle.animateTo(0f, tween(80, easing = FastOutSlowInEasing))
                    bounceScale.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
                }
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative inner accent circle matching theme
        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .clip(CircleShape)
                .background(
                    if (isDarkMode) {
                        Brush.radialGradient(
                            listOf(Color(0xFF1F382E), Color(0xFF121B17))
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFFE3EEE9), Color(0xFFF2ECE1))
                        )
                    }
                )
        )

        // Authentic Cooper Husky Head Image with animated physics
        Image(
            painter = painterResource(id = R.drawable.cooper_head),
            contentDescription = "Cooper Dog Logo Icon",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.84f)
                .graphicsLayer {
                    translationY = idleFloat
                    scaleX = bounceScale.value
                    scaleY = bounceScale.value
                    rotationZ = rotateAngle.value
                }
        )

        // Small Gold Paw Collar Coin Badge in bottom-right corner
        Box(
            modifier = Modifier
                .size(size * 0.32f)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(coinGold)
                .border(1.dp, if (isDarkMode) Color(0xFF141F1A) else Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = if (isDarkMode) Color(0xFF181512) else Color(0xFF78350F),
                modifier = Modifier.size(size * 0.20f)
            )
        }
    }
}

/**
 * Interactive Brand Header for Dashboard.
 * Includes Cooper Dog Icon, Brand Typography, Theme toggle, and Logo Showcase trigger.
 */
@Composable
fun CooperBrandHeader(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onOpenLogoShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    val view = LocalView.current
    var petCount by remember { mutableIntStateOf(0) }
    var showPetToast by remember { mutableStateOf(false) }

    LaunchedEffect(showPetToast) {
        if (showPetToast) {
            delay(1600)
            showPetToast = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Cooper Brand Identity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        petCount++
                        showPetToast = true
                    }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("cooper_brand_header_row")
            ) {
                CooperDogIcon(
                    size = 46.dp,
                    isDarkMode = isDarkMode,
                    onClick = {
                        petCount++
                        showPetToast = true
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cooper",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Mode indicator badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accentSoft)
                                .clickable(onClick = onToggleTheme)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isDarkMode) "Dark" else "Light",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                    }

                    Text(
                        text = if (petCount > 0) "Happy Dog • Petted $petCount×" else "Loyal Budget Companion",
                        fontSize = 11.sp,
                        fontWeight = if (petCount > 0) FontWeight.Medium else FontWeight.Normal,
                        color = if (petCount > 0) colors.accent else colors.textMuted
                    )
                }
            }

            // Right: Theme Mode Switcher + Showcase Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo & Showcase Action Pill
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenLogoShowcase)
                        .padding(horizontal = 10.dp)
                        .testTag("cooper_showcase_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Cooper Mascot Details",
                            tint = colors.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Mascot",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text
                        )
                    }
                }

                // Light / Dark Mode Toggle Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable(onClick = onToggleTheme)
                        .testTag("theme_mode_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else colors.text,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Fun reactive speech toast from Cooper when petted
        AnimatedVisibility(
            visible = showPetToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, start = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.accentSoft)
                    .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = listOf(
                        "Woof! Cooper is guarding your savings! 🐶",
                        "Tail wag! Good spending habits! ✨",
                        "Arf! Log your receipt in 5 seconds! ⏱️",
                        "Cooper loves smart budgeters! 💚"
                    )[petCount % 4],
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accent
                )
            }
        }
    }
}

/**
 * Dedicated Interactive Dialog showcasing Cooper Dog Logo in Light and Dark mode versions,
 * complete with interactive poses and animations from the Mascot Guide.
 */
@Composable
fun CooperLogoShowcaseDialog(
    isOpen: Boolean,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit
) {
    if (!isOpen) return

    val colors = LedgerTheme.colors
    val view = LocalView.current
    var selectedPose by remember { mutableStateOf(CooperPose.SIT) }
    var petCount by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cooper Dog Mascot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Mascot Stage
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Cooper(
                            pose = selectedPose,
                            size = 140.dp,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                petCount++
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = when (selectedPose) {
                                CooperPose.SIT -> "Cooper Sitting (Splash, Onboarding, Budgets)"
                                CooperPose.HEAD -> "Cooper Head Logo Icon (App Icon & Header)"
                                CooperPose.RUN -> "Cooper Running (Savings Goal & Under Budget)"
                                CooperPose.PLAY -> "Cooper Playing (Reminders & Welcome Back)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Tap Cooper above to pet (${petCount} pets)!",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                // Interactive Pose Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        CooperPose.SIT to "Sit",
                        CooperPose.HEAD to "Icon",
                        CooperPose.RUN to "Run",
                        CooperPose.PLAY to "Play"
                    ).forEach { (pose, label) ->
                        val isSelected = selectedPose == pose
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accent else colors.surface)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                                .clickable { selectedPose = pose }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else colors.text
                            )
                        }
                    }
                }

                // Logo Theme Versions Comparison (Light vs Dark)
                Text(
                    text = "LOGO ICON VERSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = colors.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Light Mode Logo Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (isDarkMode) onToggleTheme() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isDarkMode) 2.dp else 1.dp,
                            color = if (!isDarkMode) Color(0xFF1F5E4B) else Color(0xFFE7E3DA)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CooperDogIcon(
                                size = 42.dp,
                                isDarkMode = false
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Light Mode",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F5E4B)
                            )
                            Text(
                                text = "Ivory & Forest",
                                fontSize = 10.sp,
                                color = Color(0xFF5A665F)
                            )
                        }
                    }

                    // Dark Mode Logo Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isDarkMode) onToggleTheme() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F1A)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isDarkMode) 2.dp else 1.dp,
                            color = if (isDarkMode) Color(0xFF5FB596) else Color(0xFF283A31)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CooperDogIcon(
                                size = 42.dp,
                                isDarkMode = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Dark Mode",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5FB596)
                            )
                            Text(
                                text = "Midnight & Mint",
                                fontSize = 10.sp,
                                color = Color(0xFF8F9E96)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Got It", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onToggleTheme) {
                Text(
                    text = if (isDarkMode) "Switch to Light" else "Switch to Dark",
                    color = colors.accent
                )
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
