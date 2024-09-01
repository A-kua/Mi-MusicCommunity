package fan.akua.exam.utils

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun Long.formatSecondsToTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60

    return String.format("%02d:%02d", hours, minutes)
}
