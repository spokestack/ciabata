package io.spokestack.ciabata

import android.util.Log
import io.spokestack.spokestack.OnSpeechEventListener
import io.spokestack.spokestack.SpeechContext
import io.spokestack.spokestack.tts.TTSEvent
import io.spokestack.spokestack.tts.TTSListener

/**
 * The singleton dialogue manager that handles speech events (both input and output)
 * and dispatches any relevant results to the UI.
 */
object DialogueManager : OnSpeechEventListener, TTSListener {
    private val TAG = javaClass.simpleName
    var voiceDelegate: VoiceDelegate? = null

    override fun onEvent(event: SpeechContext.Event?, context: SpeechContext?) {
        when (event) {
            SpeechContext.Event.RECOGNIZE -> context?.let { processIntent(it.transcript) }
            SpeechContext.Event.ERROR -> context?.let {
                if (BuildConfig.DEBUG) Log.w(TAG, "ERROR: ${it.error}")
            }
            SpeechContext.Event.TRACE -> context?.let {
                if (BuildConfig.DEBUG) Log.i(TAG, it.message)
            }
            SpeechContext.Event.ACTIVATE -> if (BuildConfig.DEBUG) Log.i(TAG, "ACTIVATED")
            SpeechContext.Event.DEACTIVATE -> if (BuildConfig.DEBUG) Log.i(TAG, "DEACTIVATED")
            SpeechContext.Event.TIMEOUT -> if (BuildConfig.DEBUG) Log.i(TAG, "ASR TIMEOUT")
            else -> {
                // do nothing
            }
        }
    }

    private fun processIntent(utterance: String) {
        when (IntentClassifier.classify(utterance)) {
            Intent.START -> TabataTimer.start()
            Intent.STOP -> TabataTimer.stop()
            Intent.RESET -> TabataTimer.reset()
            Intent.UNKNOWN -> {
                if (BuildConfig.DEBUG) Log.i(TAG, "Unknown utterance: $utterance")
                voiceDelegate?.shouldSpeak("Sorry; I don't know how to do that.")
            }
        }
    }

    // TTSListener

    override fun eventReceived(event: TTSEvent?) {
        // We're only interested in TTS errors; Spokestack's output component handles playback
        // when an audio URL is returned in one of these events
        if (BuildConfig.DEBUG && event?.type == TTSEvent.Type.ERROR) {
            Log.w(TAG, "TTS error: ${event.error.message}")
        }
    }

}