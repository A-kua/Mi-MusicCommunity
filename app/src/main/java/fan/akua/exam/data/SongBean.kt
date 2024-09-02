package fan.akua.exam.data

data class SongBean(
    val url: String,
    val coverUrl: String,
    val songName: String,
    val id: Long,
    val lyricUrl: String,
    val author: String,
    var like: Boolean,
)