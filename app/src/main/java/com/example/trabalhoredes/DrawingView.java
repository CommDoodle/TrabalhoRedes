package com.example.trabalhoredes;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.io.ByteArrayOutputStream;

/**
 * Created by Avell B153 on 06/12/2016.
 */

public class DrawingView extends View {
    public String TAG = "DrawingView";

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    public DrawingView (Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
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



    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        Log.d(TAG, "Opa! Estou dentro do onDraw.");
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        Log.d(TAG, "Opa! Estou dentro de onTouchEvent.");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
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

    public void setColor(String newColor){
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public synchronized void atualizaBitmap(Bitmap bip)
    {
        Log.d(TAG, "Opa! Estou dentro de atualizaBitmap.");
        Bitmap bmp2 = bip.copy(bip.getConfig(), true);
        //Canvas canvasBmp2 = new Canvas( bmp2 );
        drawCanvas.drawBitmap(bmp2, 0, 0, null);
        //canvasBitmap = Bitmap.createBitmap(bip.getWidth(), bip.getHeight(), bip.getConfig());//bip;//overlay(bip, canvasBitmap);
        //this.draw(drawCanvas);
        invalidate();
    }

    public synchronized Bitmap getBitmap()
    {
        Log.d(TAG, "Opa! Estou dentro de getBitmap.");
        return this.canvasBitmap;
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
}
