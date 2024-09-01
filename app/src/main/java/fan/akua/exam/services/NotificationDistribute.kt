package fan.akua.exam.services

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import fan.akua.exam.R
import fan.akua.exam.data.SongBean
import fan.akua.exam.misc.utils.logD

/**
 * MusicPlayerService的一个模块，负责创建通知。
 */
open class NotificationDistribute private constructor(
    private val context: Context,
    private val sessionCompat: MediaSessionCompat

) {
    class Builder(context: Context, sessionCompat: MediaSessionCompat) :
        NotificationDistribute(context, sessionCompat)

    private val metadata = MediaMetadataCompat.Builder()

    private val notificationCompat = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

    private var playing = false

    private var position = 0L

    private val skipPrevious =
        NotificationCompat.Action(
            R.drawable.ic_last_song,
            "SKIP_TO_PREVIOUS",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
    private val pause =
        NotificationCompat.Action(
            R.drawable.ic_pausing,
            "PAUSE",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
    private val play =
        NotificationCompat.Action(
            R.drawable.ic_playing,
            "PLAY",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_PLAY
            )
        )
    private val skipNext =
        NotificationCompat.Action(
            R.drawable.ic_next_song,
            "NEXT",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )

    private val stop =
        NotificationCompat.Action(
            R.drawable.ic_close,
            "STOP",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_STOP
            )
        )

    init {
        notificationCompat.apply {
            setShowWhen(false)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_icon)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                        .setShowActionsInCompactView(0, 1, 2, 3)
                        .setMediaSession(sessionCompat.sessionToken)
                )
            setContentIntent(sessionCompat.controller.sessionActivity)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
    }

    fun build(): Notification {
        notificationCompat.clearActions()
        listOf(
            skipPrevious,
            if (playing) play else pause,
            skipNext
        ).forEach {
            notificationCompat.addAction(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationCompat.addAction(stop)
        }
        return notificationCompat.build()
    }

    fun setSongBean(songBean: SongBean) {
        metadata
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songBean.songName)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songBean.author)

        notificationCompat.apply {
            setContentTitle(songBean.songName)
            setContentText(songBean.author)
        }

        sessionCompat.setMetadata(metadata.build())
    }

    fun setPlaying(playing: Boolean) {
        this.playing = playing
        sessionCompat.setPlaybackState(
            PlaybackStateBuilder.setState(
                if (playing) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING,
                position,
                0F
            ).build()
        )
    }

    fun setPosition(position: Long) {
        this.position = position
        sessionCompat.setPlaybackState(
            PlaybackStateBuilder.setState(
                if (playing) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING,
                position,
                0F
            ).build()
        )
    }

    fun setDuration(duration: Long) {
        metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        sessionCompat.setMetadata(metadata.build())
    }

    fun setBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            sessionCompat.setMetadata(metadata.build())
        }
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "media_notification_channel"

        private val PlaybackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE
            )

        fun createChannel(context: Context): NotificationManagerCompat {
            val notificationManager = NotificationManagerCompat.from(context)
            val channel = NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            ).apply {
                setName(context.getString(R.string.channel_name))
                setDescription(context.getString(R.string.channel_name))
                setVibrationEnabled(false)
                setShowBadge(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel.build())
            return notificationManager
        }
    }
}
