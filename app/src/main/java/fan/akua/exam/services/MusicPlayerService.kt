package fan.akua.exam.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media.session.MediaButtonReceiver
import fan.akua.exam.activities.main.MainActivity
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.misc.utils.AudioFocusUtils
import fan.akua.exam.misc.utils.logD
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 播放服务，不对外提供接口，用于监听事件、维护通知。
 * 仅与PlayerManager交换。
 */
class MusicPlayerService : LifecycleService(), AudioFocusUtils.OnAudioFocusChangeListener {
    companion object {
        private const val NOTIFICATION_ID = 330771794
    }

    private var mMediaSession: MediaSessionCompat? = null

    private var mNotificationManager: NotificationManagerCompat? = null

    private var mMusicNotification: NotificationDistribute? = null

    private val mAudioFocusUtils by lazy { AudioFocusUtils(this, this) }


    override fun onDestroy() {
        super.onDestroy()
        "simon".logD("onDestroy")
        mAudioFocusUtils.abandonAudioFocus() // 放弃音频焦点
        mMediaSession?.apply {
            release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mNotificationManager == null) {
            mNotificationManager = NotificationDistribute.createChannel(this)
        }
        if (mMediaSession == null) {
            mMediaSession =
                MediaSessionCompat.fromMediaSession(this, MediaSession(this, packageName)).apply {
                    isActive = true
                    setSessionActivity(
                        PendingIntent.getActivity(
                            this@MusicPlayerService,
                            0,
                            Intent(this@MusicPlayerService, MainActivity::class.java),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    setCallback(MediaSessionCallback())
                }

            mMusicNotification = NotificationDistribute.Builder(this, mMediaSession!!)
            "simon".logD("onStartCommand")
            PlayerManager.currentSong.onEach {
                "simon".logD("notificationSetSong ")
                if (it != null) {
                    "simon".logD("notificationSetSong "+it.songName)
                    mMusicNotification?.setSongBean(it)
                    refreshMediaNotifications()
                }
            }.launchIn(lifecycleScope)

            PlayerManager.progress.onEach {
                mMusicNotification?.setPosition(it)
            }.launchIn(lifecycleScope)

            PlayerManager.duration.onEach {
                "simon".logD("getDuration ")
                mMusicNotification?.setDuration(it)
            }.launchIn(lifecycleScope)

            PlayerManager.bitmapFlow.onEach {
                mMusicNotification?.setBitmap(it)
                refreshMediaNotifications()
            }.launchIn(lifecycleScope)

            PlayerManager.pause.onEach {
                mMusicNotification?.setPlaying(!it)
                refreshMediaNotifications()

                if (!it) {
                    mAudioFocusUtils.requestAudioFocus(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                } else {
                    mAudioFocusUtils.abandonAudioFocus()
                }
            }.launchIn(lifecycleScope)

            PlayerManager.playlist.onEach {
                //播放列表被清空
                //为了避免用户尝试恢复播放
                //直接把服务杀掉
//                if (it == null) {
//                    stopSelf()
//                }
            }.launchIn(lifecycleScope)
            //服务启动后需要在规定时间内startForeground，否则可能会报undServiceDidNotStartInTimeException错误
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(NOTIFICATION_ID, mMusicNotification!!.build())
            }
        }
        MediaButtonReceiver.handleIntent(mMediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun refreshMediaNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            startForeground(NOTIFICATION_ID, mMusicNotification!!.build())
            if (PlayerManager.pause.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                }
            }
        } else {
            mNotificationManager!!.notify(NOTIFICATION_ID, mMusicNotification!!.build())
        }
    }

    /**
     * 监听MediaSessionCallback事件
     */
    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            PlayerManager.seekTo(pos)
        }

        override fun onPlay() {
            super.onPlay()
            PlayerManager.start()
        }

        override fun onPause() {
            super.onPause()
            PlayerManager.pause()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            PlayerManager.playLast()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            PlayerManager.playNext()
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val action = mediaButtonEvent.action
            if (Intent.ACTION_MEDIA_BUTTON == action) {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonEvent.getParcelableExtra(
                        Intent.EXTRA_KEY_EVENT,
                        KeyEvent::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                    // 处理媒体按钮按下事件
                    when (keyEvent.keyCode) {

                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> {
                            when (keyEvent.repeatCount) {
                                0 -> {
                                    if (PlayerManager.pause.value) {
                                        PlayerManager.start()
                                    } else {
                                        PlayerManager.pause()
                                    }
                                }

                                1 -> {
                                    PlayerManager.playNext()
                                }

                                2 -> {
                                    PlayerManager.playLast()
                                }
                            }
                        }

                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            PlayerManager.start()
                        }

                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            PlayerManager.pause()
                        }

                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            PlayerManager.playNext()
                        }

                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            PlayerManager.playLast()
                        }

                        KeyEvent.KEYCODE_MEDIA_STOP -> {
                            PlayerManager.pause()
                            mNotificationManager!!.cancelAll()
                            stopSelf()
                        }
                    }
                }
            }
            return true
        }

    }

    override fun onLoss() {
        PlayerManager.pause()
    }

    override fun onLossTransient() {
        PlayerManager.pause()
    }

    override fun onLossTransientCanDuck() {

    }

    override fun onGain(lossTransient: Boolean, lossTransientCanDuck: Boolean) {
        if (lossTransient) {
            PlayerManager.start()
        }
    }
}
