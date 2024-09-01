package fan.akua.exam.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fan.akua.exam.utils.BlurUtil;

public class BlurDrawable extends Drawable implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener, ViewTreeObserver.OnPreDrawListener {
    // 模糊相关参数 //
    private int alpha;
    private int overlayColor = 0xAAFFFFFF;
    private float radius = 12f;
    private float factor = 4f;

    private View mDecorView;//整个页面窗口
    private View blurView;//需要展示模糊的View

    private int viewW, viewH;//需要展示模糊的View的宽高
    private int scaleW, scaleH;//实际模糊后的位图大小

    private Canvas backgroundCanvas;//未模糊时的背景画布
    private Bitmap backgroundBitmap;//剔除需要展示模糊的View的图片
    private Bitmap blurBitmap;//模糊后的图片

    private Paint paint;
    private final Rect src;
    private final Rect dst;
    private final int[] location = new int[2];

    private final BlurUtil blurUtil;
    private boolean isOptionChange = true;//是否有属性改变

    /**
     * 创建BlurDrawable
     *
     * @param view 要模糊的View
     * @return BlurDrawable
     */
    public static BlurDrawable createBlurDrawable(View view) {
        BlurDrawable blurDrawable = new BlurDrawable(view);
        view.addOnAttachStateChangeListener(blurDrawable);
        view.addOnLayoutChangeListener(blurDrawable);
        blurDrawable.mDecorView = getActivityDecorView(view);
        view.setBackground(blurDrawable);
        return blurDrawable;
    }

    /**
     * 获取DecorView
     *
     * @param view 任意DecorView内的View
     * @return DecorView
     */
    private static View getActivityDecorView(View view) {
        Context ctx = view.getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        if (ctx instanceof Activity) {
            return ((Activity) ctx).getWindow().getDecorView();
        } else {
            return null;
        }
    }

    /**
     * 设置控件可见性
     *
     * @param dstView   被设置的控件
     * @param isVisible 是否可见
     */
    private static void setVisible(View dstView, boolean isVisible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dstView.setTransitionVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        } else {
            dstView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private BlurDrawable(View blurView) {
        this.blurView = blurView;
        blurUtil = new BlurUtil();
        src = new Rect();
        dst = new Rect();
    }


    @Override
    public boolean onPreDraw() {
        if (isOptionChange) {//如果视图/参数发生改变，则重新计算参数
            float downSampleFactor = factor;
            float radius = this.radius / downSampleFactor;
            if (radius > 25) {
                downSampleFactor = downSampleFactor * radius / 25;
                radius = 25;
            }

            scaleW = Math.max(1, (int) (viewW / downSampleFactor));
            scaleH = Math.max(1, (int) (viewH / downSampleFactor));

            backgroundBitmap = Bitmap.createBitmap(scaleW, scaleH, Bitmap.Config.ARGB_8888);
            blurBitmap = Bitmap.createBitmap(scaleW, scaleH, Bitmap.Config.ARGB_8888);
            backgroundCanvas = new Canvas(backgroundBitmap);
            blurView.getLocationInWindow(location);
            blurUtil.prepare(blurView.getContext(), blurBitmap, radius);
            src.right = scaleW;
            src.bottom = scaleH;
            dst.right = viewW;
            dst.bottom = viewH;
            isOptionChange = false;
        }

        setVisible(blurView, false);
        backgroundBitmap.eraseColor(overlayColor & 0xffffff);//添加遮罩
        int rc = backgroundCanvas.save();
        try {
            backgroundCanvas.scale(1.f * scaleW / viewW, 1.f * scaleH / viewH);
            backgroundCanvas.translate(-location[0]-blurView.getTranslationX(), -location[1]-blurView.getTranslationY());
            mDecorView.draw(backgroundCanvas);
        } catch (Exception ignored) {

        } finally {
            setVisible(blurView, true);
            backgroundCanvas.restoreToCount(rc);
        }
        blurUtil.blur(backgroundBitmap, blurBitmap);

        return true;
    }

    /**
     * 设置模糊半径 默认12f
     *
     * @param radius 0-25f
     */
    public void setBlurRadius(float radius) {
        this.radius = radius;
        isOptionChange = true;
    }

    /**
     * 设置缩放因子 默认4f
     *
     * @param factor >0
     */
    public void setDownSampleFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("缩放因子需要大于0");
        }
        this.factor = factor;
        isOptionChange = true;
    }

    /**
     * 设置遮罩颜色
     *
     * @param color argb
     */
    public void setOverlayColor(int color) {
        this.overlayColor = color;
        isOptionChange = true;
    }

    /**
     * 释放资源
     */
    public void release() {
        backgroundBitmap.recycle();
        blurUtil.release();
        blurBitmap.recycle();
        mDecorView = null;
        blurView.removeOnAttachStateChangeListener(this);
        blurView.removeOnLayoutChangeListener(this);
        blurView.getViewTreeObserver().removeOnPreDrawListener(this);
        blurView.setBackgroundDrawable(null);
        blurView = null;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (blurBitmap == null)
            return;
        canvas.drawBitmap(blurBitmap, src, dst, paint);
    }

    /**
     * 设置透明度
     *
     * @param alpha 0-255
     */
    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        if (paint == null)
            paint = new Paint();//懒加载
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (mDecorView != null) {
            v.getViewTreeObserver().addOnPreDrawListener(this);
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mDecorView != null) {
            v.getViewTreeObserver().removeOnPreDrawListener(this);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        viewW = right - left;
        viewH = bottom - top;
        isOptionChange = true;
    }
}
