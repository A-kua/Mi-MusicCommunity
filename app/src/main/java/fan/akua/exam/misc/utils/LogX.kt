package fan.akua.exam.misc.utils

import android.util.Log
import fan.akua.exam.BuildConfig


fun String.logD(msg: String) {
    if (BuildConfig.DEBUG)
        Log.d(this, msg)
}