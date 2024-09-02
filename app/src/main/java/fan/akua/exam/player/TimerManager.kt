package fan.akua.exam.player

import android.os.Handler
import android.os.Looper

class TimerManager(
    private val onUpdate: () -> Unit
) {
    private var isPaused: Boolean = false
    private var interval: Long = 900L
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun startTimer() {
        stopTimer()
        isPaused = false
        runnable = object : Runnable {
            override fun run() {
                if (!isPaused) {
                    onUpdate()
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(runnable!!)
    }

    fun pauseTimer() {
        isPaused = true
    }

    fun resumeTimer() {
        isPaused = false
    }

    fun stopTimer() {
        runnable?.let {
            handler.removeCallbacks(it)
        }
        runnable = null
    }
}