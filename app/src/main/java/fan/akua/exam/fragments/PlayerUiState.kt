package fan.akua.exam.fragments

import fan.akua.exam.data.SongBean
import fan.akua.exam.player.PlayerManager

data class PlayerUiState(
    val page: PageMode = PageMode.Image
) {
    enum class PageMode {
        Image, Lyric
    }
}
