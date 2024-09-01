package fan.akua.exam.misc.utils

import android.content.Context
import android.util.TypedValue

fun Int.dp(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )
}