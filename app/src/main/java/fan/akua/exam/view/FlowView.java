package fan.akua.exam.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.palette.graphics.Palette;

import fan.akua.exam.Constants;

public class FlowView extends View {

    private final RadialGradient[] gradients = new RadialGradient[5];
    private final ValueAnimator[] sizeValues = new ValueAnimator[5];
    private final ValueAnimator[][] positionValues = new ValueAnimator[5][2];
    // 3个flag  抗锯齿/位图标记/抗抖动
    private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

    private float vMax;
    private long flowTime = 12000;

    private int newBackGroundColor;

    private int backgroundColor = Color.argb(255, 249, 211, 237);
    private int flowColor[] = new int[5];

    public FlowView(Context context) {
        this(context, null);
    }

    @SuppressLint("Recycle")
    public FlowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化五个渐变的x,y位置变化
        for (int i = 0; i < positionValues.length; i++) {
            for (int j = 0; j < positionValues[i].length; j++) {
                positionValues[i][j] = new ValueAnimator();
                positionValues[i][j].setFloatValues(Constants.positionPath[i][j]);
                positionValues[i][j].setDuration(flowTime);
                positionValues[i][j].setRepeatCount(-1);
                positionValues[i][j].setInterpolator(new AccelerateDecelerateInterpolator());
            }
        }
        // 初始化五个渐变的缩放变化
        for (int i = 0; i < sizeValues.length; i++) {
            sizeValues[i] = new ValueAnimator();
            sizeValues[i].setFloatValues(Constants.sizePath[i]);
            sizeValues[i].setDuration(flowTime);
            sizeValues[i].setInterpolator(new AnticipateOvershootInterpolator(0.6f));
            sizeValues[i].setRepeatCount(-1);
        }
        sizeValues[0].addUpdateListener(animation -> invalidate());

    }

    /**
     * 是否开启色彩加强
     *
     * @param isOpen 是否开启
     */
    public void setColorEnhance(boolean isOpen) {
        if (isOpen) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(2.5f);
            ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
            gradientPaint.setColorFilter(colorMatrixColorFilter);
        } else {
            gradientPaint.setColorFilter(null);
        }
    }

    /**
     * 设置动画时长
     *
     * @param time 单位ms
     */
    public void setDuration(long time) {
        this.flowTime = time;
        resetTime();
    }

    /**
     * 设置被取色的位图
     *
     * @param bitmap 位图
     */
    public void setBitmap(Bitmap bitmap) {
        Palette.from(bitmap).generate(palette -> {
            // 提取颜色
            assert palette != null;
            int lightColor1 = palette.getLightVibrantSwatch() != null ? palette.getLightVibrantSwatch().getRgb() : Color.GRAY;
            int lightColor2 = palette.getLightMutedSwatch() != null ? palette.getLightMutedSwatch().getRgb() : lightColor1;
            int vibrantColor = palette.getVibrantSwatch() != null ? palette.getVibrantSwatch().getRgb() : lightColor2;
            int mutedColor = palette.getMutedSwatch() != null ? palette.getMutedSwatch().getRgb() : vibrantColor;
            int darkColor = palette.getDarkVibrantSwatch() != null ? palette.getDarkVibrantSwatch().getRgb() : mutedColor;
            backgroundColor = palette.getDarkMutedSwatch() != null ? palette.getDarkMutedSwatch().getRgb() : darkColor;

            flowColor[0] = lightColor1;
            flowColor[1] = vibrantColor;
            flowColor[2] = lightColor2;
            flowColor[3] = mutedColor;
            flowColor[4] = darkColor;
        });

        // 初始化渐变
        for (int i = 0; i < gradients.length; i++) {
            gradients[i] = createSignalColorGradient(flowColor[i], vMax);
        }

        newBackGroundColor = backgroundColor;

        startAnimator();
    }

    /**
     * 控件用完记得release
     */
    public void release() {
        sizeValues[0].removeAllUpdateListeners();
        for (ValueAnimator[] positionValue : positionValues) {
            for (ValueAnimator valueAnimator : positionValue) {
                valueAnimator.cancel();
            }
        }
        for (ValueAnimator sizeValue : sizeValues) {
            sizeValue.cancel();
        }
    }

    private static RadialGradient createSignalColorGradient(int color, float radius) {
        return new RadialGradient(radius / 2, radius / 2, radius / 2, color | 0xFF000000, Color.TRANSPARENT, Shader.TileMode.MIRROR);
    }

    private synchronized void resetTime() {
        if (sizeValues[0] == null) return;
        if (sizeValues[0].isStarted()) pauseAnimator();
        for (ValueAnimator[] positionValue : positionValues) {
            for (ValueAnimator valueAnimator : positionValue) {
                valueAnimator.setDuration(flowTime);
            }
        }
        for (ValueAnimator sizeValue : sizeValues) {
            sizeValue.setDuration(flowTime);
        }
        startAnimator();
    }

    private synchronized void pauseAnimator() {
        if (sizeValues[0] == null) return;
        if (!sizeValues[0].isStarted()) return;
        for (ValueAnimator[] positionValue : positionValues) {
            for (ValueAnimator valueAnimator : positionValue) {
                valueAnimator.pause();
            }
        }
        for (ValueAnimator sizeValue : sizeValues) {
            sizeValue.pause();
        }
    }

    private synchronized void startAnimator() {
        if (sizeValues[0] == null) return;
        if (sizeValues[0].isStarted()) return;
        for (ValueAnimator[] positionValue : positionValues) {
            for (ValueAnimator valueAnimator : positionValue) {
                valueAnimator.start();
            }
        }
        for (ValueAnimator sizeValue : sizeValues) {
            sizeValue.start();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        vMax = (float) Math.round(Math.max(width, height));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sizeValues[0] == null) return;
        canvas.drawColor(newBackGroundColor);

        for (int i = 0; i < sizeValues.length; i++) {
            gradientPaint.setShader(gradients[i]);
            float scaleValue = (float) sizeValues[i].getAnimatedValue();
            float xValue = (float) positionValues[i][0].getAnimatedValue();
            float yValue = (float) positionValues[i][1].getAnimatedValue();
            int save = canvas.save();
            canvas.translate(vMax * xValue, vMax * yValue);
            canvas.scale(scaleValue, scaleValue);
            canvas.drawPaint(gradientPaint);
            canvas.restoreToCount(save);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE)
            startAnimator();
        else
            pauseAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        pauseAnimator();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    private static class Constants {
        public static final float[][] sizePath = {
                {1.3f, 1.0f, 0.8f, 0.9f, 1.3f},
                {0.8f, 0.9f, 1.1f, 0.9f, 0.8f},
                {0.9f, 1.0f, 0.8f, 1.0f, 0.9f},
                {1.1f, 0.9f, 0.6f, 0.9f, 1.1f},
                {0.9f, 0.6f, 0.8f, 0.7f, 0.9f},
        };
        public static final float[][][] positionPath = {
                {{-0.8f, -0.6f, -0.5f, -0.5f, -0.8f}, {-0.8f, -0.9f, -0.7f, -0.4f, -0.8f}},
                {{0.6f, 0.5f, 0.4f, 0.5f, 0.6f}, {-0.3f, -0.4f, -0.3f, -0.3f, -0.3f}},
                {{0.1f, 0.0f, 0.1f, 0.2f, 0.1f}, {0.1f, -0.2f, 0.0f, 0.0f, 0.1f}},
                {{-0.3f, -0.4f, 0.2f, -0.1f, -0.3f}, {-0.1f, -0.2f, 0.1f, 0.1f, -0.1f}},
                {{0.5f, 0.4f, 0.3f, 0.4f, 0.5f}, {0.5f, 0.6f, 0.7f, 0.6f, 0.5f}},
        };
    }
}

