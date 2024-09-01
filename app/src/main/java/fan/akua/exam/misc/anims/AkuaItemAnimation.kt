package fan.akua.exam.misc.anims

import android.view.View
import com.drake.brv.animation.ItemAnimation
import fan.akua.exam.misc.utils.AnimatorUtils

class AkuaItemAnimation : ItemAnimation {
    override fun onItemEnterAnimation(view: View) {
        AnimatorUtils.gptIntroAnimate(view, 0.2f, -510)
    }
}