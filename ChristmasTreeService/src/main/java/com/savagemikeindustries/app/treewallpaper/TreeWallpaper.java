package com.savagemikeindustries.app.treewallpaper;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;

public class TreeWallpaper extends WallpaperService {

    enum Filter {
        MORNING(Color.parseColor("#20ffd700")),
        NOON(Color.parseColor("#00000000")),
        EVENING(Color.parseColor("#4000008b"));
        int filterColor;
        Filter(int color){
            this.filterColor = color;
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new TreeWallpaperEngine();
    }

    class TreeWallpaperEngine extends Engine {

        private static final int SIZE_OF_TREE = 200;
        private static final int TIC_SPEED = 700;

        private final Handler mHandler = new Handler();
        private final Runnable mDrawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private final Drawable mBackgroundImage;
        private final Calendar mCalendar;
        boolean mVisibility = false;

        TreeWallpaperEngine() {
            Resources res = getResources();
            mBackgroundImage = res.getDrawable(R.drawable.tree, null);
            mCalendar = Calendar.getInstance();
            draw();
        }

        Filter filter;
        int mWidth, mHeight;
        Rect mTreePlacement;
        Rect mScreenSize;
        int light1 = Color.RED, light2 = Color.BLUE, light3 = Color.YELLOW;

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
            mScreenSize = new Rect(0, 0, width, height);
            mBackgroundImage.setBounds(mScreenSize);

            if (mBackgroundImage != null) {
                mTreePlacement = new Rect();
                int centerX = mWidth/2;
                int centerY = mHeight/2;
                int halfSize = SIZE_OF_TREE;
                mTreePlacement.set((int) (centerX-halfSize*1.8),
                        (int) (centerY-halfSize*2.5),
                        (int) (centerX+halfSize*1.8),
                        (int) (centerY+halfSize*2.5));
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

                    Paint a = new Paint();
                    Paint b = new Paint();
                    Paint c = new Paint();
                    a.setColor(light1);
                    b.setColor(light2);
                    c.setColor(light3);

                    mBackgroundImage.draw(canvas);

                    canvas.drawColor(filter.filterColor);

                    drawOrnaments(canvas, a, b, c);

                    int temp = light1;
                    light1 = light2;
                    light2 = light3;
                    light3 = temp;
                }
            } finally {
                if (null != canvas) {
                    h.unlockCanvasAndPost(canvas);
                }
            }
            mHandler.removeCallbacks(mDrawRunner);
            if (mVisibility) {
                mHandler.postDelayed(mDrawRunner, TIC_SPEED);
            }
        }


        private void drawOrnaments(Canvas canvas, Paint p1, Paint p2, Paint p3) {
            int rows = 6;
            int cols = 5;
            float xSpacing = mTreePlacement.width()/cols;
            float ySpacing = mTreePlacement.height()/rows;
            int r = 12;
            for (int upwards = 0; upwards < rows; upwards++) {
                int narrowingFactor = upwards * 3 * cols;

                for (int across = 0; across <= cols; across++) {

                    int raisingFactor = across * 5 * rows;
                    float ornamentX = mTreePlacement.right - ((xSpacing - narrowingFactor) * across + (upwards * 28));
                    float ornamentY = mTreePlacement.bottom - (ySpacing * upwards + raisingFactor);

                    switch ((across + upwards) % 3){
                        case 0:
                            canvas.drawCircle(ornamentX, ornamentY, r, p1);
                            break;
                        case 1:
                            canvas.drawCircle(ornamentX, ornamentY, r, p2);
                            break;
                        case 2:
                            canvas.drawCircle(ornamentX, ornamentY, r, p3);
                            break;
                    }
                }
            }
        }
    }
}
