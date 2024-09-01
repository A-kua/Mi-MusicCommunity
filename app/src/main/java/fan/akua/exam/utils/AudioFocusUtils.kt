package fan.akua.exam.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

@Suppress("DEPRECATION")
class AudioFocusUtils(
    context: Context,
    listener: OnAudioFocusChangeListener
) {
    private val mAudioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mListener: OnAudioFocusChangeListener = listener
    private var mAudioFocusRequest: AudioFocusRequest? = null
    private var mAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var mLossTransient = false
    private var mLossTransientCanDuck = false

    init {
        initAudioFocusChangeListener()
    }

    private fun initAudioFocusChangeListener() {
        mAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    mListener.onLoss()
                    mLossTransient = false
                    mLossTransientCanDuck = false
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    mLossTransient = true
                    mListener.onLossTransient()
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    mLossTransientCanDuck = true
                    mListener.onLossTransientCanDuck()
                }

                AudioManager.AUDIOFOCUS_GAIN -> {
                    mListener.onGain(mLossTransient, mLossTransientCanDuck)
                    mLossTransient = false
                    mLossTransientCanDuck = false
                }
            }
        }
    }

    fun requestAudioFocus(streamType: Int, durationHint: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusAPI26(streamType, durationHint)
        } else mAudioManager.requestAudioFocus(
            mAudioFocusChangeListener,
            streamType,
            durationHint
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusAPI26(streamType: Int, durationHint: Int): Int {
        mAudioFocusRequest = AudioFocusRequest.Builder(durationHint)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setLegacyStreamType(streamType)
                    .build()
            )
            .setOnAudioFocusChangeListener(mAudioFocusChangeListener!!)
            .build()
        return mAudioManager.requestAudioFocus(mAudioFocusRequest!!)
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            abandonAudioFocusAPI26()
            return
        }
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun abandonAudioFocusAPI26() {
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest ?: return)
    }

    interface OnAudioFocusChangeListener {
        /**
         * 音频焦点永久性丢失（此时应暂停播放）
         */
        fun onLoss()

        /**
         * 音频焦点暂时性丢失（此时应暂停播放）
         */
        fun onLossTransient()

        /**
         * 音频焦点暂时性丢失（此时只需降低音量，不需要暂停播放）
         */
        fun onLossTransientCanDuck()

        /**
         * 重新获取到音频焦点。
         *
         *
         * 如果应用因 [.onLossTransientCanDuck] 事件而降低了音量（lossTransientCanDuck 参数为 true），
         * 那么此时应恢复正常的音量。
         *
         * @param lossTransient        指示音频焦点是否是暂时性丢失，如果是，则此时可以恢复播放。
         * @param lossTransientCanDuck 指示音频焦点是否是可降低音量的暂时性丢失，如果是，则此时只需恢复音量即可。
         */
        fun onGain(lossTransient: Boolean, lossTransientCanDuck: Boolean)
    }
}