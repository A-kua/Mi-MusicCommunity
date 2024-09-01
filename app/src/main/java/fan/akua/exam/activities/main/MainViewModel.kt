package fan.akua.exam.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import fan.akua.exam.AppState
import fan.akua.exam.data.api.MusicService
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.data.separateBanner
import fan.akua.exam.data.toSongBean
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.misc.utils.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel提供冷流（状态流、事件流）。
 * 主动、被动Intent均发送给ViewModel，ViewModel处理后发送Event。
 * 如果无法直接与ViewModel交互，则提供热流，意图发送给热流，ViewModel监听热流。
 */
class MainViewModel : ViewModel() {
    private val apiService: MusicService by lazy {
        MusicService.getApi()
    }
    private var currentPage = 0
    private var totalPages = 1

    private val _uiState = MutableStateFlow(
        MainUiState(
            RecyclerViewState(), MainPanelState(
                isPause = true,
                bitmap = null,
                songBean = null,
                visible = true
            ), SlidingViewState(PanelState.COLLAPSED)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun slidingShow() = viewModelScope.launch {
        val updatedSlidingViewState = SlidingViewState(state = PanelState.EXPANDED)
        _uiState.value = _uiState.value.copy(slidingViewState = updatedSlidingViewState)
    }


    fun slidingHide() = viewModelScope.launch {
        val updatedSlidingViewState = SlidingViewState(state = PanelState.COLLAPSED)
        _uiState.value = _uiState.value.copy(slidingViewState = updatedSlidingViewState)
    }


    fun panelHide() = viewModelScope.launch {
        val updatedMainPanelState = _uiState.value.panelState.copy(visible = false)
        _uiState.value = _uiState.value.copy(panelState = updatedMainPanelState)
    }


    fun panelShow() = viewModelScope.launch {
        val updatedMainPanelState = _uiState.value.panelState.copy(visible = true)
        _uiState.value = _uiState.value.copy(panelState = updatedMainPanelState)
    }


    fun loadNextPage() = viewModelScope.launch {
        if (currentPage >= totalPages) {
            postLoadedAll()
        } else {
            val updatedRecyclerViewState =
                _uiState.value.recyclerViewState.copy(state = RequestState.LOADING)
            _uiState.value = _uiState.value.copy(recyclerViewState = updatedRecyclerViewState)
            try {
                val response = apiService.getHomePage(current = currentPage + 1)
                if (response.code == 200) {
                    currentPage = response.data.current
                    totalPages = response.data.pages
                    val data = response.data
                    val (bannerList, otherItemList) = data.records.separateBanner()

                    val oldState = _uiState.value.recyclerViewState
                    val recyclerViewState = RecyclerViewState(
                        banner = (oldState.banner + bannerList).distinctBy { it.id },
                        items = (oldState.items + otherItemList).distinctBy { it.moduleConfigId },
                        state = RequestState.SUCCESS
                    )
                    _uiState.value = _uiState.value.copy(recyclerViewState = recyclerViewState)

                    if (response.data.current == response.data.pages)
                        postLoadedAll()
                } else {
                    postLoadError("internal error: ${response.msg}")
                }
            } catch (e: Exception) {
                postLoadError("get error: $e")
            }
        }
    }


    fun refresh() {
        currentPage = 0
        totalPages = 1
        loadNextPage()
    }


    /**
     * 已经加载了所有数据
     */
    private fun postLoadedAll() {
        val updatedRecyclerViewState =
            _uiState.value.recyclerViewState.copy(state = RequestState.All)
        _uiState.value = _uiState.value.copy(recyclerViewState = updatedRecyclerViewState)
    }

    /**
     * 加载数据出错
     */
    private fun postLoadError(msg: String) {
        val updatedRecyclerViewState =
            _uiState.value.recyclerViewState.copy(state = RequestState.All)
        _uiState.value = _uiState.value.copy(recyclerViewState = updatedRecyclerViewState)
    }

    /**
     * 监听热流：Item点击、播放图片更新、播放暂停、切歌、关闭播放页
     * 监听冷流：点击音乐，关闭播放页面
     */
    init {
        viewModelScope.launch {
            AppState.clickMusicIntent.collect { clickMusic ->
                playSong(clickMusic.musicInfo)
            }
        }
        viewModelScope.launch {
            AppState.closePlayerPageIntent.collect { clickMusic ->
                val updatedSlidingViewState = SlidingViewState(state = PanelState.COLLAPSED)
                _uiState.value = _uiState.value.copy(slidingViewState = updatedSlidingViewState)
            }
        }
        viewModelScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let {
                    val updatedPanelState =
                        _uiState.value.panelState.copy(bitmap = bitmap)
                    _uiState.value = _uiState.value.copy(panelState = updatedPanelState)
                }
            }
        }
        viewModelScope.launch {
            PlayerManager.pause.collect { isPause ->
                val updatedPanelState =
                    _uiState.value.panelState.copy(isPause = isPause)
                _uiState.value = _uiState.value.copy(panelState = updatedPanelState)
            }
        }
        viewModelScope.launch {
            PlayerManager.currentSong.collect { songBean ->
                songBean?.let {
                    val updatedPanelState =
                        _uiState.value.panelState.copy(songBean = songBean)
                    _uiState.value = _uiState.value.copy(panelState = updatedPanelState)
                }
            }
        }
    }

    private fun playSong(musicInfo: MusicInfo) {
        viewModelScope.launch {
            val recyclerViewState = _uiState.value.recyclerViewState
            var position = 0
            val musicList = recyclerViewState.items.first {
                position = it.musicInfoList.indexOf(musicInfo)
                position != -1
            }.musicInfoList.map {
                it.toSongBean()
            }
            if (position == -1) return@launch
            PlayerManager.play(musicList, position)
            slidingShow()
        }
    }
}