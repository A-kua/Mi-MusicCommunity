package fan.akua.exam.data

data class HomePageInfo(
    val moduleConfigId: Int,
    val moduleName: String,
    val style: Int, // 1: banner, 2: 横滑大卡, 3: 一行一列, 4: 一行两列
    val musicInfoList: List<MusicInfo>
)