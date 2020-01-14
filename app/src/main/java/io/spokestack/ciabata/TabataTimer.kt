package io.spokestack.ciabata

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import io.spokestack.spokestack.tts.SynthesisRequest

/**
 * The timer logic. Dispatches state changes and associated voice prompts to a delegate capable of
 * updating the UI.
 */
object TabataTimer {

    private var timer: TabataCountdown? = null
    private val settings = TimerSettings()
    var curMode: TimerMode = TimerMode.PREP
    private var curCycle = 1
    private var timeLeft = 0
    private var isRunning = false
    var uiDelegate: TimerUI? = null
    var voiceDelegate: VoiceDelegate? = null

    init {
        updateSettings()
    }

    fun defaultSeconds(): Int {
        return settings.prep
    }

    fun defaultMode(): String {
        return TimerMode.PREP.text()
    }

    private fun updateSettings() {
        curCycle = 1
        curMode = TimerMode.PREP
        timeLeft = defaultSeconds()
    }

    fun start() {
        if (!this.isRunning) {
            // if the timer was started by voice, but the ASR does
            // not dispatch events on the main thread, we have to move
            // back to it here, even though the UI updating code in
            // MainActivity is forced onto the UI thread
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                timer = TabataCountdown(timeLeft * 1000L, 1000, this)
                timer?.start()
                isRunning = true
            }
        }
    }

    fun stop() {
        timer?.cancel()
        isRunning = false
    }

    fun reset() {
        timer?.cancel()
        isRunning = false
        updateSettings()
    }


    fun updateTimer() {
        timeLeft -= 1
        uiDelegate?.timeChanged(curMode.text(), timeLeft)
        handleSpecialTimes()
    }

    fun finishTimer() {
        // we have to change modes to reset the time itself
        changeModes()

        // changing modes means we need to update the UI again
        uiDelegate?.timeChanged(curMode.text(), timeLeft)

        // one last check in case we're totally done
        if (curCycle == settings.cycles) {
            timerDone()
            reset()
        } else {
            timer = TabataCountdown(timeLeft * 1000L, 1000, this)
            timer?.start()
        }
    }

    private fun handleSpecialTimes() {
        when (timeLeft) {
            // the countdown starts at "3", but we're going to allow some time for
            // the synthesis request to deliver actual audio
            4 -> countdown()
            // same for announcing the next timer mode
            1 -> voiceDelegate?.shouldSpeak(nextMode().text())
        }
    }

    private fun countdown() {
        // if the audio were retrieved before use and cached as mentioned in MainActivity, this
        // could be broken up into separate clips to get the timing more exact, but this example
        // demonstrates one possible use of SSML input
        voiceDelegate?.shouldSpeak(
            """<speak>three <break time="500ms"/> two <break time="500ms"/> one 
                |<break time="500ms"/></speak>""".trimMargin(),
            SynthesisRequest.Mode.SSML
        )
    }

    private fun changeModes() {
        when (curMode) {
            TimerMode.PREP -> {
                timeLeft = settings.work
            }
            TimerMode.WORK -> {
                curCycle += 1
                timeLeft = settings.rest
            }
            TimerMode.REST -> {
                timeLeft = settings.work
            }
        }
        curMode = nextMode()
    }

    private fun nextMode(): TimerMode {
        return when (curMode) {
            TimerMode.PREP -> TimerMode.WORK
            TimerMode.WORK -> TimerMode.REST
            TimerMode.REST -> TimerMode.WORK
        }
    }

    private fun timerDone() {
        voiceDelegate?.shouldSpeak("Done!")
    }
}

private class TabataCountdown(
    millisInFuture: Long,
    countDownInterval: Long,
    private val delegate: TabataTimer
) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        delegate.updateTimer()
    }

    override fun onFinish() {
        delegate.finishTimer()
    }

}