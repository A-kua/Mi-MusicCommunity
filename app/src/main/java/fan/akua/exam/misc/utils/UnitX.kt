package fan.akua.exam.misc.utils

import android.content.Context
import android.util.TypedValue

private val dpCache = mutableMapOf<Int, Float>()

fun Int.dp(context: Context): Float {
    return dpCache[this] ?: run {
        val dpValue = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        )
        dpCache[this] = dpValue
        dpValue
    }
}