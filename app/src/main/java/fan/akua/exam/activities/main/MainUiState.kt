package fan.akua.exam.activities.main

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
    LOADING,
    SUCCESS,
    ERROR,
    Initial,
    All
}

fun MainUiState.toRVModels(): List<Any> {
    return (listOf(
        BannerModel(data = banner)
    ) + items.groupBy { it.style }
        .map { (style, infos) ->
            when (style) {
                2 -> LargeCard(infos.flatMap { it.musicInfoList })
                3 -> GirdModel(data = infos.flatMap { it.musicInfoList }, rowCount = 1)
                4 -> GirdModel(data = infos.flatMap { it.musicInfoList }, rowCount = 2)
                else -> {

                }
            }
        })
}
