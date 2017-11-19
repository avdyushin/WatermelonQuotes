package com.devfruit.watermelon;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class UpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
        // Get widget id to update (used in click action)
        int widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        );
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // If not, update all widgets
            Log.d("WQ", "Update ALL");
            updateAll(manager);
        } else {
            Log.d("WQ", "Update only ONE");
            updateAppWidget(this.getApplicationContext(), manager, widgetID);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("WQ", "New orientation");
        AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
        updateAll(manager);
    }

    void updateAll(AppWidgetManager manager) {
        ComponentName widget = new ComponentName(getApplicationContext(), WidgetProvider.class);
        for (int id : manager.getAppWidgetIds(widget)) {
            updateAppWidget(this.getApplicationContext(), manager, id);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        String quote = new QuotesProvider().getNextQuote(context, appWidgetId);
        SharedPreferences settings = UserSettings.shared(context);

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        float h = appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight;
        float w = appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth;

        Display display = ((WindowManager)context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = display.getRotation();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Log.d("WQ", "w = " + metrics.widthPixels + " h = " + metrics.heightPixels);
//        w = minWidth;
//        h = maxHeight;

        // TODO: Check for tablet and phone
        if (metrics.widthPixels > metrics.heightPixels) {
            // Landscape: maxWidth, minHeight
//            Log.d("WQ","LANDSCAPE");
            if (maxWidth > 0 && minHeight > 0) {
                w = maxWidth;
                h = minHeight;
            }
        } else {
            // Portrait: minWidth, maxHeight
//            Log.d("WQ","PORTRAIT");
            if (minWidth > 0 && maxHeight > 0) {
                w = minWidth;
                h = maxHeight;
            }
        }

        float s = context.getResources().getDisplayMetrics().density;

        int bg = settings.getInt(appWidgetId + "_background", 0x80000000);
        int fg = settings.getInt(appWidgetId + "_foreground", 0xFFFFFFFF);
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/OldStandard-Regular.otf");
		remoteViews.setImageViewBitmap(R.id.update_bitmap, Renderer.renderQuote(s, w, h, bg, fg, font, quote));
		remoteViews.apply(context, null);
		
		Intent clickIntent = new Intent(context, UpdateService.class);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);		
		remoteViews.setOnClickPendingIntent(R.id.update_bitmap, pendingIntent);
		
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}
