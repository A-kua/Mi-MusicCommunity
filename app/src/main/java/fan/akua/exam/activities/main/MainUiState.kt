package fan.akua.exam.activities.main

import android.content.res.Resources
import android.graphics.Bitmap
import com.drake.brv.item.ItemBind
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.activities.main.model.TitleModel
import fan.akua.exam.data.HomePageInfo
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.data.SongBean

/**
 * BannerModel需要List<MusicInfo>
 * LargeCardModel需要HomePageInfo
 */
data class RecyclerViewState(
    val banner: List<MusicInfo> = emptyList(),
    val items: List<HomePageInfo> = emptyList(),
    val state: RequestState = RequestState.Initial
)

data class MainPanelState(
    val bitmap: Bitmap?,
    val isPause: Boolean,
    val songBean: SongBean?,
    val visible: Boolean,
)

data class SlidingViewState(
    val state: SlidingUpPanelLayout.PanelState
)

data class MainUiState(
    val recyclerViewState: RecyclerViewState,
    val panelState: MainPanelState,
    val slidingViewState: SlidingViewState
)

enum class RequestState {
    LOADING, // 加载中
    SUCCESS, // 加载成功
    ERROR, // 加载失败
    Initial, // 初始
    All // 已加载所有
}

/**
 * 对Banner采用合并，对其他采用转换
 */
fun RecyclerViewState.toRVModels(resources: Resources): List<ItemBind> {
    val largeModels = items.filter { it.style == 2 }.map { LargeCardModel(it) }

    val gridModels = items.filter { it.style != 2 }.map { Pair(it.style, it.musicInfoList) }

    val otherList = mutableListOf<ItemBind>()

    for (pair in gridModels) {
        otherList.add(
            TitleModel(
                resources.getString(if (pair.first == 3) R.string.str_hot_rank else R.string.str_daily_recommend)
            )
        )
        otherList.addAll(pair.second.map {
            GridModel(
                musicInfo = it,
                spanCount = if (pair.first == 3) 1 else 2
            )
        })
    }


    return listOf(BannerModel(banner)) + otherList + largeModels
}