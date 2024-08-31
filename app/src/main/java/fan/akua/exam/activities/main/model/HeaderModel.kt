package fan.akua.exam.activities.main.model

import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import com.drake.brv.item.ItemHover
import fan.akua.exam.databinding.ItemTypeHeaderBinding

class HeaderModel : ItemBind {
    override fun onBind(vh: BindingAdapter.BindingViewHolder) {
        val binding = ItemTypeHeaderBinding.bind(vh.itemView)
    }
}