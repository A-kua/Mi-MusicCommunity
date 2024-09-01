package fan.akua.exam

import fan.akua.exam.activities.main.fragments.player.PageMode
import fan.akua.exam.activities.main.intents.PlayMusicIntent
import fan.akua.exam.activities.main.intents.ClosePlayerPageIntent
import fan.akua.exam.activities.main.intents.SwitchPageIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppState {
    private val _closePlayerPageIntent = MutableSharedFlow<ClosePlayerPageIntent>()
    val closePlayerPageIntent = _closePlayerPageIntent.asSharedFlow()
    private val _switchPageIntent = MutableSharedFlow<SwitchPageIntent>()
    val switchPageIntent = _switchPageIntent.asSharedFlow()
    private val _playMusicIntentIntent = MutableSharedFlow<PlayMusicIntent>()
    val clickMusicIntent = _playMusicIntentIntent.asSharedFlow()

    /**
     * 关闭播放页面
     */
    suspend fun closePlayerPage() {
        _closePlayerPageIntent.emit(ClosePlayerPageIntent())
    }

    /**
     * 切换播放器页面
     */
    suspend fun switchPage(page: PageMode) {
        _switchPageIntent.emit(SwitchPageIntent(page))
    }

    /**
     * 点击音乐
     */
    suspend fun clickMusic(playMusicIntent: PlayMusicIntent) {
        _playMusicIntentIntent.emit(playMusicIntent)
    }
}