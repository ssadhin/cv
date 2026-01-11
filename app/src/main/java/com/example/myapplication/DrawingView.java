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
    private RectF dirtyRect = new RectF();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(Color.WHITE);
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
                break;
            case MotionEvent.ACTION_UP:
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
        drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        drawCanvas.drawColor(Color.WHITE);
        invalidate();
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    public Bitmap getCroppedBitmap() {
        if (dirtyRect.isEmpty()) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        int padding = 40;
        return Bitmap.createBitmap(canvasBitmap, (int) Math.max(0, dirtyRect.left - padding),
                (int) Math.max(0, dirtyRect.top - padding),
                (int) Math.min(canvasBitmap.getWidth() - dirtyRect.left + padding, dirtyRect.width() + padding * 2),
                (int) Math.min(canvasBitmap.getHeight() - dirtyRect.top + padding, dirtyRect.height() + padding * 2));
    }
}
