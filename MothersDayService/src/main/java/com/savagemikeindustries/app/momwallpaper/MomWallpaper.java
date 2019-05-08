package com.savagemikeindustries.app.momwallpaper;

import android.content.res.Resources;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Calendar;

public class MomWallpaper extends WallpaperService {

    enum Filter{
        MORNING(Color.parseColor("#20ffd700"), Color.YELLOW),
        NOON(Color.parseColor("#00000000"), Color.parseColor("#551A8B")),
        EVENING(Color.parseColor("#4000008b"), Color.parseColor("#5a1A8B"));
        int filterColor;
        int lineColor;
        Filter(int color, int line){
            this.filterColor = color;
            lineColor = line;
        }
    }

    public static final String TAG = "MomWallpaper";

    @Override
    public Engine onCreateEngine() {
        return new MomWallpaperEngine();
    }

    class MomWallpaperEngine extends Engine {

        private static final int SIZE_OF_ICON = 200;
        private static final int SIZE_OF_LINES = 200;
        private static final int TIC_SPEED = 500;

        private final Handler mHandler = new Handler();
        private final Runnable mDrawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private Drawable mMothersDay;
        private Drawable mBackgroundImage;
        Paint mLinePaint = new Paint();
        private Calendar mCalendar;
        float mRotation = 0f;
        boolean mVisibility = false;

        MomWallpaperEngine() {
            Resources res = getResources();
            mBackgroundImage = res.getDrawable(R.drawable.road_wallpaper, null);
            mMothersDay = res.getDrawable(R.drawable.happy_mothers_day, null);
            mLinePaint.setStrokeWidth(15f);
            mCalendar = Calendar.getInstance();
            draw();
        }

        Filter filter;
        int mWidth, mHeight;
        Rect mIconPlacement;
        Rect mScreenSize;

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mWidth = width;
            mHeight = height;
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            if (hour > 6 && hour < 10) {
                filter = Filter.MORNING;
            } else if (hour > 18 || hour < 6) {
                filter = Filter.EVENING;
            } else {
                filter = Filter.NOON;
            }
            mLinePaint.setColor(filter.lineColor);
            mScreenSize = new Rect(0, 0, width, height);
            mBackgroundImage.setBounds(mScreenSize);

            if (mBackgroundImage != null) {
                mIconPlacement = new Rect();
                int centerX = mWidth/2;
                int centerY = mHeight/2;
                int halfSize = SIZE_OF_ICON;
                mIconPlacement.set(centerX-halfSize,
                        centerY-halfSize,
                        centerX+halfSize,
                        centerY+halfSize);
                mIconPlacement.offset(0, -300);
                mMothersDay.setBounds(mIconPlacement);
            }
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mHandler.removeCallbacks(mDrawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.mVisibility = visible;
            if (visible) {
                mHandler.post(mDrawRunner);
            } else {
                mHandler.removeCallbacks(mDrawRunner);
            }
        }

        private void draw() {
            SurfaceHolder h = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = h.lockCanvas();
                if (null != canvas) {

                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);

                    mBackgroundImage.draw(canvas);
                    mMothersDay.draw(canvas);

                    canvas.drawColor(filter.filterColor);
                    canvas.rotate(mRotation, mIconPlacement.centerX(), mIconPlacement.centerY());
                    drawLines(canvas);
                    canvas.rotate(45f, mIconPlacement.centerX(), mIconPlacement.centerY());
                    drawLines(canvas);
                    mRotation =(mRotation + 7.5f)%360;
                }
            } finally {
                if (null != canvas) h.unlockCanvasAndPost(canvas);
            }
            mHandler.removeCallbacks(mDrawRunner);
            if (mVisibility) {
                mHandler.postDelayed(mDrawRunner, TIC_SPEED);
            }
        }


        private void drawLines(Canvas canvas) {
            //left line
            canvas.drawLine(mIconPlacement.left,
                    mIconPlacement.centerY(),
                    mIconPlacement.left-SIZE_OF_LINES,
                    mIconPlacement.centerY(),
                    mLinePaint);

            //right line
            canvas.drawLine(mIconPlacement.right,
                    mIconPlacement.centerY(),
                    mIconPlacement.right + SIZE_OF_LINES,
                    mIconPlacement.centerY(),
                    mLinePaint);

            //up line
            canvas.drawLine(mIconPlacement.centerX(),
                    mIconPlacement.top,
                    mIconPlacement.centerX(),
                    mIconPlacement.top-SIZE_OF_LINES,
                    mLinePaint);

            //down line
            canvas.drawLine(mIconPlacement.centerX(),
                    mIconPlacement.bottom,
                    mIconPlacement.centerX(),
                    mIconPlacement.bottom + SIZE_OF_LINES,
                    mLinePaint);
        }
    }
}
