package fan.akua.exam.misc.anims

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

fun View.likeAnim(onStart: () -> Unit, onEnd: () -> Unit) {
    val scaleAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.2f, 1.0f).apply {
        duration = 1000
    }

    val rotationAnimator = ObjectAnimator.ofFloat(this, "rotationY", 0f, 360f).apply {
        duration = 1000
    }

    val animatorSet = AnimatorSet().apply {
        playTogether(scaleAnimator, rotationAnimator)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                onStart()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onEnd()
            }
        })
    }
    animatorSet.start()
}

fun View.unLikeAnim(onStart: () -> Unit, onEnd: () -> Unit) {
    val scaleAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.8f, 1.0f).apply {
        duration = 1000
    }

    val scaleYAnimator = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.8f, 1.0f).apply {
        duration = 1000
    }

    val animatorSet = AnimatorSet().apply {
        playTogether(scaleAnimator, scaleYAnimator)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                onStart()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onEnd()
            }
        })
    }
    animatorSet.start()
}