import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TimerManager(
    private val onUpdate: () -> Unit
) : CoroutineScope {
    private var job: Job? = null
    private var isPaused: Boolean = false
    private var interval: Long = 900L

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    fun startTimer() {
        job = launch {
            while (true) {
                if (!isPaused) {
                    delay(interval)
                    onUpdate()
                }
            }
        }
    }

    fun pauseTimer() {
        isPaused = true
    }

    fun resumeTimer() {
        isPaused = false
    }

    fun stopTimer() {
        job?.cancel()
        job = null
    }
}