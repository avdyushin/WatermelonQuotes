package com.devfruit.watermelon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "Quotes";
//    public static final String DOTPATH = "watermelon";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_layout);

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                cancel();
            }

        });

        findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                save();
            }

        });

//        PreferenceCategory externalCategory = (PreferenceCategory)findPreference("external_category");
//        PreferenceScreen exthelp = (PreferenceScreen)findPreference("external_help");


//        String state = Environment.getExternalStorageState();
//
//        if( Environment.MEDIA_MOUNTED.equals(state) ||
//            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {
//            //
//            String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            String wqpath = sdpath + File.separator + DOTPATH;
//            File appdir = new File(wqpath);
//
//            int total = 0;
//            if( appdir.exists() && appdir.isDirectory() ) {
//                File[] files = appdir.listFiles();
//
//                if( files.length == 0 ) {
//                    exthelp.setEnabled(false);
//                    exthelp.setTitle(getString(R.string.no_quotes_installed));
//                    exthelp.setSummary(getString(R.string.put_quotes, wqpath));
//                } else {
//                    // Update new checkboxes
//                    for( File file : files ) {
//                        if( file.canRead() ) {
//                            //file.
//                            try {
//                                BufferedReader in = new BufferedReader(new FileReader(file));
//                                String title = in.readLine();
//                                String description = in.readLine();
//
//                                if( !title.trim().equals("") && !description.trim().equals("") ) {
//
//                                    String key = file.getName();
//                                    CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
//                                    checkBoxPreference.setChecked(false);
//                                    checkBoxPreference.setEnabled(true);
//                                    checkBoxPreference.setTitle(title);
//                                    checkBoxPreference.setKey(key);
//                                    checkBoxPreference.setSummary(description);
//                                    externalCategory.addPreference(checkBoxPreference);
//                                    //
//                                    sources.add(key);
//                                    total++;
//                                }
//                            } catch (FileNotFoundException e) {
//                                // TODO Auto-generated catch block
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                            }
//                        }
//                    }
//
//                    if( total > 0 ) {
//                        exthelp.setEnabled(false);
//                        exthelp.setTitle(getString(R.string.found_quotes, total));
//                        exthelp.setSummary(getString(R.string.put_quotes, wqpath));
//                    } else {
//                        exthelp.setEnabled(false);
//                        exthelp.setTitle(getString(R.string.no_quotes_installed));
//                        exthelp.setSummary(getString(R.string.put_quotes, wqpath));
//                    }
//
//                }
//            } else {
//
//                String filekey = "MarkTwain.txt";
//                if( appdir.mkdir() ) {
//
//                    File template = new File(appdir, filekey);
//                    try {
//                        OutputStream o = new FileOutputStream(template);
//                        InputStream i = getApplicationContext().getResources().openRawResource(R.raw.src_mark_twain);
//                        int r;
//                        byte []b = new byte[1024];
//                        while( (r = i.read(b)) >= 0 ) {
//                            o.write(b, 0, r);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                exthelp.setEnabled(false);
//                exthelp.setTitle(getString(R.string.installed_quotes));
//                exthelp.setSummary(getString(R.string.put_quotes, wqpath));
//
//                CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
//                checkBoxPreference.setChecked(false);
//                checkBoxPreference.setEnabled(true);
//                checkBoxPreference.setKey(filekey);
//                checkBoxPreference.setTitle("Mark Twain");
//                checkBoxPreference.setSummary("Mark Twain Quotes");
//                externalCategory.addPreference(checkBoxPreference);
//                sources.add(filekey);
//            }
//
//
//        } else {
//            exthelp.setEnabled(false);
//            exthelp.setTitle(getString(R.string.no_external_storage));
//            exthelp.setSummary(getString(R.string.requires_external));
//        }
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }
      
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancel();
    }

    private void save() {

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "Invalid widget id!");
            cancel();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences settings = UserSettings.shared(this);
        SharedPreferences.Editor editor = settings.edit();

        int backgroundColor = preferences.getInt("background", 0x80000000);
        int foregroundColor = preferences.getInt("foreground", 0xEEFFFFFF);

        List<String> buildInSources = Arrays.asList("src_bible_en", "src_bible_ru", "src_bible_cn");
        List<String> biterSource = Arrays.asList(getResources().getStringArray(R.array.biter_values));
        List<String> allSources = new ArrayList<>();
        allSources.addAll(buildInSources);
        allSources.addAll(biterSource);

        Set<String> selected = preferences.getStringSet("biter_quotes_key", null);

        Log.d(TAG,"All sources: " + allSources.toString());

        boolean hasSources = false;
        int i = 0;
        for (String key: allSources) {
            if(preferences.getBoolean(key, false) || selected.contains(key)) {
                String config_key = appWidgetId + "_use_source_" + (i++);
                editor.putString(config_key, key);
                hasSources = true;
            }
        }
        editor.putInt(appWidgetId + "_total_source_total", i);
        editor.putInt(appWidgetId + "_background", backgroundColor);
        editor.putInt(appWidgetId + "_foreground", foregroundColor);
        editor.apply();

        Log.d(TAG,"Selected sources count: " + i);

        if (hasSources) {
            UpdateService.updateAppWidget(SettingsActivity.this, appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        } else {
            showWarning();
        }
    }

    private void cancel() {
        Intent i = new Intent();
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private void showWarning() {
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button
        ad.setTitle(getString(R.string.select_quotes_title));
        ad.setMessage(getString(R.string.select_quotes));
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }
}
