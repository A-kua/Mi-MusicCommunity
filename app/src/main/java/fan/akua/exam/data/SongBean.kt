package fan.akua.exam.data

import android.graphics.Bitmap

data class SongBean(
    val url: String,
    val coverUrl: String,
    val songName: String,
    val id: Long,
    val lyric: String?,
    val author: String,
    val like: Boolean,
)