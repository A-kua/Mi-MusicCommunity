package fan.akua.exam.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class ClipPathCircleImage extends AppCompatImageView {
    private final Path path = new Path();
    private ValueAnimator animator;
    private float rotationAngle = 0f;
    private boolean isAnimating = false;
    private float halfW, halfH;

    public ClipPathCircleImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipPathCircleImage(Context context) {
        this(context, null);
    }

    public ClipPathCircleImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(rotationAngle, halfW, halfH);
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfW = (float) w / 2;
        halfH = (float) h / 2;
        path.reset();
        path.addCircle(halfW, halfH, halfW, Path.Direction.CCW);
    }

    public void startRotation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(9000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            rotationAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
        isAnimating = true;
    }

    public void pauseRotation() {
        if (isAnimating && animator != null) {
            animator.pause();
            isAnimating = false;
        }
    }

    public void resumeRotation() {
        if (!isAnimating && animator != null) {
            animator.resume();
            isAnimating = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator.removeAllListeners();
            animator = null;
        }
        super.onDetachedFromWindow();
    }
}