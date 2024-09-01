package fan.akua.exam

import fan.akua.exam.events.CloseMusic
import fan.akua.exam.events.OpenMusic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppState {
    private val _openFlow = MutableSharedFlow<OpenMusic>()
    val openFlow = _openFlow.asSharedFlow()
    private val _closeFlow = MutableSharedFlow<CloseMusic>()
    val closeFlow = _closeFlow.asSharedFlow()

    suspend fun openMusic() {
        _openFlow.emit(OpenMusic())
    }

    suspend fun closeMusic() {
        _closeFlow.emit(CloseMusic())
    }
}