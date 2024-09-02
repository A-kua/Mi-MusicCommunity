package fan.akua.exam.activities.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.data.SongBean
import fan.akua.exam.databinding.FragmentMenuBinding
import fan.akua.exam.misc.utils.dp
import fan.akua.exam.misc.utils.logD
import fan.akua.exam.player.PlayerManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class MenuDialog : SuperBottomSheetFragment() {

    private lateinit var binding: FragmentMenuBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialViews()
        lifecycleScope.launch {
            viewModel.uiState.collect { (recyclerViewState, mainPanelState, _, _) ->

            }
        }
        lifecycleScope.launch {
            PlayerManager.playMode.collect { mode ->
                parsePlayMode(mode)
            }
        }
        lifecycleScope.launch {
            PlayerManager.playlist.collect { playlist ->
                parsePlayList(playlist)
            }
        }
    }

    private fun initialViews() {
        binding.rv.linear().setup {
            addType<SongBean>(R.layout.item_menu)
            onBind {
                val model = getModel<SongBean>()
                findView<TextView>(R.id.music_name).text = model.songName
                findView<TextView>(R.id.music_author).text = model.author
            }
        }
    }

    /**
     * 处理播放模式
     */
    private var previousMode: PlayerManager.PlayMode? = null
    private fun parsePlayMode(playMode: PlayerManager.PlayMode) {
        if (previousMode != null)
            if (previousMode == playMode) return
        when (playMode) {
            PlayerManager.PlayMode.SINGLE_LOOP -> {
                binding.chip.setChipIconResource(R.drawable.ic_play_type_circulation)
                binding.chip.text = resources.getString(R.string.str_mode_single)
            }

            PlayerManager.PlayMode.LIST_LOOP -> {
                binding.chip.setChipIconResource(R.drawable.ic_play_type_order)
                binding.chip.text = resources.getString(R.string.str_mode_loop)
            }

            PlayerManager.PlayMode.RANDOM -> {
                binding.chip.setChipIconResource(R.drawable.ic_play_type_random)
                binding.chip.text = resources.getString(R.string.str_mode_random)
            }
        }
        previousMode = playMode
    }

    /**
     * 处理列表
     */
    private var previousList: List<SongBean>? = null
    private fun parsePlayList(playlist: List<SongBean>?) {
        if (previousList != null)
            if (previousList == playlist) return
        playlist?.let {
            binding.rv.bindingAdapter.setDifferModels(playlist, false)
            if (it.size != previousList?.size) {
                binding.playlistSize.text = "${playlist.size}"
            }
        }
        previousList = playlist
    }

    override fun getCornerRadius(): Float {
        return 20.dp(requireContext())
    }

    override fun getPeekHeight(): Int {
        return (requireContext().resources.displayMetrics.widthPixels * 0.6767).roundToInt()
    }

    override fun onDestroy() {
        viewModel.closeMenu()
        super.onDestroy()
    }

}