package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean erase = false;
    private float penWidth = 20f;
    private float eraserWidth = 50f;
    private RectF dirtyRect = new RectF();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.WHITE);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(penWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setStrokeWidth(float width) {
        if (erase) {
            eraserWidth = width;
        } else {
            penWidth = width;
        }
        drawPaint.setStrokeWidth(width);
    }

    public float getStrokeWidth() {
        return erase ? eraserWidth : penWidth;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                updateDirtyRect(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                updateDirtyRect(touchX, touchY);
                // Draw to canvas in real-time
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                updateDirtyRect(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void updateDirtyRect(float eventX, float eventY) {
        if (dirtyRect.isEmpty()) {
            dirtyRect.set(eventX - 1, eventY - 1, eventX + 1, eventY + 1);
        } else {
            dirtyRect.union(eventX - 1, eventY - 1, eventX + 1, eventY + 1);
        }
    }

    public void clearCanvas() {
        dirtyRect.setEmpty();
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            drawPaint.setStrokeWidth(eraserWidth);
        } else {
            drawPaint.setColor(Color.BLACK);
            drawPaint.setXfermode(null);
            drawPaint.setStrokeWidth(penWidth);
        }
    }

    public Bitmap getCroppedBitmap() {
        if (dirtyRect.isEmpty()) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        
        int padding = 40;
        float left = Math.max(0, dirtyRect.left - padding);
        float top = Math.max(0, dirtyRect.top - padding);
        float right = Math.min(canvasBitmap.getWidth(), dirtyRect.right + padding);
        float bottom = Math.min(canvasBitmap.getHeight(), dirtyRect.bottom + padding);
        
        int width = (int) (right - left);
        int height = (int) (bottom - top);
        
        // Safety check for min size
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        return Bitmap.createBitmap(canvasBitmap, (int) left, (int) top, width, height);
    }
}
