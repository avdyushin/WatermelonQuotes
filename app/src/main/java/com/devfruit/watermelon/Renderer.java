package com.devfruit.watermelon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

class Renderer {

    private final static String TAG = Renderer.class.getName();

    static Bitmap renderQuote(Appearance appearance, String text, String source) {

        int w = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, appearance.width, appearance.metrics
        );

        int h = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, appearance.height, appearance.metrics
        );

        int minTextSize = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 11, appearance.metrics
        );

        int padding = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, appearance.metrics
        );

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Fill background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(appearance.background);
        canvas.drawRect(0, 0, w, h, paint);

        // Prepare text settings
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setColor(appearance.foreground);
        paint.setAlpha(250);
        paint.setTextSize(minTextSize);
        paint.setTextAlign(Paint.Align.LEFT);

        if( appearance.font != null ) {
            paint.setTypeface(appearance.font);
        } else {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        }

        // Quote text
        StaticLayout layout;
        do  {
            paint.setTextSize(minTextSize++);
            layout = new StaticLayout(
                    text, new TextPaint(paint), w - padding * 2,
                    Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false
            );
        } while (layout.getHeight() < h / 1.75);

        canvas.save();
        canvas.translate(padding, padding);
        layout.draw(canvas);
        canvas.restore();

        // Quote source

        paint.setTextSize(h/6);
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        paint.setAlpha(150);
        layout = new StaticLayout(
                source, new TextPaint(paint), w - padding * 2,
                Layout.Alignment.ALIGN_CENTER, 0.85f, -0.3f, false
        );
        canvas.save();
        canvas.translate(padding, h - layout.getHeight() - padding);
        layout.draw(canvas);
        canvas.restore();

        // Top and bottom lines
        paint.setColor(appearance.foreground);
        paint.setAlpha(150);
        canvas.drawLine(w/5, 4, w-w/5, 4, paint);
        canvas.drawLine(w/5, h-4, w-w/5, h-4, paint);

        return bitmap;
    }

    static Bitmap renderQuote_(Appearance appearance, String text) {

        Bitmap bitmap = Bitmap.createBitmap(
                (int)(appearance.width * appearance.density),
                (int)(appearance.height * appearance.density),
                Bitmap.Config.ARGB_4444
        );
        bitmap.setDensity(appearance.densityDpi);

        Canvas canvas = new Canvas(bitmap);
        canvas.setDensity(appearance.densityDpi);

        Log.d(TAG,"Size " + appearance.width + "x" + appearance.height + " d=" + appearance.density + " dpi=" + appearance.densityDpi);

        Paint paint = new Paint();

        int srcTextSize = (int)(11 * appearance.density + 0.5f);
        int textSize = srcTextSize;

        // Setup default paint style
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(appearance.foreground);
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

        if( appearance.font != null ) {
            paint.setTypeface(appearance.font);
        } else {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        }
        paint.setAlpha(250);
        StaticLayout layout = new StaticLayout(quote, new TextPaint(paint), appearance.width, Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);

        // Update font size
        canvas.save();
        textSize = srcTextSize;
        while ( (layout.getHeight()) < appearance.height/2.2 ) {
            paint.setTextSize(textSize++);
            layout = new StaticLayout(quote, new TextPaint(paint), appearance.width, Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);
        }

        paint.setTextSize(textSize - 2);
        layout = new StaticLayout(quote, new TextPaint(paint), appearance.width, Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);

        float delta;

        if( appearance.width/appearance.height > 3 ) {
            // slim
            delta = 10;
        } else {
            delta = 30;
        }

        float dy = (appearance.height - layout.getHeight()) / 2 - delta - 5;

        // Debug only:
        paint.setColor(appearance.background);
        canvas.drawRect(0, 0, appearance.width, appearance.height, paint);

        paint.setColor(appearance.foreground);
        paint.setAlpha(100);

        canvas.translate(0, dy); //position the text
        layout.draw(canvas);

        // Source
        canvas.restore();
        canvas.save();
        paint.setTextSize(appearance.height/6);
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        paint.setAlpha(150);
        layout = new StaticLayout(source, new TextPaint(paint), appearance.width, Layout.Alignment.ALIGN_CENTER, 0.85f, -0.3f, false);
        canvas.translate(0, appearance.height - appearance.height/3 + appearance.height/20);
        layout.draw(canvas);

        canvas.restore();
        canvas.drawLine(appearance.width/5, 4, appearance.width-appearance.width/5, 4, paint);
        canvas.drawLine(appearance.width/5, appearance.height-4, appearance.width-appearance.width/5, appearance.height-4, paint);

        return bitmap;
    }
}
