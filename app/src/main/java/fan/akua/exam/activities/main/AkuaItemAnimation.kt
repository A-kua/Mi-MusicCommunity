package fan.akua.exam.activities.main

import android.view.View
import com.drake.brv.animation.ItemAnimation
import fan.akua.exam.utils.AnimatorUtils

class AkuaItemAnimation : ItemAnimation {
    override fun onItemEnterAnimation(view: View) {
        AnimatorUtils.gptIntroAnimate(view, 0.2f, -510)
    }
}