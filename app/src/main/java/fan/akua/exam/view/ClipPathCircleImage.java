package fan.akua.exam.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class ClipPathCircleImage extends AppCompatImageView {
    private int width;
    private int height;
    private final Path path;

    public ClipPathCircleImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipPathCircleImage(Context context) {
        this(context, null);
    }

    public ClipPathCircleImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        path.reset();
        path.addCircle(width, height, width, Path.Direction.CCW);
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w / 2;
        height = h / 2;
    }

}