package fan.akua.exam.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fan.akua.exam.api.MusicService
import kotlinx.coroutines.launch

class MainViewModel: ViewModel()  {

    private val apiService: MusicService by lazy {
        MusicService.getApi()
    }

    fun fetchHomePage(current: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getHomePage(current = current)
                // 打印响应内容
                println("Response Code: ${response.code}")
                println("Response Message: ${response.msg}")
                println("Data: ${response.data}")

            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}