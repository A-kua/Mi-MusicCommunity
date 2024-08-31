package fan.akua.exam.activities.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fan.akua.exam.api.MusicService
import fan.akua.exam.data.filterBannerMusicInfo
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

    fun loadNextPage() {
        Log.e("simon", "loadNext")
        viewModelScope.launch {
            if (currentPage <= totalPages) {
                _uiState.value = _uiState.value.copy(state = RequestState.LOADING)
                try {
                    val response = apiService.getHomePage(current = currentPage + 1)
                    if (response.code == 200) {
                        currentPage++
                        totalPages = response.data.total
                        val data = response.data
                        val (bannerList, otherList) = data.records.filterBannerMusicInfo()

                        _uiState.value = MainUiState(
                            banner = (bannerList + _uiState.value.banner).distinctBy { it.id },
                            items = (otherList + _uiState.value.items).distinctBy { it.moduleConfigId },
                            state = RequestState.SUCCESS
                        )
                        Log.e("simon", "update")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            state = RequestState.ERROR
                        )
                        Log.e("simon", "error")
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        state = RequestState.ERROR
                    )
                    Log.e("simon", "exce")
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    state = RequestState.All
                )
                Log.e("simon", "no more")
            }
        }
    }

    fun refresh() {
        currentPage = 0
        totalPages = 1
        loadNextPage()
    }

}