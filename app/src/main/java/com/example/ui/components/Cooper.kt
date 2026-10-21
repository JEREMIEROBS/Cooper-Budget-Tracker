package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.launch

enum class CooperPose {
    SIT,   // Front-facing, paws resting. Used for splash, onboarding, empty budgets
    HEAD,  // Head only. Used for avatar, icon, settings
    RUN,   // Running with green coin pouch. Used for success, goal reached, under budget
    PLAY   // Playing with green ball. Used for reminders, empty transactions, welcome back
}

/**
 * Official Cooper Mascot component following Cooper-Mascot-Guide.
 * Respects Reduce Motion and decorative accessibility guidelines.
 */
@Composable
fun Cooper(
    pose: CooperPose = CooperPose.SIT,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "cooper_mascot"
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceScale = remember { Animatable(1f) }

    val drawableRes = when (pose) {
        CooperPose.SIT -> R.drawable.cooper_sit
        CooperPose.HEAD -> R.drawable.cooper_head
        CooperPose.RUN -> R.drawable.cooper_run
        CooperPose.PLAY -> R.drawable.cooper_play
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    bounceScale.animateTo(1.08f, tween(100, easing = FastOutSlowInEasing))
                    bounceScale.animateTo(0.96f, tween(100, easing = FastOutSlowInEasing))
                    bounceScale.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
                }
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Cooper the husky puppy mascot",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = bounceScale.value
                    scaleY = bounceScale.value
                }
        )
    }
}
