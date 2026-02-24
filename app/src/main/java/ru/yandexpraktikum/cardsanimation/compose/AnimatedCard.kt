package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationStep
import kotlin.math.cos
import kotlin.math.sin

private const val SHORT_ANIMATION_DURATION = 300
private const val LONG_ANIMATION_DURATION = 800

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    finalRotation: Float,
    isAnimating: Boolean,
    animationStep: CardSwapAnimationStep,
    onAnimationStepComplete: ((CardSwapAnimationStep) -> Unit)? = null,
) {

    val density = LocalDensity.current

    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationStep == CardSwapAnimationStep.THIRD_STEP -> finalRotation
            isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(
            durationMillis = if (animationStep == CardSwapAnimationStep.THIRD_STEP) {
                SHORT_ANIMATION_DURATION
            } else {
                LONG_ANIMATION_DURATION
            }
        ),
        finishedListener = {
            if (animationStep == CardSwapAnimationStep.THIRD_STEP && isAnimating) {
                onAnimationStepComplete?.invoke(CardSwapAnimationStep.THIRD_STEP)
            }
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == CardSwapAnimationStep.FIRST_STEP -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }

            isAnimating && animationStep == CardSwapAnimationStep.SECOND_STEP -> 0f

            else -> 0f
        },
        animationSpec = tween(durationMillis = SHORT_ANIMATION_DURATION),
        finishedListener = {
            if (isAnimating) {
                when (animationStep) {
                    CardSwapAnimationStep.FIRST_STEP -> {
                        onAnimationStepComplete?.invoke(CardSwapAnimationStep.FIRST_STEP)
                    }

                    CardSwapAnimationStep.SECOND_STEP -> {
                        onAnimationStepComplete?.invoke(CardSwapAnimationStep.SECOND_STEP)
                    }

                    else -> Unit
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == CardSwapAnimationStep.FIRST_STEP -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }

            isAnimating && animationStep == CardSwapAnimationStep.SECOND_STEP -> 0f

            else -> 0f
        },
        animationSpec = tween(durationMillis = SHORT_ANIMATION_DURATION),
        label = "translationY"
    )

    val shouldBringToFront = isAnimating && animationStep == CardSwapAnimationStep.SECOND_STEP ||
            isAnimating && animationStep == CardSwapAnimationStep.THIRD_STEP

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier ->
                if (shouldBringToFront) modifier.zIndex(1000f) else modifier
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}