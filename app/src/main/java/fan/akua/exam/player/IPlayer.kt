package fan.akua.exam.player

import kotlinx.coroutines.flow.StateFlow

interface IPlayer<B> {
    var completionListener: ((IPlayer<B>) -> Unit)?
    var errorListener: ((IPlayer<B>) -> Unit)?
    var preparedListener: ((IPlayer<B>, B?) -> Unit)?

    val pauseFlow: StateFlow<Boolean>
    val currentProgressFlow: StateFlow<Long>
    val durationFlow: StateFlow<Long>
    val songFlow: StateFlow<B?>

    var autoStart: Boolean

    fun setBean(bean: B)
    fun start()
    fun pause()
    fun release()
    fun seekTo(long: Long)
    fun getDuration(): Long
}