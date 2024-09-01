package fan.akua.exam.player

import TimerManager
import android.media.MediaPlayer
import fan.akua.exam.data.SongBean
import fan.akua.exam.utils.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 封装的播放器，内部处理生命周期。
 * 仅处理某个SongBean。
 */
class AndroidMusicPlayer(
    override var completionListener: ((IPlayer<SongBean>) -> Unit)?,
    override var errorListener: ((IPlayer<SongBean>) -> Unit)?,
    override var preparedListener: ((IPlayer<SongBean>, SongBean?) -> Unit)?
) : IPlayer<SongBean> {

    private val mediaPlayer = MediaPlayer().apply {
        resetState(this)
    }
    private val timerManager = TimerManager({
        if (mediaPlayer.isPlaying)
            _currentProgressFlow.value = mediaPlayer.currentPosition.toLong()
    })

    private fun resetState(mediaPlayer: MediaPlayer) {
        mediaPlayer.apply {
            reset()
            setOnPreparedListener {
                preparedListener?.invoke(this@AndroidMusicPlayer, _songFlow.value)
                if (_autoStart) this@AndroidMusicPlayer.start()
                _durationFlow.value = this@AndroidMusicPlayer.getDuration()
                "simon".logD("duration flow")
            }
            setOnErrorListener { _, _, _ ->
                errorListener?.invoke(this@AndroidMusicPlayer)
                _pauseFlow.value = true
                return@setOnErrorListener true
            }
            setOnCompletionListener {
                completionListener?.invoke(this@AndroidMusicPlayer)
                _pauseFlow.value = true
            }
        }
    }


    private val _pauseFlow = MutableStateFlow(true)
    override val pauseFlow: StateFlow<Boolean> = _pauseFlow

    private val _currentProgressFlow = MutableStateFlow(0L)
    override val currentProgressFlow: StateFlow<Long> = _currentProgressFlow

    private val _durationFlow = MutableStateFlow(0L)
    override val durationFlow: StateFlow<Long> = _durationFlow

    private val _songFlow = MutableStateFlow<SongBean?>(null)
    override val songFlow: StateFlow<SongBean?> = _songFlow

    private var _autoStart: Boolean = true
    override var autoStart = _autoStart

    override fun setBean(bean: SongBean) {
        _songFlow.value = bean
        _pauseFlow.value = true
        _currentProgressFlow.value = 0
        _durationFlow.value = 0
        resetState(mediaPlayer)
        mediaPlayer.setDataSource(bean.url)
        mediaPlayer.prepareAsync()
    }

    override fun start() {
        mediaPlayer.start()
        timerManager.startTimer()
        _pauseFlow.value = false
        "simon".logD("pause ${_pauseFlow.value}")
    }

    override fun pause() {
        mediaPlayer.pause()
        timerManager.pauseTimer()
        _pauseFlow.value = true
    }

    override fun release() {
        mediaPlayer.release()
        timerManager.stopTimer()
        _pauseFlow.value = true
    }

    override fun seekTo(long: Long) = mediaPlayer.seekTo(long.toInt())

    override fun getDuration(): Long = mediaPlayer.duration.toLong()
}