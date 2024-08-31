package fan.akua.exam.activities.main.model

import com.drake.brv.item.ItemBind
import fan.akua.exam.data.MusicInfo

interface BaseModel {
    val modelID: Int
    val data: List<MusicInfo>
}