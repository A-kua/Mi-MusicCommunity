package fan.akua.exam.activities.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter
import fan.akua.exam.R
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.databinding.ItemBannerBinding


class MainBannerAdapter(var data: List<MusicInfo>) :
    BannerAdapter<MusicInfo, MainBannerAdapter.BannerViewHolder>(data) {

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        )
        return BannerViewHolder(binding)
    }

    override fun onBindView(holder: BannerViewHolder, data: MusicInfo, position: Int, size: Int) {
        Glide.with(holder.itemView)
            .load(data.coverUrl)
            .into(holder.binding.img)
    }

    class BannerViewHolder(val binding: ItemBannerBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}