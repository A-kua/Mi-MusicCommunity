package fan.akua.exam.activities.main.model

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import fan.akua.exam.AppState
import fan.akua.exam.activities.main.intents.AddSongIntent
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.databinding.ItemTypeGridBinding
import fan.akua.exam.activities.main.intents.PlayMusicGroupIntent
import fan.akua.exam.data.toSongBean
import fan.akua.exam.misc.utils.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GridModel(val musicInfo: MusicInfo, val spanCount: Int) : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeGridBinding.bind(vh.itemView)

        // 170dp 120dp
        if (spanCount == 2) {
            val layoutParams = binding.parentCardView.layoutParams
            layoutParams.height = 170.dp(vh.context).toInt()
            binding.parentCardView.layoutParams = layoutParams
        } else {
            val layoutParams = binding.parentCardView.layoutParams
            layoutParams.height = 120.dp(vh.context).toInt()
            binding.parentCardView.layoutParams = layoutParams
        }
        Glide.with(binding.img)
            .load(musicInfo.coverUrl)
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .into(binding.img)
        binding.titleTextView.text = musicInfo.musicName
        binding.authorTextView.text = musicInfo.author


        binding.root.setOnClickListener {
            /**
             * 无法与ViewModel通信，需要借助热流。
             */
            CoroutineScope(Dispatchers.Main).launch {
                AppState.clickMusic(PlayMusicGroupIntent(musicInfo = musicInfo))
            }
        }
        binding.addButton.setOnClickListener {
            /**
             * 无法与ViewModel通信，需要借助热流。
             */
            CoroutineScope(Dispatchers.Main).launch {
                AppState.addSong(AddSongIntent(musicInfo.toSongBean()))
            }
        }
    }
}