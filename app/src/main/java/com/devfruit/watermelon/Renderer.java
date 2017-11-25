package com.devfruit.watermelon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;

class Renderer {

    static Bitmap renderQuote(Appearance appearance, String text, String source) {

        int w = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, appearance.width, appearance.metrics
        );

        int h = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, appearance.height, appearance.metrics
        );

        float minTextSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, appearance.metrics
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

        if( appearance.font != null ) {
            paint.setTypeface(appearance.font);
        } else {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        }

        float maxHeight = (h - padding * 2) * 0.8f;

        // Quote text
        StaticLayout layout;
        do  {
            paint.setTextSize(minTextSize += 0.25f);
            layout = new StaticLayout(
                    text, new TextPaint(paint), w - padding * 2,
                    Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false
            );
        } while (layout.getHeight() < maxHeight);

        paint.setTextSize(minTextSize - 0.5f);
        layout = new StaticLayout(
                text, new TextPaint(paint), w - padding * 2,
                Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false
        );

        canvas.save();
        canvas.translate(padding, padding);
        layout.draw(canvas);
        canvas.restore();

        int textHeight = layout.getHeight();

        // Quote source

        paint.setColor(appearance.foreground);
        paint.setTextSize(h/6);
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        paint.setAlpha(150);
        layout = new StaticLayout(
                source, new TextPaint(paint), w - padding * 2,
                Layout.Alignment.ALIGN_CENTER, 0.85f, -0.3f, false
        );

        float dy = (h - padding - textHeight - layout.getHeight() - padding) / 2.0f;

        canvas.save();
        canvas.translate(padding, textHeight + padding + dy);
        layout.draw(canvas);
        canvas.restore();

        // Top and bottom lines
//        paint.setColor(appearance.foreground);
//        paint.setAlpha(150);
//        canvas.drawLine(w/5, 4, w-w/5, 4, paint);
//        canvas.drawLine(w/5, h-4, w-w/5, h-4, paint);

        return bitmap;
    }
}
