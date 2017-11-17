package com.devfruit.watermelon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Created by avdyushin on 17/11/2017.
 */

public class Renderer {

    static public Bitmap renderQuote(float scale,
                                     float w,
                                     float h,
                                     int bg,
                                     int fg,
                                     Typeface font,
                                     String text) {

        Bitmap bitmap = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        int srcTextSize = (int)(11 * scale + 0.5f);
        int textSize = srcTextSize;

        // Setup default paint style
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fg);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);

        // Split string into quote and source of quote
        String[] quoteAndSource = text.split("\\\\");
        String quote = text;
        String source = "";

        if( quoteAndSource.length >= 2 ) {
            quote = "\"" + quoteAndSource[0].trim() + "\"";
            source = " ~ " + quoteAndSource[quoteAndSource.length - 1];
        }

        // Two text paints, one for quote, second to source

        if( font != null ) {
            paint.setTypeface(font);
        } else {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        }
        paint.setAlpha(250);
        int padding = (int)(10 * scale + 0.5f);
        StaticLayout layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);


        // Update font size
        canvas.save();
        textSize = srcTextSize;
        while ( (layout.getHeight()) < h/2.2 ) {
            paint.setTextSize(textSize++);
            layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);
        }

        paint.setTextSize(textSize - 2);
        layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);

        float delta;

        if( w/h > 3 ) {
            // slim
            delta = 10;
        } else {
            delta = 30;
        }

        float dy = (h - layout.getHeight()) / 2 - delta - 5;

        // Debug only:
        paint.setColor(bg);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(fg);
        paint.setAlpha(100);

        canvas.translate(0, dy); //position the text
        layout.draw(canvas);

        // Source
        canvas.restore();
        canvas.save();
        dy += layout.getHeight();
        paint.setTextSize(h/6);
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        paint.setAlpha(150);
        layout = new StaticLayout(source, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.85f, -0.3f, false);
        float dx = w - paint.measureText(source) - padding;
        canvas.translate(0, h - h/3 + h/20);
        layout.draw(canvas);

        canvas.restore();
        canvas.drawLine(w/5, 4, w-w/5, 4, paint);
        canvas.drawLine(w/5, h-4, w-w/5, h-4, paint);

        return bitmap;

    }
}
