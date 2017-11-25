package com.devfruit.watermelon;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class WidgetProvider extends AppWidgetProvider {

    private final static String TAG = WidgetProvider.class.getName();

    private static BroadcastReceiver receiver;

    private void register(Context context, String action) {
        if (receiver == null) {
            Log.d(TAG, "Will register to " + action);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "New screen on");
                    context.startService(new Intent(context, UpdateService.class));
                }
            };
            context.getApplicationContext().registerReceiver(
                    receiver, new IntentFilter(action)
            );
        }
    }

    private void unregister(Context context) {
        if (receiver != null) {
            context.getApplicationContext().unregisterReceiver(receiver);
        }
    }

    private void start(Context context) {
        context.startService(new Intent(context, UpdateService.class));
        register(context, Intent.ACTION_SCREEN_ON);
    }

    private  void stop(Context context) {
        context.stopService(new Intent(context, UpdateService.class));
        unregister(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "New enabled");
//        start(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG, "New update");
        start(context);
    }

    @Override
	public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "New disabled");
        stop(context);
	}

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(TAG, "New options");
        start(context);
    }
}
