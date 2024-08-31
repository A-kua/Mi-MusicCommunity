package fan.akua.exam

import android.app.Application
import android.util.Log
import com.drake.statelayout.StateConfig
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.BuildConfig
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val mmkvPath = MMKV.initialize(this)
        if (BuildConfig.DEBUG)
            Log.i("MMKV", mmkvPath)
//        StateConfig.apply {
//            emptyLayout = R.layout.layout_empty
//            errorLayout = R.layout.layout_error
//            loadingLayout = R.layout.layout_loading
//        }
    }
}