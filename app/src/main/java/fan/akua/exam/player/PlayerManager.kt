package fan.akua.exam.player

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fan.akua.exam.App
import fan.akua.exam.data.SongBean
import fan.akua.exam.misc.utils.areListsEqual
import fan.akua.exam.misc.utils.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/**
 * 真正执行播放的类，提供监听状态变化的接口。
 * 处理播放列表事宜。
 */
object PlayerManager {
    enum class PlayMode {
        SINGLE_LOOP, LIST_LOOP, RANDOM
    }

    private var waitForPrepare: Boolean = false

    private val androidMusicPlayer: IPlayer<SongBean> by lazy {
        AndroidMusicPlayer(
            errorListener = {

            },
            preparedListener = { iPlayer, songBean ->
                if (waitForPrepare)
                    waitForPrepare = false
            },
            completionListener = {
                playNext()
            }
        )
    }

    /**
     * 不是播放器维护的，直接暴露
     */
    val duration = androidMusicPlayer.durationFlow
    val progress = androidMusicPlayer.currentProgressFlow
    val pause = androidMusicPlayer.pauseFlow

    private val _playMode = MutableStateFlow(PlayMode.LIST_LOOP)
    val playMode: StateFlow<PlayMode> = _playMode

    private val _currentSong = MutableStateFlow<SongBean?>(null)
    val currentSong: StateFlow<SongBean?> = _currentSong

    private val _playList = MutableStateFlow<List<SongBean>?>(null)
    val playlist: StateFlow<List<SongBean>?> = _playList

    private val _indexFlow = MutableStateFlow(-1)
    val index: StateFlow<Int> = _indexFlow

    private val _bitmapFlow = MutableStateFlow<Bitmap?>(null)
    val bitmapFlow: StateFlow<Bitmap?> = _bitmapFlow
    private var currentRequest: CustomTarget<Bitmap>? = null

    fun setPlayMode(mode: PlayMode) {
        _playMode.value = mode
    }

    fun like(songBean: SongBean) {
        _currentSong.value?.let {
            if (songBean.id == it.id)
                _currentSong.value = it.copy(like = !songBean.like)
        }
    }

    fun addSong(songBean: SongBean) {
        val currentList = _playList.value ?: emptyList()

        if (currentList.none { it.id == songBean.id }) {
            val updatedList = currentList + songBean
            _playList.value = updatedList
        }
    }

    fun removeSong(song: SongBean) {
        val currentList = _playList.value ?: return

        val updatedList = currentList.filter { it.id != song.id }
        _playList.value = updatedList

        if (updatedList.isNotEmpty())
            when (_playMode.value) {
                PlayMode.SINGLE_LOOP -> {
                    internalPlay(_indexFlow.value)
                }

                PlayMode.LIST_LOOP -> {
//                    val index = _indexFlow.value + 1
//                    internalPlay(index)
                }

                PlayMode.RANDOM -> {
                    internalPlay(Random.nextInt(0, updatedList.size))
                }
            }
    }

    fun play(songBean: SongBean) {
        val currentList = _playList.value ?: return

        val index = currentList.indexOfFirst { it.id == songBean.id }
        internalPlay(index)
    }

    fun play(list: List<SongBean>, index: Int) {
        if (!list.areListsEqual(_playList.value)) {
            _playList.value = list
        }
        internalPlay(index)
    }

    fun pause() {
        androidMusicPlayer.pause()
    }

    fun start() {
        androidMusicPlayer.start()
    }

    fun playNext() {
        when (_playMode.value) {
            PlayMode.SINGLE_LOOP -> {
                "bug9-2".logD("single loop ")
                singleLoopPlay(_indexFlow.value)
            }

            PlayMode.LIST_LOOP -> {
                val index = _indexFlow.value + 1
                internalPlay(index)
            }

            PlayMode.RANDOM -> {
                "bug9-2".logD("random  ")
                _playList.value?.let { playlist ->
                    if (playlist.size >= 2) {
                        val randomNumber = if (playlist.size == 2) {
                            if (_indexFlow.value == 0) 1 else 0
                        } else {
                            var newRandomNumber: Int
                            do {
                                newRandomNumber = Random.nextInt(0, playlist.size)
                            } while (newRandomNumber == _indexFlow.value)
                            newRandomNumber
                        }
                        internalPlay(randomNumber)
                    }
                }
            }

        }
    }

    fun playLast() {
        val index = _indexFlow.value - 1
        internalPlay(index)
    }

    fun seekTo(long: Long) {
        androidMusicPlayer.seekTo(long)
    }

    /**
     * 专门针对单曲播放的性能优化
     */
    private fun singleLoopPlay(index: Int) {
        val song = _playList.value?.getOrNull(index)
        if (song == _currentSong.value) {
            androidMusicPlayer.seekTo(0)
            androidMusicPlayer.start()
        } else {
            internalPlay(index)
        }
    }

    private fun internalPlay(index: Int) {
        val song = _playList.value?.getOrNull(index)
        song?.let {
            _indexFlow.value = index
            _currentSong.value = it
            androidMusicPlayer.setBean(song)
            "bug9-2".logD("song ${song.songName}")
            waitForPrepare = true

            // 如果有当前请求，取消它
            currentRequest?.let {
                Glide.with(App.context).clear(currentRequest)
            }

            currentRequest = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    _bitmapFlow.value = resource.copy(Bitmap.Config.ARGB_8888, false)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    currentRequest = null
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    currentRequest = null
                }
            }

            Glide.with(App.context)
                .asBitmap()
                .load(song.coverUrl)
                .override(500, 500)
                .into(currentRequest!!)
        }
    }

}