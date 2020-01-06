package io.spokestack.ciabata

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import io.spokestack.spokestack.tts.TTSCallback

/**
 * A simple manager for playing TTS output.
 */
object VoiceOutput : TTSCallback(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var mediaPlayer: MediaPlayer? = null
    private val attributes: AudioAttributes

    init {
        val usage =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes.USAGE_ASSISTANT
            } else {
                AudioAttributes.USAGE_MEDIA
            }

        attributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }

    override fun onError(message: String?) {
        System.err.println(message)
    }

    override fun onUrlReceived(url: String) {
        val player = configMediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
        }
        if (mediaPlayer == null) {
            mediaPlayer = player
        } else {
            mediaPlayer?.setNextMediaPlayer(player)
        }
    }

    private fun configMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setOnPreparedListener(VoiceOutput)
            setOnCompletionListener(VoiceOutput)
            setAudioAttributes(attributes)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (mp != null && mp == mediaPlayer) {
            mp.start()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.release()
        mediaPlayer = null
    }
}