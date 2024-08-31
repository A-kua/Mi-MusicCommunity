package fan.akua.exam.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fan.akua.exam.api.MusicService
import fan.akua.exam.data.separateBanner
import fan.akua.exam.utils.logD
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