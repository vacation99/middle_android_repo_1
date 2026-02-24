package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationStep
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
private fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var currentCards by remember { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var dragAmountOffset by remember { mutableStateOf(Offset.Zero) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    if (!animationState.isAnimating) {
                        val threshold = 1f
                        val isVerticalDominant = abs(dragAmountOffset.y) > abs(dragAmountOffset.x)
                        val isHorizontalDominant = abs(dragAmountOffset.x) > abs(dragAmountOffset.y)

                        when {
                            isVerticalDominant && abs(dragAmountOffset.y) > threshold -> {
                                isRotated = dragAmountOffset.y < 0
                            }

                            isHorizontalDominant && abs(dragAmountOffset.x) > threshold -> {
                                if (isRotated) return@detectDragGestures
                                animationState = CardSwapAnimationState(
                                    isAnimating = true,
                                    animationStep = CardSwapAnimationStep.FIRST_STEP
                                )
                            }
                        }
                    }
                    dragAmountOffset = Offset.Zero
                }
            ) { _, dragAmount ->
                dragAmountOffset = dragAmount
            }
        },
        contentAlignment = Alignment.Center
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(
                    cardIndex = i,
                    cardCount = currentCards.size,
                    isRotated = isRotated
                )
                val finalRotation = calculateCardRotation(
                    cardIndex = if (i == 0) currentCards.size - 1 else i - 1,
                    cardCount = currentCards.size,
                    isRotated = isRotated
                )

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    finalRotation = finalRotation,
                    cardData = cardData,
                    isAnimating = if (i == 0) animationState.isAnimating else false,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = if (i == 0) {
                        { currentStep ->
                            handleAnimationStepComplete(
                                step = currentStep,
                                cardIndex = i,
                                onStepChange = { newStep ->
                                    animationState = animationState.copy(animationStep = newStep)
                                },
                                onAnimationComplete = {
                                    animationState = CardSwapAnimationState()
                                    currentCards = reorderCards(currentCards)
                                }
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

// Простая функция перестановки карт
private fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

private fun handleAnimationStepComplete(
    step: CardSwapAnimationStep,
    cardIndex: Int,
    onStepChange: (CardSwapAnimationStep) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            CardSwapAnimationStep.FIRST_STEP -> onStepChange(CardSwapAnimationStep.SECOND_STEP)
            CardSwapAnimationStep.SECOND_STEP -> onStepChange(CardSwapAnimationStep.THIRD_STEP)
            CardSwapAnimationStep.THIRD_STEP -> onAnimationComplete()
            CardSwapAnimationStep.EMPTY -> Unit
        }
    }
}