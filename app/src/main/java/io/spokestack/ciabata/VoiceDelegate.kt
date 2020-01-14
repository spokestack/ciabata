package io.spokestack.ciabata

import io.spokestack.spokestack.tts.SynthesisRequest

/**
 * A simple interface adopted by classes that have access to the TTS subsystem.
 */
interface VoiceDelegate {

    // An audio message should be delivered to the user
    fun shouldSpeak(text: String, mode: SynthesisRequest.Mode = SynthesisRequest.Mode.TEXT)
}