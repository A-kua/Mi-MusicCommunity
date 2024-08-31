package fan.akua.exam.data

data class HomePageInfo(
    val moduleConfigId: Int,
    val moduleName: String,
    val style: Int, // 1: banner, 2: 横滑大卡, 3: 一行一列, 4: 一行两列
    val musicInfoList: List<MusicInfo>
)

fun List<HomePageInfo>.filterBannerMusicInfo(): Pair<List<MusicInfo>, List<HomePageInfo>> {
    val bannerMusicInfo = filter { it.style == 1 }
        .flatMap { it.musicInfoList }

    val otherModules = filter { it.style != 1 }

    return Pair(bannerMusicInfo, otherModules)
}