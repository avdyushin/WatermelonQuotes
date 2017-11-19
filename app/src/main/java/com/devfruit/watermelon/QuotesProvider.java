package com.devfruit.watermelon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

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

class QuotesProvider {

    String getNextQuote(Context context, int appWidgetId) {
        SharedPreferences settings = UserSettings.shared(context);

        int total = settings.getInt(appWidgetId + "_total_source_total", 0);
        List<String> sources = new ArrayList<>();
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
        switch (source) {
            case "src_bible_en":
                inputStream = context.getResources().openRawResource(R.raw.src_bible_en);
                break;
            case "src_bible_ru":
                inputStream = context.getResources().openRawResource(R.raw.src_bible_ru);
                break;
            case "src_bible_cn":
                inputStream = context.getResources().openRawResource(R.raw.src_bible_cn);
                break;
            case "src_classics_biter":
                inputStream = context.getResources().openRawResource(R.raw.src_classics_biter);
                break;
            default:
                String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
                try {
                    inputStream = new FileInputStream(sdpath + File.separator + SettingsActivity.DOTPATH + File.separator + source);
                } catch (FileNotFoundException e) {
                    inputStream = context.getResources().openRawResource(R.raw.src_bible_en);
                }
                break;
        }

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        List<String> array = new ArrayList<>();
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
            e.printStackTrace();
        }

        // Create some random data
        int number = (new Random().nextInt(array.size()));
        return array.get(number);
    }
}
