package com.devfruit.watermelon;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateService extends Service {

    static final String TAG = UpdateService.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        // Get widget id to update (used in click action)
        int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        );
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "Update onStart ONE");
            updateWidgets(new int[] { widgetId });
        } else {
            // If not, update all widgets
            Log.d(TAG, "Update onStart ALL");
            int [] appWidgetIds = getAppWidgetIds(getApplicationContext());
            updateWidgets(appWidgetIds);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Update onConfiguration ALL");
        int [] appWidgetIds = getAppWidgetIds(getApplicationContext());
        updateWidgets(appWidgetIds);
    }

    private static int [] getAppWidgetIds(Context context) {
        ComponentName widget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        return manager.getAppWidgetIds(widget);
    }

    private void updateWidgets(int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateAppWidget(this.getApplicationContext(), widgetId);
        }
    }

    static void updateAppWidget(Context context, int appWidgetId) {
        // Get configuration for widget with given id
        String quote = new QuoteProvider().getNextQuote(context, appWidgetId);

        String [] quoteAndSource = quote.split("\\\\");
        String text = quote;
        String source = "";

        if( quoteAndSource.length >= 2 ) {
            text = "\"" + quoteAndSource[0].trim() + "\"";
            source = "~ " + quoteAndSource[quoteAndSource.length - 1];
        }

        Appearance appearance = new Appearance(context, appWidgetId);
        Bitmap bitmap = Renderer.renderQuote(appearance, text, source);

        // Configure remove views
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setImageViewBitmap(R.id.update_bitmap, bitmap);
		remoteViews.apply(context, null);

		// Configure on click intent for this widget
		Intent clickIntent = new Intent(context, UpdateService.class);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getService(
		        context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
		remoteViews.setOnClickPendingIntent(R.id.update_bitmap, pendingIntent);

		// Update views
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(appWidgetId, remoteViews);
    }
}
