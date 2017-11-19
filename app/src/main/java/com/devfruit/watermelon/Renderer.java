package com.devfruit.watermelon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

class Renderer {

    static Bitmap renderQuote(Appearance appearance, String text) {

        Bitmap bitmap = Bitmap.createBitmap(
                appearance.width, appearance.height, Bitmap.Config.ARGB_4444
        );

        Canvas canvas = new Canvas(bitmap);
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
