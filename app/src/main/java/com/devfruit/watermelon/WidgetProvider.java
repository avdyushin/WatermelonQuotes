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

    private static BroadcastReceiver receiver;

    private void register(Context context, String action) {
        if (receiver == null) {
            Log.d("WQ", "Will register to " + action);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("WQ", "New screen on");
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
        Log.d("WQ", "New enabled");
        start(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WQ", "New update");
        start(context);
    }

    @Override
	public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("WQ", "New disabled");
        stop(context);
	}

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d("WQ", "New options");
        start(context);
    }
}
