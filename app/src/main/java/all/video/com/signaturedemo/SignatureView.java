package all.video.com.signaturedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yangbo on 2017/10/10.
 */

public class SignatureView extends View {

    private Paint mPaint;
    private Path mPath;
    /**
     * Optimizes painting by invalidating the smallest possible area.
     */
    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    private float STROKE_WIDTH = 5f;
    private float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    public SignatureView(Context context) {
        this(context, null);
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAlpha(1);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    public void clearInput() {
        if (mPath != null) {
            mPath.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                lastTouchX = x;
                lastTouchY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                resetDirtyRect(x, y);
                //When the hardware tracks events faster than they are delivered,
                // the event will contain a history of those skipped points.
                int size = event.getHistorySize();
                for (int i = 0; i < size; i++) {
                    float itemX = event.getHistoricalX(i);
                    float itemY = event.getHistoricalY(i);
                    expandDirtyRect(itemX, itemY);
                    mPath.lineTo(itemX, itemY);
                }
                mPath.lineTo(x, y);
                break;
            default:
//                return false;
                return super.onTouchEvent(event);
        }
        // Include half the stroke width to avoid clipping.
        invalidate(
                (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));
        lastTouchX = x;
        lastTouchY = y;
        return true;
    }

    private void resetDirtyRect(float eventX, float eventY) {
        // The lastTouchX and lastTouchY were set when the ACTION_DOWN
        // motion event occurred.
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    private void expandDirtyRect(float historicalX, float historicalY) {

        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }
}
