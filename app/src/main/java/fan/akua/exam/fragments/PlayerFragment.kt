package fan.akua.exam.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import fan.akua.exam.AppState
import fan.akua.exam.R
import fan.akua.exam.databinding.FragmentPlayerBinding
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.utils.formatSecondsToTime
import fan.akua.exam.utils.logD
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPlayerBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentPlayerBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        eventListen(binding)
        binding.close.setOnClickListener {
            lifecycleScope.launch {
                AppState.closeMusic()
            }
        }
        binding.playType.setOnClickListener {
            when (PlayerManager.playMode.value) {
                PlayerManager.PlayMode.SINGLE_LOOP -> {
                    PlayerManager.setPlayMode(PlayerManager.PlayMode.LIST_LOOP)
                }

                PlayerManager.PlayMode.LIST_LOOP -> {
                    PlayerManager.setPlayMode(PlayerManager.PlayMode.RANDOM)
                }

                PlayerManager.PlayMode.RANDOM -> {
                    PlayerManager.setPlayMode(PlayerManager.PlayMode.SINGLE_LOOP)
                }
            }
        }
        binding.lastSong.setOnClickListener {
            PlayerManager.playNext()
        }
        binding.nextSong.setOnClickListener {
            PlayerManager.playNext()
        }
        binding.playPause.setOnClickListener {
            if (PlayerManager.pause.value) PlayerManager.start() else PlayerManager.pause()
        }
    }

    private fun eventListen(binding: FragmentPlayerBinding) {
        lifecycleScope.launch {
            PlayerManager.playMode.collect { mode ->
                binding.playType.run {
                    when (mode) {
                        PlayerManager.PlayMode.SINGLE_LOOP -> setImageResource(R.drawable.ic_play_type_circulation)
                        PlayerManager.PlayMode.LIST_LOOP -> setImageResource(R.drawable.ic_play_type_order)
                        PlayerManager.PlayMode.RANDOM -> setImageResource(R.drawable.ic_play_type_random)
                    }
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let { binding.flowView.setBitmap(it) }
            }
        }
        lifecycleScope.launch {
            PlayerManager.currentSong.collect { song ->
                song?.let {
                    binding.musicName.text = it.songName
                    binding.musicAuthor.text = it.author
                    binding.like.setImageResource(if (it.like) R.drawable.ic_like else R.drawable.ic_unlike)
                }
            }
        }

        lifecycleScope.launch {
            PlayerManager.duration.collect { duration ->
                "simon".logD("duration $duration ")
                binding.durationTime.text = duration.formatSecondsToTime()
                binding.progressBar.max = duration.toInt()
            }
        }
        lifecycleScope.launch {
            PlayerManager.progress.collect { progress ->
                binding.currentTime.text = progress.formatSecondsToTime()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.progressBar.setProgress(progress.toInt(), false)
                } else {
                    binding.progressBar.progress = progress.toInt()
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.pause.collect { isPause ->
                binding.playPause.setImageResource(if (isPause) R.drawable.ic_pausing else R.drawable.ic_playing)
            }
        }
    }
}
