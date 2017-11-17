package com.devfruit.watermelon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.IBinder;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.RemoteViews;

public class UpdateService extends Service {
	
	private static final String PREFS = "watermelonQuotes";
	private static int[] mAllWidgetsIds;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if( appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID ) {

            // Update only this widget
            updateAppWidget(this.getApplicationContext(), appWidgetManager, appWidgetId);

        } else {

            ComponentName thisWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);
            mAllWidgetsIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : mAllWidgetsIds) {
                updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId);
            }

        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
		

		int total = settings.getInt(appWidgetId + "_total_source_total", 0);
		List<String> sources = new ArrayList<String>();
		if( total > 0 ) {
			for( int i = total - 1; i >= 0; --i ) {
				String s = settings.getString(appWidgetId + "_use_source_" + i, "");
				sources.add(s);
			}
		}
		String source = "src_bible_en";
		
		if( sources.size() > 1 ) {
			int r = (new Random().nextInt(sources.size()));
			source = sources.get(r);
		} else if( sources.size() == 1) {
			source = sources.get(0);
		}
		
		InputStream inputStream;
		
		if( source.equals("src_bible_en") ) {
			//
			inputStream = context.getResources().openRawResource(R.raw.src_bible_en);
		} else if ( source.equals("src_bible_ru") ) {
			//
			inputStream = context.getResources().openRawResource(R.raw.src_bible_ru);
		} else if ( source.equals("src_bible_cn") ) {
			//
			inputStream = context.getResources().openRawResource(R.raw.src_bible_cn);
		} else if ( source.equals("src_classics_biter") ) {
			//
			inputStream = context.getResources().openRawResource(R.raw.src_classics_biter);
		} else {
			String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
			try {
				inputStream = new FileInputStream(sdpath + File.separator + SettingsActivity.DOTPATH + File.separator + source);
			} catch (FileNotFoundException e) {
				inputStream = context.getResources().openRawResource(R.raw.src_bible_en);
			}
		}

		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		List<String> array = new ArrayList<String>();
		int skipper = 0;
		try {
			while( (line = buffreader.readLine()) != null ) {
				if( skipper++ > 1 ) {
					if( !line.trim().equals("") ) {
						array.add(line);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		// Create some random data
		int number = (new Random().nextInt(array.size()));
		
		String quote = array.get(number);

		float s = context.getResources().getDisplayMetrics().density;			
		float h = appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight;
		float w = appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth;

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
