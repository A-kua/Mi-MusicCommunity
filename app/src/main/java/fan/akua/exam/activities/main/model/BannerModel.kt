package fan.akua.exam.activities.main.model

import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import com.youth.banner.indicator.CircleIndicator
import fan.akua.exam.activities.main.adapters.MainBannerAdapter
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.databinding.ItemTypeBannerBinding

class BannerModel(val data: List<MusicInfo>) : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeBannerBinding.bind(vh.itemView)
        binding.bannerView.indicator=CircleIndicator(vh.context)
        binding.bannerView.setAdapter(MainBannerAdapter(data))
    }
}