package fan.akua.exam.activities.main.fragments.player

import android.graphics.Bitmap
import fan.akua.exam.data.SongBean
import fan.akua.exam.player.PlayerManager

data class PlayerPanelState(
    val bitmap: Bitmap?,
    val isPause: Boolean,
    val songBean: SongBean?,
    val playMode: PlayerManager.PlayMode,
    val duration: Long,
    val currentTime: Long,
)

data class PlayerPageState(
    val page: PageMode = PageMode.Image
)

data class PlayerUiState(
    val playerPanelState: PlayerPanelState,
    val playerPageState: PlayerPageState,
)

enum class PageMode {
    Image, Lyric
}