package fan.akua.exam

import fan.akua.exam.events.OpenMusic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppState {
    private val _openFlow = MutableSharedFlow<OpenMusic>()
    val openFlow = _openFlow.asSharedFlow()

    suspend fun openMusic(){
        _openFlow.emit(OpenMusic())
    }
}