package io.spokestack.ciabata

import io.spokestack.spokestack.tts.SynthesisRequest

/**
 * An interface to be adopted by activities that hold references to UI elements used to display
 * timer state to the user.
 */
interface TimerUI {
    // The timer state has changed.
    fun timeChanged(mode: String, seconds: Int)

    // The timer should start counting.
    fun shouldStartTimer()

    // The timer should stop counting.
    fun shouldStopTimer()

    // The timer should reset its internal state.
    fun shouldResetTimer()

    // The speech pipeline is inactive, so the UI should be enabled.
    fun shouldEnableUI()

    // The speech pipeline is active, so the UI should be disabled.
    fun shouldDisableUI()
}