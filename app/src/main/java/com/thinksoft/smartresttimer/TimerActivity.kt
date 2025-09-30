package com.thinksoft.smartresttimer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.thinksoft.smartresttimer.databinding.ActivityTimerBinding
import java.util.Locale

class TimerActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var b: ActivityTimerBinding
    private var totalMs: Long = 60_000
    private var remainingMs: Long = 60_000
    private var timer: CountDownTimer? = null
    private var tts: TextToSpeech? = null

    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private val prefs by lazy { getSharedPreferences("smartrest", MODE_PRIVATE) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(b.root)

        val secs = intent.getIntExtra("seconds", 60)
        totalMs = secs * 1000L
        remainingMs = totalMs
        renderTime(remainingMs)

        b.btnPause.setOnClickListener { pauseTimer() }
        b.btnResume.setOnClickListener { resumeTimer() }
        b.btnReset.setOnClickListener { resetTimer() }
        b.btnStop.setOnClickListener { stopAndExit() }

        tts = TextToSpeech(this, this)
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(remainingMs, 100) {
            override fun onTick(ms: Long) {
                remainingMs = ms
                renderTime(ms)
            }
            @RequiresPermission(Manifest.permission.VIBRATE)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFinish() {
                remainingMs = 0
                renderTime(0)
                vibrate()
                speak(getString(R.string.done))
                incrementTodayDone()
                finish()
            }
        }.start()
        b.btnPause.isEnabled = true
        b.btnResume.isEnabled = false
    }

    private fun pauseTimer() {
        timer?.cancel()
        b.btnPause.isEnabled = false
        b.btnResume.isEnabled = true
    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun resetTimer() {
        timer?.cancel()
        remainingMs = totalMs
        renderTime(remainingMs)
        b.btnPause.isEnabled = true
        b.btnResume.isEnabled = false
        startTimer()
    }

    private fun stopAndExit() {
        timer?.cancel()
        tts?.stop()
        finish()
    }

    private fun renderTime(ms: Long) {
        val totalSec = (ms / 1000).toInt()
        val mm = totalSec / 60
        val ss = totalSec % 60
        b.txtRemaining.text = String.format("%02d:%02d", mm, ss)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrate() {
        val effect = VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    private fun speak(text: String) {
        tts?.language = Locale.US
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "done_id")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun incrementTodayDone() {
        val key = "done_${DateUtils.todayKey()}"
        val n = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, n).apply()
    }

    override fun onInit(status: Int) {
        val loc = resources.configuration.locales[0]
        // tenta pt-BR; senão, cai para en-US
        val pt = java.util.Locale("pt", "BR")
        val en = java.util.Locale.US
        val target = if (loc.language.equals("pt", true)) pt else en
        val result = tts?.setLanguage(target)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = en
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        tts?.shutdown()
        super.onDestroy()
    }
}
