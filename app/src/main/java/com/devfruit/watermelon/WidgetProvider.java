package com.devfruit.watermelon;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class WidgetProvider extends AppWidgetProvider {

	public static final String PREFS = "com.devfruit.waterlemonquotesnew.prefs";
	public static final String PROVIDERS  = "providers";
	
	public static int countProviders = 0;
	
	protected int requestCode = 0;
	
	private PendingIntent pendingIntent = null;
	private AlarmManager alarmManager = null;
	private static boolean screen_receiver = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		super.onReceive(context, intent);

	}
	
	@Override
	public void onEnabled(Context context) {

		super.onEnabled(context);

	};
	
	@Override
	public void onDisabled(Context context) {

		context.stopService(new Intent(context, UpdateService.class));
		super.onDisabled(context);

	}

	private void createAlarm(Context context, Intent intent) {
		
		pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager  = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		final Calendar TIME = Calendar.getInstance();
		TIME.set(Calendar.MINUTE, 0);  
		TIME.set(Calendar.SECOND, 0);  
		TIME.set(Calendar.MILLISECOND, 0);
		
		alarmManager.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), 5000, pendingIntent);
		
	}
	
	static void registerScreenReceiver(Context context) {
		
		if( !screen_receiver ) {
			
			screen_receiver = true;
			
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
	
		        @Override
		        public void onReceive(Context context, Intent intent) {
		        	Intent service = new Intent(context.getApplicationContext(), UpdateService.class);
		        	context.startService(service);
		        }
	
		    }, filter);
			
		}
		
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		Intent intent = new Intent(context.getApplicationContext(), UpdateService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);;
		
		if( countProviders == 1 /* first call */) {
			createAlarm(context, intent);
		}

		registerScreenReceiver(context);
		
		// Update the widgets via the service
		context.startService(intent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
