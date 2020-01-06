package io.spokestack.ciabata

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

    // The UI should deliver an audio message to the user
    fun shouldSpeak(text: String, mode: TTSInputType = TTSInputType.TEXT)
}