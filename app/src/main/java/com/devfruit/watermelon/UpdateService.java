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
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.IBinder;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.RemoteViews;
import com.devfruit.watermelon.R;

public class UpdateService extends Service {
	
	private static final String LOG = "com.devfruit.wq";
	private static final String PREFS = "watermelonQuotes";	
	
	private static int[] mAllWidgetsIds;
	
	private static Typeface font = null;

	private static int bg, fg;
	
	static public Bitmap renderQuote(float scale, float w, float h, String text) {

	    Bitmap bitmap = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.ARGB_4444);
	    Canvas canvas = new Canvas(bitmap);	    
	    Paint paint = new Paint();
	    
	    int srcTextSize = (int)(11 * scale + 0.5f);
	    int textSize = srcTextSize;

	    // Setup default paint style
	    paint.setAntiAlias(true);
	    paint.setSubpixelText(true);
	    paint.setStyle(Paint.Style.FILL);
	    paint.setColor(fg);
	    paint.setTextSize(textSize);
	    paint.setTextAlign(Align.LEFT);

	    // Split string into quote and source of quote
	    String[] quoteAndSource = text.split("\\\\");
	    String quote = text;
	    String source = "";
	    
	    if( quoteAndSource.length >= 2 ) {
	    	quote = "\"" + quoteAndSource[0].trim() + "\"";
	    	source = " ~ " + quoteAndSource[quoteAndSource.length - 1];
	    }
	    
	    // Two text paints, one for quote, second to source
	    
	    if( font != null ) {
	    	paint.setTypeface(font);
	    } else {
	    	paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
	    }
	    paint.setAlpha(250);
	    int padding = (int)(10 * scale + 0.5f);
	    StaticLayout layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);
	    

	    // Update font size
	    canvas.save();
	    textSize = srcTextSize;
	    while ( (layout.getHeight()) < h/2.2 ) {
		    paint.setTextSize(textSize++);
		    layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);
	    }

	    paint.setTextSize(textSize - 2);	   	    
	    layout = new StaticLayout(quote, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.73f, -0.4f, false);
	    
	    float delta;
	    
	    if( w/h > 3 ) {
	    	// slim
	    	delta = 10;
	    } else {
	    	delta = 30;
	    }
	    
	    float dy = (h - layout.getHeight()) / 2 - delta - 5;
	    
	    // Debug only:
	    paint.setColor(bg);
	    canvas.drawRect(0, 0, w, h, paint);
	    
	    paint.setColor(fg);
	    paint.setAlpha(100);

	    canvas.translate(0, dy); //position the text
	    layout.draw(canvas);
	    
	    // Source
	    canvas.restore();
	    canvas.save();
	    dy += layout.getHeight();
	    paint.setTextSize(h/6);
	    paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
	    paint.setAlpha(150);
	    layout = new StaticLayout(source, new TextPaint(paint), (int)(w), Layout.Alignment.ALIGN_CENTER, 0.85f, -0.3f, false);
	    float dx = w - paint.measureText(source) - padding;
	    canvas.translate(0, h - h/3 + h/20);
	    layout.draw(canvas);
	    
	    canvas.restore();
	    canvas.drawLine(w/5, 4, w-w/5, 4, paint);
	    canvas.drawLine(w/5, h-4, w-w/5, h-4, paint);
	    
	    return bitmap;

    }	

	@Override
	public void onCreate() {

		super.onCreate();

	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

		if( appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID ) {
			
			// Update only this widget
			updateAppWidget(this.getApplicationContext(), appWidgetManager, appWidgetId);
			
		} else {

			ComponentName thisWidget = new ComponentName(getApplicationContext(), WidgetProviderSmall.class);
			mAllWidgetsIds = appWidgetManager.getAppWidgetIds(thisWidget);

			for (int widgetId : mAllWidgetsIds) {
				updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId);
			}

		}
		
		stopSelf();
		super.onStart(intent, startId);

	}

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	
		CustomRemoteViews remoteViews = new CustomRemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
		
		bg = settings.getInt(appWidgetId + "_background", 0x80000000);
		fg = settings.getInt(appWidgetId + "_foreground", 0xFFFFFFFF);
		
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
		
		InputStream inputStream = null;
		
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
				inputStream = new FileInputStream(sdpath + File.separator + wqPreference.DOTPATH + File.separator + source);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
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
		
		font = Typeface.createFromAsset(context.getAssets(), "fonts/OldStandard-Regular.otf");
		remoteViews.setImageViewBitmap(R.id.update_bitmap, renderQuote(s, w, h, quote));
		remoteViews.apply(context, null);
		
		Intent clickIntent = new Intent(context, UpdateService.class);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);		
		remoteViews.setOnClickPendingIntent(R.id.update_bitmap, pendingIntent);
		
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);    	
    
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
