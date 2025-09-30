package com.thinksoft.smartresttimer

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
    @RequiresApi(Build.VERSION_CODES.O)
    fun todayKey(): String = LocalDate.now().format(fmt)
}
