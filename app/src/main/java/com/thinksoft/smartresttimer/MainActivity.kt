package com.thinksoft.smartresttimer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.thinksoft.smartresttimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val prefs by lazy { getSharedPreferences("smartrest", MODE_PRIVATE) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnStartCustom.isEnabled = false

        b.edtCustom.addTextChangedListener {
            val sec = it?.toString()?.trim()?.toIntOrNull()
            b.btnStartCustom.isEnabled = (sec != null && sec > 0)
        }

        b.btn45.setOnClickListener { openTimer(45) }
        b.btn60.setOnClickListener { openTimer(60) }
        b.btn90.setOnClickListener { openTimer(90) }

        b.btnStartCustom.setOnClickListener {
            val sec = b.edtCustom.text.toString().trim().toIntOrNull()
            if (sec != null && sec > 0) openTimer(sec) else b.edtCustom.error = getString(R.string.error_seconds_gt_zero)
        }

        updateStats()
    }

    private fun openTimer(seconds: Int) {
        val i = Intent(this, TimerActivity::class.java).apply {
            putExtra("seconds", seconds)
        }
        startActivity(i)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStats() {
        val todayKey = DateUtils.todayKey()
        val count = prefs.getInt("done_$todayKey", 0)
        b.txtStats.text = getString(R.string.completed_today, count)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        updateStats()
    }
}
