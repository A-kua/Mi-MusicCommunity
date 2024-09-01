package fan.akua.exam.activities.main.model

import androidx.recyclerview.widget.DiffUtil
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import com.youth.banner.indicator.CircleIndicator
import fan.akua.exam.activities.main.adapters.MainBannerAdapter
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.databinding.ItemTypeBannerBinding
import fan.akua.exam.misc.utils.GenericDiffUtil
import fan.akua.exam.misc.utils.logD

class BannerModel(val data: List<MusicInfo>) : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeBannerBinding.bind(vh.itemView)
        if (binding.bannerView.indicator == null)
            binding.bannerView.indicator = CircleIndicator(vh.context)
        if (binding.bannerView.adapter == null) {
            binding.bannerView.setAdapter(MainBannerAdapter(data))
        } else {
            val adapter = binding.bannerView.adapter as MainBannerAdapter
            val diffUtilCallback = GenericDiffUtil(
                oldList = adapter.data,
                newList = data,
                areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                areContentsTheSame = { oldItem, newItem ->
                    oldItem.coverUrl == newItem.coverUrl
                }
            )
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)
            adapter.data = data
            adapter.setDatas(data)
            diffResult.dispatchUpdatesTo(adapter)
        }

    }
}