package ru.yandexpraktikum.cardsanimation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: CardSwapAnimationStep = CardSwapAnimationStep.EMPTY,
)

enum class CardSwapAnimationStep {
    FIRST_STEP,
    SECOND_STEP,
    THIRD_STEP,
    EMPTY
}
