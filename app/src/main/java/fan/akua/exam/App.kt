package fan.akua.exam

import android.app.Application
import android.util.Log
import com.tencent.mmkv.BuildConfig
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val mmkvPath = MMKV.initialize(this)
        if (BuildConfig.DEBUG)
            Log.i("MMKV", mmkvPath)
    }
}