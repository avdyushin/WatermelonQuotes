package com.devfruit.watermelon;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
//import com.devfruit.watermelon.R;

public class wqColorPickerDialog extends Dialog {

    public interface OnColorChangedListener {
        void colorChanged(int color, String key);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;
	private static String mKey;

    private static class ColorPickerView extends View {
        private Paint mPaint;
        private Paint mCenterPaint;
        private final int[] mColors;
        private OnColorChangedListener mListener;
        private static float mScale;
        
        private static int CENTER_X = 100;
        private static int CENTER_Y = 100;
        private static int CENTER_RADIUS = 32;
        private static float oval_r;
        private static RectF oval_rect;
        
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            
            //
            mScale = c.getResources().getDisplayMetrics().density;
            CENTER_X = (int)(100 * mScale + 0.5f);
            CENTER_Y = (int)(100 * mScale + 0.5f);
            CENTER_RADIUS = (int)(32 * mScale + 0.5f);
            
            mListener = l;
            mColors = new int[] {
                0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFFFF, 0xFF000000,
                0x00000000, 0xFFFFFFFF, 0x00000000, 0xFFFF0000
                //0xFFFFFF00, 0xFFFF0000
            };
            Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
            
            oval_r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
            oval_rect = new RectF(-oval_r, -oval_r, oval_r, oval_r);
        }

        private boolean mTrackingCenter;
        private boolean mHighlightCenter;

        @Override
        protected void onDraw(Canvas canvas) { 

            canvas.translate(CENTER_X, CENTER_X);
 
            canvas.drawOval(oval_rect, mPaint);
            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

            if (mTrackingCenter) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);

                if (mHighlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else {
                    mCenterPaint.setAlpha(0x80);
                }
                canvas.drawCircle(0, 0,
                                  CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                                  mCenterPaint);

                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
        }


        

//        private int floatToByte(float x) {
//            int n = java.lang.Math.round(x);
//            return n;
//        }
//        private int pinToByte(int n) {
//            if (n < 0) {
//                n = 0;
//            } else if (n > 255) {
//                n = 255;
//            }
//            return n;
//        }

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

//        private int rotateColor(int color, float rad) {
//            float deg = rad * 180 / 3.1415927f;
//            int r = Color.red(color);
//            int g = Color.green(color);
//            int b = Color.blue(color);
//
//            ColorMatrix cm = new ColorMatrix();
//            ColorMatrix tmp = new ColorMatrix();
//
//            cm.setRGB2YUV();
//            tmp.setRotate(0, deg);
//            cm.postConcat(tmp);
//            tmp.setYUV2RGB();
//            cm.postConcat(tmp);
//
//            final float[] a = cm.getArray();
//
//            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
//            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
//            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
//
//            return Color.argb(Color.alpha(color), pinToByte(ir),
//                              pinToByte(ig), pinToByte(ib));
//        }

        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            boolean inCenter = android.util.FloatMath.sqrt(x*x + y*y) <= CENTER_RADIUS;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        mCenterPaint.setColor(interpColor(mColors, unit));
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterPaint.getColor(), mKey);
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }

    public wqColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int initialColor, String key) {
        super(context);
        
        mListener = listener;
        mInitialColor = initialColor;
        mKey = key;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color, String key) {
                mListener.colorChanged(color, mKey);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle("Pick a Color");
    }
}