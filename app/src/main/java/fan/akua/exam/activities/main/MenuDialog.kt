package fan.akua.exam.activities.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
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
    private var currentIndex: Int = -1
    private val selectTitleColor = Color.parseColor("#3325CD")
    private val selectAuthorColor = Color.parseColor("#993325CD")
    private val unSelectTitleColor = Color.parseColor("#000000")
    private val unSelectAuthorColor = Color.parseColor("#99000000")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialViews()
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
        lifecycleScope.launch {
            PlayerManager.index.collect { index ->
                currentIndex = index
                // Todo 性能优化
                binding.rv.bindingAdapter.notifyDataSetChanged()
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
                if (modelPosition == currentIndex) {
                    findView<TextView>(R.id.music_name).setTextColor(selectTitleColor)
                    findView<TextView>(R.id.music_author).setTextColor(selectAuthorColor)
                } else {
                    findView<TextView>(R.id.music_name).setTextColor(unSelectTitleColor)
                    findView<TextView>(R.id.music_author).setTextColor(unSelectAuthorColor)
                }
            }
            onClick(R.id.item_root) {
                viewModel.playMusic(getModel())
            }
            onClick(R.id.music_remove) {
                viewModel.removeMusic(getModel())
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
            binding.rv.bindingAdapter.models = playlist
            if (it.size != previousList?.size) {
                binding.playlistSize.text = "${playlist.size}"
                if (previousList != null && it.isEmpty()) {
                    dismissAllowingStateLoss()
                }
            }
        }
        previousList = playlist
    }

    override fun getCornerRadius(): Float {
        return 20.dp(requireContext())
    }

    override fun getPeekHeight(): Int {
        // 黄金分割
        return (requireContext().resources.displayMetrics.heightPixels * 0.382).roundToInt()
    }

    override fun onDestroy() {
        viewModel.closeMenu()
        super.onDestroy()
    }

}