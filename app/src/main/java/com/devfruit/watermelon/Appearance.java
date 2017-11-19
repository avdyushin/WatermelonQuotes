package com.devfruit.watermelon;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;

class Appearance {

    int width;
    int height;
    float density;
    Typeface font;
    int foreground;
    int background;

    Appearance(Context context, int appWidgetId) {
        SharedPreferences settings = UserSettings.shared(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        Bundle options = manager.getAppWidgetOptions(appWidgetId);

        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        width = manager.getAppWidgetInfo(appWidgetId).minWidth;
        height = manager.getAppWidgetInfo(appWidgetId).minHeight;

        if (metrics.widthPixels > metrics.heightPixels) {
            if (maxWidth > 0 && minHeight > 0) {
                width = maxWidth;
                height = minHeight;
            }
        } else {
            if (minWidth > 0 && maxHeight > 0) {
                width = minWidth;
                height = maxHeight;
            }
        }

        density = context.getResources().getDisplayMetrics().density;
        background = settings.getInt(appWidgetId + "_background", 0x80000000);
        foreground = settings.getInt(appWidgetId + "_foreground", 0xFFFFFFFF);

        font = Typeface.createFromAsset(
                context.getAssets(), "fonts/OldStandard-Regular.otf"
        );
    }
}
