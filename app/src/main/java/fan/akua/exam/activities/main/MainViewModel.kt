package fan.akua.exam.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fan.akua.exam.App
import fan.akua.exam.AppState
import fan.akua.exam.api.MusicService
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.data.separateBanner
import fan.akua.exam.data.toSongBean
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.utils.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val apiService: MusicService by lazy {
        MusicService.getApi()
    }

    private val _uiState = MutableStateFlow(MainUiState())
    private var currentPage = 0
    private var totalPages = 1
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private fun postLoadedAll() {
        _uiState.value = _uiState.value.copy(
            state = RequestState.All
        )
        "MainViewModel".logD("no more data")
    }

    private fun postLoadError(msg: String) {
        _uiState.value = _uiState.value.copy(
            state = RequestState.ERROR
        )
        "MainViewModel".logD(msg)
    }

    init {
        viewModelScope.launch {
            AppState.clickMusicFlow.collect { clickMusic ->
                playSong(clickMusic.musicInfo)
            }
        }
    }

    private fun playSong(musicInfo: MusicInfo) {
        viewModelScope.launch {
            val state = _uiState.value
            var position = 0
            val musicList = state.items.first {
                position = it.musicInfoList.indexOf(musicInfo)
                position != -1
            }.musicInfoList.map {
                it.toSongBean()
            }
            if (position == -1) return@launch
            PlayerManager.play(musicList, position)

            CoroutineScope(Dispatchers.Main).launch {
                AppState.openMusic()
            }
        }
    }

    fun loadNextPage() {
        "MainViewModel".logD("loadNextPage")
        viewModelScope.launch {
            if (currentPage >= totalPages) {
                postLoadedAll()
            } else {
                _uiState.value = _uiState.value.copy(state = RequestState.LOADING)
                try {
                    val response = apiService.getHomePage(current = currentPage + 1)
                    if (response.code == 200) {
                        currentPage = response.data.current
                        totalPages = response.data.pages
                        val data = response.data
                        val (bannerList, otherItemList) = data.records.separateBanner()

                        _uiState.value = MainUiState(
                            banner = (_uiState.value.banner + bannerList).distinctBy { it.id },
                            items = (_uiState.value.items + otherItemList).distinctBy { it.moduleConfigId },
                            state = RequestState.SUCCESS
                        )

                        if (response.data.current == response.data.pages)
                            postLoadedAll()
                        "MainViewModel".logD("update ${response.data.current} : ${response.data.pages}")
                    } else {
                        postLoadError("internal error: ${response.msg}")
                    }
                } catch (e: Exception) {
                    postLoadError("get error: $e")
                }
            }
        }
    }

    fun refresh() {
        "MainViewModel".logD("refresh")
        currentPage = 0
        totalPages = 1
        loadNextPage()
    }

}