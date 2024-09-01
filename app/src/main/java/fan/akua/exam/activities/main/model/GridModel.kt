package fan.akua.exam.activities.main.model

import android.widget.Toast
import com.bumptech.glide.Glide
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import fan.akua.exam.AppState
import fan.akua.exam.R
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.data.toSongBean
import fan.akua.exam.databinding.ItemTypeGridBinding
import fan.akua.exam.events.ClickMusic
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.utils.dp
import fan.akua.exam.utils.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GridModel(val musicInfo: MusicInfo, val spanCount: Int) : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeGridBinding.bind(vh.itemView)

        if (spanCount == 2) {
            val layoutParams = binding.parentCardView.layoutParams
            layoutParams.height = 170.dp(vh.context).toInt()
            binding.parentCardView.layoutParams = layoutParams
        }
        Glide.with(binding.img)
            .load(musicInfo.coverUrl)
            .into(binding.img)
        binding.titleTextView.text = musicInfo.musicName
        binding.authorTextView.text = musicInfo.author


        binding.root.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                AppState.clickMusic(ClickMusic(musicInfo = musicInfo))
            }
        }
        binding.playButton.setOnClickListener {
            Toast.makeText(
                vh.context,
                "将${musicInfo.musicName}添加到音乐列表",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}