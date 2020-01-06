package io.spokestack.ciabata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.spokestack.ciabata.databinding.ActivityMainBinding
import io.spokestack.spokestack.SpeechPipeline
import io.spokestack.spokestack.tts.SSML
import io.spokestack.spokestack.tts.SpokestackTTSClient
import android.content.Context
import io.spokestack.spokestack.SpeechContext
import java.io.File
import java.io.FileOutputStream


private const val PREF_NAME = "CiabataPrefs"
private const val versionKey = "versionCode"
private const val nonexistent = -1

class MainActivity : AppCompatActivity(), TimerUI {

    private lateinit var binding: ActivityMainBinding
    private var pipeline: SpeechPipeline? = null
    private var tts: SpokestackTTSClient? = null
    private val timer = TabataTimer()
    private val audioPemission = 1337


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // other setup
        tts = SpokestackTTSClient(VoiceOutput)
        tts!!.setApiKey("f854fbf30a5f40c189ecb1b38bc78059")
        timer.uiDelegate = this
        DialogueManager.uiDelegate = this
        resetLabels()
        if (checkMicPermission()) {
            buildPipeline()
        }
    }

    private fun checkMicPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            audioPemission
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            audioPemission -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    buildPipeline()
                }
            }
            else -> {
                // do nothing
            }

        }
    }

    private fun buildPipeline() {
        checkForModels()
        pipeline = SpeechPipeline.Builder()
            .setInputClass("io.spokestack.spokestack.android.MicrophoneInput")
            .addStageClass("io.spokestack.spokestack.webrtc.AcousticNoiseSuppressor")
            .addStageClass("io.spokestack.spokestack.webrtc.AutomaticGainControl")
            .addStageClass("io.spokestack.spokestack.webrtc.VoiceActivityDetector")
            .addStageClass("io.spokestack.spokestack.wakeword.WakewordTrigger")
            .addStageClass("io.spokestack.spokestack.google.GoogleSpeechRecognizer")
            .setProperty("agc-compression-gain-db", 15)
            .setProperty("wake-active-min", 2000)
            .setProperty("pre-emphasis", 0.97)
            .setProperty("wake-detect-path", "$cacheDir/detect.lite")
            .setProperty("wake-encode-path", "$cacheDir/encode.lite")
            .setProperty("wake-filter-path", "$cacheDir/filter.lite")
            .setProperty("google-credentials", BuildConfig.GOOGLE_CREDENTIALS)
            .setProperty("locale", "en-US")
//            .setProperty("trace-level", 20)
            .addOnSpeechEventListener(DialogueManager)
            .build()

        pipeline?.start()
    }

    private fun checkForModels() {
        // this function ensures that wakeword models are only decompressed if the app has been
        // upgraded or if the user has cleared its cache

        if (!modelsCached()) {
            decompressModels()
        } else {
            val currentVersionCode = BuildConfig.VERSION_CODE
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedVersionCode = prefs.getInt(versionKey, nonexistent)

            if (currentVersionCode != savedVersionCode) {
                decompressModels()

                // Update the shared preferences with the current version code
                prefs.edit().putInt(versionKey, currentVersionCode).apply()
            }
        }
    }

    private fun modelsCached(): Boolean {
        val filterName = "filter.lite"
        val filterFile = File("$cacheDir/$filterName")
        return filterFile.exists()
    }

    private fun decompressModels() {
        listOf("detect.lite", "encode.lite", "filter.lite").forEach(::cacheAsset)
    }

    private fun cacheAsset(modelName: String) {
        val filterFile = File("$cacheDir/$modelName")
        val inputStream = assets.open(modelName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val fos = FileOutputStream(filterFile)
        fos.write(buffer)
        fos.close()
    }

    @Suppress("UNUSED_PARAMETER")
    fun startTapped(view: View) {
        timer.start()
    }

    @Suppress("UNUSED_PARAMETER")
    fun stopTapped(view: View) {
        timer.stop()
    }

    @Suppress("UNUSED_PARAMETER")
    fun resetTapped(view: View) {
        timer.reset()
        resetLabels()
    }

    private fun resetLabels() {
        binding.modeLabel.text = timer.defaultMode()
        binding.timerLabel.text = timer.defaultSeconds().toString()
    }

    @Suppress("UNUSED_PARAMETER")
    fun micTapped(view: View) {
        if (pipeline != null && !pipeline!!.isRunning) {
            pipeline?.context?.isActive = true
        }
    }

    override fun timeChanged(mode: String, seconds: Int) {
        runOnUiThread {
            binding.modeLabel.text = mode
            binding.timerLabel.text = seconds.toString()
        }
    }

    override fun shouldStartTimer() {
        runOnUiThread {
            timer.start()
        }
    }

    override fun shouldStopTimer() {
        runOnUiThread {
            timer.stop()
        }
    }

    override fun shouldResetTimer() {
        runOnUiThread {
            timer.reset()
        }
    }

    override fun shouldEnableUI() {
        toggleButtons(true)
    }

    override fun shouldDisableUI() {
        toggleButtons(false)
    }

    override fun shouldSpeak(text: String, mode: TTSInputType) {
        when (mode) {
            TTSInputType.TEXT -> tts?.synthesize(text)
            TTSInputType.SSML -> tts?.synthesize(SSML(text))
        }
    }

    private fun toggleButtons(enabled: Boolean) {
        binding.startButton.isEnabled = enabled
        binding.stopButton.isEnabled = enabled
        binding.resetButton.isEnabled = enabled
        binding.micButton.isEnabled = enabled
    }

}
