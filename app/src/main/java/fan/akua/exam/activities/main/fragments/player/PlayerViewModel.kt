package fan.akua.exam.activities.main.fragments.player

import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fan.akua.exam.App
import fan.akua.exam.AppState
import fan.akua.exam.activities.main.intents.LikeMusicIntent
import fan.akua.exam.misc.utils.logD
import fan.akua.exam.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        PlayerUiState(
            PlayerPanelState(
                isPause = true,
                bitmap = null,
                songBean = null,
                playMode = PlayerManager.PlayMode.LIST_LOOP,
                duration = 0,
                currentTime = 0,
            ), PlayerPageState()
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun seekTo(long: Long) = viewModelScope.launch {
        PlayerManager.seekTo(long)
    }

    fun closePlayerPage() = viewModelScope.launch {
        AppState.closePlayerPage()
    }

    fun changePlayMode() = viewModelScope.launch {
        when (_uiState.value.playerPanelState.playMode) {
            PlayerManager.PlayMode.SINGLE_LOOP -> PlayerManager.setPlayMode(PlayerManager.PlayMode.LIST_LOOP)
            PlayerManager.PlayMode.LIST_LOOP -> PlayerManager.setPlayMode(PlayerManager.PlayMode.RANDOM)
            PlayerManager.PlayMode.RANDOM -> PlayerManager.setPlayMode(PlayerManager.PlayMode.SINGLE_LOOP)
        }
    }

    fun playLast() = viewModelScope.launch {
        PlayerManager.playLast()
    }

    fun playNext() = viewModelScope.launch {
        PlayerManager.playNext()
    }

    fun playPause() = viewModelScope.launch {
        if (PlayerManager.pause.value) PlayerManager.start() else PlayerManager.pause()
    }

    fun like() = viewModelScope.launch {
        val song = _uiState.value.playerPanelState.songBean
        song?.let {
            PlayerManager.like(it)
        }
    }

    /**
     * 监听热流：播放图片更新、播放暂停、切歌、播放模式、总时长、当前时长
     * 监听冷流：页面切换
     */
    init {
        viewModelScope.launch {
            AppState.switchPageIntent.collect { intent ->
                val updatedPlayerPageState =
                    _uiState.value.playerPageState.copy(page = intent.page)
                _uiState.value = _uiState.value.copy(playerPageState = updatedPlayerPageState)
            }
        }
        viewModelScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let {
                    val updatedPlayerPanelState =
                        _uiState.value.playerPanelState.copy(bitmap = bitmap)
                    _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
                }
            }
        }
        viewModelScope.launch {
            PlayerManager.pause.collect { isPause ->
                val updatedPlayerPanelState =
                    _uiState.value.playerPanelState.copy(isPause = isPause)
                _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
            }
        }
        viewModelScope.launch {
            PlayerManager.currentSong.collect { songBean ->
                songBean?.let {
                    val updatedPlayerPanelState =
                        _uiState.value.playerPanelState.copy(songBean = songBean)
                    _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
                }
            }
        }
        viewModelScope.launch {
            PlayerManager.playMode.collect { playMode ->
                val updatedPlayerPanelState =
                    _uiState.value.playerPanelState.copy(playMode = playMode)
                _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
            }
        }
        viewModelScope.launch {
            PlayerManager.duration.collect { duration ->
                val updatedPlayerPanelState =
                    _uiState.value.playerPanelState.copy(duration = duration)
                _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
            }
        }
        viewModelScope.launch {
            PlayerManager.progress.collect { progress ->
                val updatedPlayerPanelState =
                    _uiState.value.playerPanelState.copy(currentTime = progress)
                _uiState.value = _uiState.value.copy(playerPanelState = updatedPlayerPanelState)
            }
        }
    }
}