package fan.akua.exam.activities.main

import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GirdModel
import fan.akua.exam.activities.main.model.LargeCard
import fan.akua.exam.data.HomePageInfo
import fan.akua.exam.data.MusicInfo

data class MainUiState(
    val banner: List<MusicInfo> = emptyList(),
    val items: List<HomePageInfo> = emptyList(),
    val state: RequestState = RequestState.Initial
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
fun MainUiState.toRVModels(): List<ItemBind> {
    return listOf(
        BannerModel(data = banner)
    ) + items.map {
        when (it.style) {
            2 -> LargeCard(it.moduleConfigId, it.musicInfoList)
            3 -> GirdModel(it.moduleConfigId, it.musicInfoList, rowCount = 1)
            4 -> GirdModel(it.moduleConfigId, it.musicInfoList, rowCount = 2)
            else -> {
                GirdModel(it.moduleConfigId, it.musicInfoList, rowCount = 1)
            }
        }
    }.toList()
}
