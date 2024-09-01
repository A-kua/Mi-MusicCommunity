package fan.akua.exam.data

data class MusicInfo(
    val id: Long,
    val musicName: String,
    val author: String,
    val coverUrl: String,
    val musicUrl: String,
    val lyricUrl: String,
)

fun MusicInfo.toSongBean(): SongBean {
    return SongBean(
        url = musicUrl,
        coverUrl = coverUrl,
        songName = musicName,
        id = id,
        lyric = lyricUrl,
        author = author,
        like = false,
    )
}