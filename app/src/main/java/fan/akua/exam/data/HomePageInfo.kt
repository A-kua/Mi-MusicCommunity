package fan.akua.exam.data

data class HomePageInfo(
    val moduleConfigId: Int,
    val moduleName: String,
    val style: Int, // 1: banner, 2: 横滑大卡, 3: 一行一列, 4: 一行两列
    val musicInfoList: List<MusicInfo>
)

fun List<HomePageInfo>.separateBanner(): Pair<List<MusicInfo>, List<HomePageInfo>> {
    // 所有banner放到一起
    val bannerMusicInfo = filter { it.style == 1 }
        .flatMap { list ->
            list.musicInfoList
        }

    val otherModules =
        filter { it.style != 1 }

    return Pair(bannerMusicInfo, otherModules)
}