package io.spokestack.ciabata

import android.os.CountDownTimer

class TabataTimer {

    private var timer: TabataCountdown? = null
    private val settings = TimerSettings()
    var curMode: TimerMode = TimerMode.PREP
    private var curCycle = 1
    private var timeLeft = 0
    private var isRunning = false
    var uiDelegate: TimerUI? = null


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
            timer = TabataCountdown(timeLeft * 1000L, 1000, this)
            timer?.start()
            isRunning = true
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
        // the countdown starts at "3", but we're going to allow some time for
        // the audio to start playing
        if (timeLeft == 4) {
            countdown()
        }
    }

    private fun countdown() {
        uiDelegate?.shouldSpeak(
            """<speak>three <break time="500ms"/> two <break time="500ms"/> one</speak>""",
            TTSInputType.SSML
        )
    }

    private fun changeModes() {
        when (curMode) {
            TimerMode.PREP -> {
                curMode = TimerMode.WORK
                timeLeft = settings.work
            }
            TimerMode.WORK -> {
                curCycle += 1
                curMode = TimerMode.REST
                timeLeft = settings.rest
            }
            TimerMode.REST -> {
                curMode = TimerMode.WORK
                timeLeft = settings.work
            }
        }
        uiDelegate?.shouldSpeak(curMode.text())
    }

    private fun timerDone() {
        uiDelegate?.shouldSpeak("Done!")
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