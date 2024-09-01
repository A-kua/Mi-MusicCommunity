package fan.akua.exam.activities.main

import androidx.recyclerview.widget.DiffUtil
import com.drake.brv.item.ItemBind
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.misc.utils.areListsEqual

object MainDataMerge {
    fun merge(old: List<ItemBind?>, naw: List<ItemBind?>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return old.size
            }

            override fun getNewListSize(): Int {
                return naw.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = old[oldItemPosition]
                val nawItem = naw[oldItemPosition]
                if (oldItem is BannerModel && nawItem is BannerModel) {
                    return true
                } else if (oldItem is GridModel && nawItem is GridModel) {
                    return oldItem.musicInfo.id == nawItem.musicInfo.id
                } else if (oldItem is LargeCardModel && nawItem is LargeCardModel) {
                    return oldItem.data.moduleConfigId == nawItem.data.moduleConfigId
                }
                return false
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = old[oldItemPosition]
                val nawItem = naw[oldItemPosition]
                if (oldItem is BannerModel && nawItem is BannerModel) {
                    return oldItem.data.areListsEqual(nawItem.data) { aItem, bItem ->
                        !(aItem.id == bItem.id && aItem.musicName == bItem.musicName && aItem.author == bItem.author && aItem.coverUrl == bItem.coverUrl)
                    }
                } else if (oldItem is GridModel && nawItem is GridModel) {
                    val aMusicInfo = oldItem.musicInfo
                    val bMusicInfo = nawItem.musicInfo
                    return aMusicInfo.id == bMusicInfo.id && aMusicInfo.musicName == bMusicInfo.musicName && aMusicInfo.author == bMusicInfo.author && aMusicInfo.coverUrl == bMusicInfo.coverUrl
                } else if (oldItem is LargeCardModel && nawItem is LargeCardModel) {
                    return oldItem.data.musicInfoList.areListsEqual(nawItem.data.musicInfoList) { aItem, bItem ->
                        !(aItem.id == bItem.id && aItem.musicName == bItem.musicName && aItem.author == bItem.author && aItem.coverUrl == bItem.coverUrl)
                    }
                }
                return false
            }
        }, false)
    }
}
