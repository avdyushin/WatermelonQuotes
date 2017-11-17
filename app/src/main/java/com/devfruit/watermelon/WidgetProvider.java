package com.devfruit.watermelon;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class WidgetProvider extends AppWidgetProvider {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, UpdateService.class));
        }
    };

    private void registerScreenOn(Context context) {
        try {
            context.getApplicationContext().registerReceiver(
                    receiver, new IntentFilter(Intent.ACTION_SCREEN_ON)
            );
        } catch (Exception e) {
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(context, UpdateService.class));
        registerScreenOn(context);
    }

    @Override
	public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, UpdateService.class));
        try {
            context.getApplicationContext().unregisterReceiver(receiver);
        } catch (Exception e) {

        }
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
		context.startService(new Intent(context, UpdateService.class));
		registerScreenOn(context);
	}
}
