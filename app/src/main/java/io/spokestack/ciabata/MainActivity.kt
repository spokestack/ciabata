package io.spokestack.ciabata

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.spokestack.ciabata.databinding.ActivityMainBinding
import io.spokestack.spokestack.SpeechPipeline
import io.spokestack.spokestack.tts.SynthesisRequest
import io.spokestack.spokestack.tts.TTSManager
import java.io.File
import java.io.FileOutputStream


private const val PREF_NAME = "CiabataPrefs"
private const val versionKey = "versionCode"
private const val nonexistent = -1

/**
 * The activity housing the TabataTimer's UI elements and the Spokestack speech pipeline and TTS
 * subsystem.
 */
class MainActivity : AppCompatActivity(), TimerUI, VoiceDelegate {
    private val logTag = javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private var pipeline: SpeechPipeline? = null
    private var tts: TTSManager? = null

    // a sentinel value we'll use to verify we have the proper permissions to use Spokestack to
    // record audio
    private val audioPermission = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TabataTimer UI setup
        TabataTimer.uiDelegate = this
        TabataTimer.voiceDelegate = this
        // the dialogue manager needs to be able to control the TabataTimer so we can respond to
        // voice commands
        DialogueManager.voiceDelegate = this

        // Spokestack setup
        if (this.pipeline == null && checkMicPermission()) {
            buildPipeline()
        }
        buildTTS()
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
            audioPermission
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            audioPermission -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    buildPipeline()
                } else {
                    Log.w(logTag, "Record permission not granted; voice control disabled!")
                }
                return
            }
            else -> {
                // do nothing
            }

        }
    }

    private fun buildPipeline() {
        checkForModels()
        val pipelineBuilder = SpeechPipeline.Builder()
            .setInputClass("io.spokestack.spokestack.android.MicrophoneInput")
            .addStageClass("io.spokestack.spokestack.webrtc.AcousticNoiseSuppressor")
            .addStageClass("io.spokestack.spokestack.webrtc.AutomaticGainControl")
            .addStageClass("io.spokestack.spokestack.webrtc.VoiceActivityDetector")
            .addStageClass("io.spokestack.spokestack.wakeword.WakewordTrigger")
            .setProperty("agc-compression-gain-db", 15)
            .setProperty("wake-active-min", 2000)
            .setProperty("pre-emphasis", 0.97)
            .setProperty("wake-detect-path", "$cacheDir/detect.lite")
            .setProperty("wake-encode-path", "$cacheDir/encode.lite")
            .setProperty("wake-filter-path", "$cacheDir/filter.lite")
            .setAndroidContext(applicationContext)
            .addOnSpeechEventListener(DialogueManager)

        // The else clause demonstrates falling back to Google Cloud ASR if built-in speech recognition is not available on the current
        // device.
        // Note that this requires Google credentials as described in the README, so it's commented
        // out because the code that loads the credentials in the app's Gradle file is disabled
        // by default.
        if (SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
            pipelineBuilder.addStageClass(
                "io.spokestack.spokestack.android.AndroidSpeechRecognizer"
            )
//        } else {
//            pipelineBuilder.addStageClass("io.spokestack.spokestack.google.GoogleSpeechRecognizer")
//            pipelineBuilder.setProperty("google-credentials", BuildConfig.GOOGLE_CREDENTIALS)
//                .setProperty("locale", "en-US")
        }

        pipeline = pipelineBuilder.build()
        pipeline?.start()
    }

    private fun checkForModels() {
        // this function ensures that wakeword models are only decompressed if the app has been
        // upgraded or if the user has cleared its cache
        // we could also reduce app download size by downloading the models on first launch or by
        // not using a wakeword at all, but that would sort of negate the main benefit of a
        // voice-controlled timer
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

    private fun buildTTS() {
        if (this.tts == null) {
            this.tts = TTSManager.Builder()
                .setTTSServiceClass("io.spokestack.spokestack.tts.SpokestackTTSService")
                .setOutputClass("io.spokestack.spokestack.tts.SpokestackTTSOutput")
                // demo Spokestack credentials, not guaranteed to work indefinitely
                // see https://spokestack.io for instructions on getting your own
                .setProperty("spokestack-id", "f0bc990c-e9db-4a0c-a2b1-6a6395a3d97e")
                .setProperty(
                    "spokestack-secret",
                    "5BD5483F573D691A15CFA493C1782F451D4BD666E39A9E7B2EBE287E6A72C6B6"
                )
                .setAndroidContext(applicationContext)
                .setLifecycle(lifecycle)
                // we'll use Spokestack's automatic media player for voice,
                // but set the DialogueManager as a listener simply so we can log errors
                .addTTSListener(DialogueManager)
                .build()
        } else {
            this.tts!!.apply {
                prepare()
                registerLifecycle(lifecycle)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pipeline?.stop()
    }

    @Suppress("UNUSED_PARAMETER")
    fun startTapped(view: View) {
        TabataTimer.start()
    }

    @Suppress("UNUSED_PARAMETER")
    fun stopTapped(view: View) {
        TabataTimer.stop()
    }

    @Suppress("UNUSED_PARAMETER")
    fun resetTapped(view: View) {
        TabataTimer.reset()
        resetLabels()
    }

    private fun resetLabels() {
        binding.modeLabel.text = TabataTimer.defaultMode()
        binding.timerLabel.text = TabataTimer.defaultSeconds().toString()
    }

    @Suppress("UNUSED_PARAMETER")
    fun micTapped(view: View) {
        if (pipeline != null) {
            pipeline!!.context?.isActive = true
        }
    }

    override fun timeChanged(mode: String, seconds: Int) {
        runOnUiThread {
            binding.modeLabel.text = mode
            binding.timerLabel.text = seconds.toString()
        }
    }

    override fun shouldStartTimer() {
        TabataTimer.start()
    }

    override fun shouldStopTimer() {
        runOnUiThread {
            TabataTimer.stop()
        }
    }

    override fun shouldResetTimer() {
        runOnUiThread {
            TabataTimer.reset()
        }
    }

    override fun shouldEnableUI() {
        toggleButtons(true)
    }

    override fun shouldDisableUI() {
        toggleButtons(false)
    }

    override fun shouldSpeak(text: String, mode: SynthesisRequest.Mode) {
        // since the voice prompts in this app don't change, it would
        // be better to synthesize them all once and cache the audio, but
        // we'll do it this way instead to demonstrate Spokestack's automatic
        // handling of the media player
        val synthRequest = SynthesisRequest.Builder(text).withMode(mode).build()
        tts?.synthesize(synthRequest)
    }

    private fun toggleButtons(enabled: Boolean) {
        binding.startButton.isEnabled = enabled
        binding.stopButton.isEnabled = enabled
        binding.resetButton.isEnabled = enabled
        binding.micButton.isEnabled = enabled
    }

}
