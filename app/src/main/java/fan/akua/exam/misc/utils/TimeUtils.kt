package fan.akua.exam.misc.utils

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun Long.formatSecondsToTime(): String {
    val totalSeconds = (this / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
