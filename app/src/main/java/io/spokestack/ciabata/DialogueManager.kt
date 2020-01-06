package io.spokestack.ciabata

import io.spokestack.spokestack.OnSpeechEventListener
import io.spokestack.spokestack.SpeechContext

/**
 * The singleton dialogue manager that handles speech events and dispatches results to the UI.
 */
object DialogueManager : OnSpeechEventListener {
    var uiDelegate: TimerUI? = null

    override fun onEvent(event: SpeechContext.Event?, context: SpeechContext?) {
        when (event) {
            SpeechContext.Event.RECOGNIZE -> context?.let { onIntent(it) }
            SpeechContext.Event.ERROR -> context?.let { println("ERROR: ${it.error}") }
            SpeechContext.Event.TRACE -> context?.let { println(it.message) }
            SpeechContext.Event.ACTIVATE -> println("ACTIVATED")
            SpeechContext.Event.DEACTIVATE -> println("DEACTIVATED")
            SpeechContext.Event.TIMEOUT -> println("ASR TIMEOUT")
            else -> {
                // do nothing
            }
        }
    }

    private fun onIntent(result: SpeechContext) {
        when (IntentClassifier.classify(result.transcript)) {
            Intent.START -> {
                println("STARTING TIMER")
                uiDelegate?.shouldStartTimer()
            }
            Intent.STOP -> {
                println("STOPPING TIMER")
                uiDelegate?.shouldStopTimer()
            }
            Intent.RESET ->
                uiDelegate?.shouldResetTimer()
            Intent.UNKNOWN -> {
                println("Unknown utterance: ${result.transcript}")
                uiDelegate?.shouldSpeak("Sorry; I don't know how to do that.")
            }
        }
    }

}