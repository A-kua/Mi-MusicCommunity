package fan.akua.exam.activities.main.model

import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import fan.akua.exam.AppState
import fan.akua.exam.R
import fan.akua.exam.misc.anims.AkuaItemAnimation
import fan.akua.exam.data.HomePageInfo
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.databinding.ItemLargecardBinding
import fan.akua.exam.databinding.ItemTypeLargecardBinding
import fan.akua.exam.activities.main.intents.PlayMusicIntent
import fan.akua.exam.misc.utils.GenericDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LargeCardModel(val data: HomePageInfo) : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeLargecardBinding.bind(vh.itemView)
        binding.title.text = vh.context.resources.getString(R.string.str_exclusive_song)
        // 数据复用优化
        if (binding.rv.adapter != null) {
            val diffUtilCallback = GenericDiffUtil(
                oldList = binding.rv.bindingAdapter.models as List<MusicInfo>,
                newList = data.musicInfoList,
                areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                areContentsTheSame = { oldItem, newItem ->
                    oldItem.coverUrl == newItem.coverUrl &&
                            oldItem.musicName == newItem.musicName &&
                            oldItem.author == newItem.author
                }
            )
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback,false)
            binding.rv.bindingAdapter.models = data.musicInfoList
            diffResult.dispatchUpdatesTo(binding.rv.bindingAdapter)
        } else {
            val width = binding.root.context.resources.displayMetrics.widthPixels * 0.8
            binding.rv.linear(RecyclerView.HORIZONTAL).setup {
                addType<MusicInfo>(R.layout.item_largecard)
                onCreate {
                    val itemLargeBinding = getBinding<ItemLargecardBinding>()
                    val layoutParams = itemLargeBinding.parentCardView.layoutParams
                    layoutParams.width = width.toInt()
                    itemLargeBinding.parentCardView.layoutParams = layoutParams
                }
                onBind {
                    val model = getModel<MusicInfo>()
                    val itemLargeBinding = getBinding<ItemLargecardBinding>()
                    itemLargeBinding.musicInfo = model
                    Glide.with(itemLargeBinding.img)
                        .load(model.coverUrl)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(itemLargeBinding.img)
                    itemLargeBinding.playButton.setOnClickListener {
                        Toast.makeText(
                            context,
                            "将${model.musicName}添加到音乐列表",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                onClick(R.id.parentCardView) {
                    /**
                     * 无法与ViewModel通信，需要借助热流。
                     */
                    CoroutineScope(Dispatchers.Main).launch {
                        AppState.clickMusic(PlayMusicIntent(musicInfo = getModel()))
                    }
                }
            }.models = data.musicInfoList
            binding.rv.bindingAdapter.setAnimation(AkuaItemAnimation())
            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.rv)
        }
    }
}