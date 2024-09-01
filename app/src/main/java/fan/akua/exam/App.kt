package fan.akua.exam

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.drake.statelayout.StateConfig
import com.tencent.mmkv.BuildConfig
import com.tencent.mmkv.MMKV
import fan.akua.exam.services.MusicPlayerService
import fan.akua.exam.utils.logD


class App : Application() {
    companion object {
        lateinit var context: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val mmkvPath = MMKV.initialize(this)
        if (BuildConfig.DEBUG)
            "MMkv".logD(mmkvPath)
        StateConfig.apply {
            emptyLayout = R.layout.layout_empty
            errorLayout = R.layout.layout_error
            loadingLayout = R.layout.layout_loading
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, MusicPlayerService::class.java))
        } else {
            startService(Intent(this, MusicPlayerService::class.java))
        }
    }
}