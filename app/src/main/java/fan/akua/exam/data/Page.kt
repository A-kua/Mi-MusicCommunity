package fan.akua.exam.data

data class Page(
    val records: List<HomePageInfo>, // 音乐列表
    val total: Int,                   // 总数
    val size: Int,                    // 当前页大小
    val current: Int,                 // 当前页
    val pages: Int                    // 总页数
)