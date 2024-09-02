package fan.akua.exam.activities.main.fragments.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import fan.akua.exam.AppState
import fan.akua.exam.R
import fan.akua.exam.activities.main.RecyclerViewState
import fan.akua.exam.activities.main.fragments.ImageFragment
import fan.akua.exam.activities.main.fragments.LyricFragment
import fan.akua.exam.databinding.FragmentPlayerBinding
import fan.akua.exam.misc.anims.likeAnim
import fan.akua.exam.misc.anims.unLikeAnim
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.misc.utils.formatSecondsToTime
import fan.akua.exam.misc.utils.logD
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var binding: FragmentPlayerBinding

    private lateinit var imageFragment: ImageFragment
    private lateinit var lyricFragment: LyricFragment

    private var isTouching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resumeChildFragment(savedInstanceState)
        initialViews()
        initialIntent()

        lifecycleScope.launch {
            viewModel.uiState.collect { (playerPanelState, playerPageState) ->
                parsePlayerPanelState(playerPanelState)
                parsePlayerPageState(playerPageState)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.flowView.startAnimator()
    }

    override fun onStop() {
        binding.flowView.pauseAnimator()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.flowView.release()
        super.onDestroyView()
    }

    /**
     * 尝试恢复Fragment
     */
    private fun resumeChildFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            imageFragment = ImageFragment()
            lyricFragment = LyricFragment()
            childFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.container, imageFragment, ImageFragment::class.qualifiedName)
                add(R.id.container, lyricFragment, LyricFragment::class.qualifiedName)
                hide(lyricFragment)
            }
        } else {
            imageFragment =
                childFragmentManager.findFragmentByTag(ImageFragment::class.qualifiedName) as ImageFragment
            lyricFragment =
                childFragmentManager.findFragmentByTag(LyricFragment::class.qualifiedName) as LyricFragment
        }
    }

    private var previousPlayerPanelState: PlayerPanelState? = null
    private fun parsePlayerPanelState(playerPanelState: PlayerPanelState) {
        if (previousPlayerPanelState != null)
            if (previousPlayerPanelState == playerPanelState) return

        playerPanelState.bitmap?.let {
            if (previousPlayerPanelState?.bitmap == playerPanelState.bitmap) return@let
            binding.flowView.setBitmap(it)
        }
        binding.playPause.run {
            if (playerPanelState.isPause && binding.playPause.isPlay) pause()
            else if (!binding.playPause.isPlay) play()
        }
        if (previousPlayerPanelState?.playMode != playerPanelState.playMode)
            binding.playType.run {
                when (playerPanelState.playMode) {
                    PlayerManager.PlayMode.SINGLE_LOOP -> setImageResource(R.drawable.ic_play_type_circulation)
                    PlayerManager.PlayMode.LIST_LOOP -> setImageResource(R.drawable.ic_play_type_order)
                    PlayerManager.PlayMode.RANDOM -> setImageResource(R.drawable.ic_play_type_random)
                }
            }
        if (previousPlayerPanelState?.songBean != playerPanelState.songBean) {
            playerPanelState.songBean?.let {
                binding.musicName.text = it.songName
                binding.musicAuthor.text = it.author
            }
        }
        playerPanelState.songBean?.let {
            binding.like.tag = it.like
            binding.like.setImageResource(if (it.like) R.drawable.ic_like else R.drawable.ic_unlike)
        }
        if (previousPlayerPanelState?.duration != playerPanelState.duration)
            playerPanelState.duration.run {
                binding.durationTime.text = formatSecondsToTime()
                binding.progressBar.max = toInt()
            }
        playerPanelState.currentTime.run {
            if (isTouching) return@run
            binding.currentTime.text = formatSecondsToTime()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.progressBar.setProgress(toInt(), false)
            } else {
                binding.progressBar.progress = toInt()
            }
        }

        previousPlayerPanelState = playerPanelState
    }

    private var previousPlayerPageState: PlayerPageState? = null
    private fun parsePlayerPageState(playerPageState: PlayerPageState) {
        if (previousPlayerPageState != null)
            if (previousPlayerPageState == playerPageState) return
        when (playerPageState.page) {
            PageMode.Image -> childFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .hide(lyricFragment)
                .show(imageFragment)
                .commit()

            PageMode.Lyric -> childFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .hide(imageFragment)
                .show(lyricFragment)
                .commit()
        }
        previousPlayerPageState = playerPageState
    }

    /**
     * 初始化View
     */
    private fun initialViews() {
        binding.progressBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTouching = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isTouching = false
                viewModel.seekTo(seekBar.progress.toLong())
            }
        })
        binding.flowView.setColorEnhance(true)
    }

    /**
     * 初始化主动意图
     */
    private fun initialIntent() {
        binding.close.setOnClickListener {
            viewModel.closePlayerPage()
        }
        binding.playType.setOnClickListener {
            viewModel.changePlayMode()
        }
        binding.lastSong.setOnClickListener {
            viewModel.playLast()
        }
        binding.nextSong.setOnClickListener {
            viewModel.playNext()
        }
        binding.playPause.setOnClickListener {
            viewModel.playPause()
        }
        binding.like.setOnClickListener {
            if (binding.like.tag as Boolean)
                binding.like.unLikeAnim(
                    onStart = {

                    }, onEnd = {
                        viewModel.like()
                    })
            else
                binding.like.likeAnim(
                    onStart = {
                        viewModel.like()
                    },
                    onEnd = {

                    })
        }
    }
}
