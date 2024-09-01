package fan.akua.exam.utils;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.util.Log;
import android.util.Property;
import android.view.View;

public class AnimatorUtils {

    public static float getDistanceToCenter(View paramView) {
        float f = paramView.getTop() + paramView.getHeight() / 2.0F;
        return (float) ((View) paramView.getParent()).getHeight() / 2 + paramView.getHeight() / 2.0F - f;
    }

    public static float getDistanceToCenterX(View paramView) {
        float f = paramView.getLeft() + paramView.getWidth() / 2.0F;
        return (float) ((View) paramView.getParent()).getWidth() / 2 + paramView.getWidth() / 2.0F - f;
    }


    public static void gptIntroAnimate(View targetLayout, float translationDis, int duration) {
        if (targetLayout.getAnimation() != null)
            targetLayout.getAnimation().cancel();
        targetLayout.clearAnimation();
        targetLayout.setPivotY(getDistanceToCenter(targetLayout));
        targetLayout.setPivotX(getDistanceToCenterX(targetLayout));
        targetLayout.setCameraDistance(10000.0F * targetLayout.getResources().getDisplayMetrics().density);

        // 初始化状态
        targetLayout.setTranslationY(targetLayout.getResources().getDisplayMetrics().heightPixels);
        targetLayout.setTranslationX(-targetLayout.getResources().getDisplayMetrics().widthPixels);
        targetLayout.setRotationX(60.0F);
        targetLayout.setScaleX(0.5F);
        targetLayout.setScaleY(0.5F);

        ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(targetLayout, View.TRANSLATION_Y, -translationDis * targetLayout.getResources().getDisplayMetrics().density, 0.0F);
        translateYAnimator.setDuration(800L);
        translateYAnimator.setInterpolator(new ExpoOut());
        translateYAnimator.setStartDelay(duration + 700);

        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(targetLayout, View.TRANSLATION_X, -targetLayout.getResources().getDisplayMetrics().widthPixels, 0.0F);
        translateXAnimator.setDuration(800L);
        translateXAnimator.setInterpolator(new ExpoOut());
        translateXAnimator.setStartDelay(duration + 700);

        ObjectAnimator rotationXAnimator = ObjectAnimator.ofFloat(targetLayout, View.ROTATION_X, 60.0F, 0.0F);
        rotationXAnimator.setDuration(1000L);
        rotationXAnimator.setInterpolator(new QuintInOut());
        rotationXAnimator.setStartDelay(duration + 1000);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(targetLayout, View.SCALE_X, 0.5F, 1.0F);
        scaleXAnimator.setDuration(1000L);
        scaleXAnimator.setInterpolator(new CircInOut());
        scaleXAnimator.setStartDelay(duration + 1000);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(targetLayout, View.SCALE_Y, 0.5F, 1.0F);
        scaleYAnimator.setDuration(1000L);
        scaleYAnimator.setInterpolator(new CircInOut());
        scaleYAnimator.setStartDelay(duration + 1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translateYAnimator, translateXAnimator, rotationXAnimator, scaleXAnimator, scaleYAnimator);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 确保最终状态
                targetLayout.setTranslationY(0);
                targetLayout.setTranslationX(0);
                targetLayout.setRotationX(0);
                targetLayout.setScaleX(1.0F);
                targetLayout.setScaleY(1.0F);
            }
        });

        animatorSet.start();
    }

    public static class QuadInOut implements TimeInterpolator {
        public float getInterpolation(float paramFloat) {
            float f1 = paramFloat * 2.0F;
            if (f1 < 1.0F) {
                return f1 * (0.5F * f1);
            }
            float f2 = f1 - 1.0F;
            return -0.5F * (f2 * (f2 - 2.0F) - 1.0F);
        }
    }

    public static class BackOut implements TimeInterpolator {
        protected float param_s = 1.70158F;

        public BackOut amount(float paramFloat) {
            this.param_s = paramFloat;
            return this;
        }

        public float getInterpolation(float paramFloat) {
            float f1 = this.param_s;
            float f2 = paramFloat - 1.0F;
            return 1.0F + f2 * f2 * (f1 + f2 * (f1 + 1.0F));
        }
    }


    public static class QuintInOut implements TimeInterpolator {
        public float getInterpolation(float paramFloat) {
            float f1 = paramFloat * 2.0F;
            if (f1 < 1.0F) {
                return f1 * (f1 * (f1 * (f1 * (0.5F * f1))));
            }
            float f2 = f1 - 2.0F;
            return 0.5F * (2.0F + f2 * (f2 * (f2 * (f2 * f2))));
        }
    }


    public static class CircInOut implements TimeInterpolator {
        public float getInterpolation(float paramFloat) {
            float f1 = paramFloat * 2.0F;
            if (f1 < 1.0F) {
                return -0.5F * ((float) Math.sqrt(1.0F - f1 * f1) - 1.0F);
            }
            float f2 = f1 - 2.0F;
            return 0.5F * (1.0F + (float) Math.sqrt(1.0F - f2 * f2));
        }
    }

    public static class ExpoOut implements TimeInterpolator {
        public float getInterpolation(float paramFloat) {
            if (paramFloat == 1.0F) {
                return 1.0F;
            }
            return 1.0F + -(float) Math.pow(2.0D, -10.0F * paramFloat);
        }
    }
}
