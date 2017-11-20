package com.devfruit.watermelon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {

    private static final String TAG = "Quotes";
    public static final String DOTPATH = "watermelon";
    private static final String PREFS = "watermelonQuotes";
    
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    List<String> sources;

    private int backgroundColor = 0x80000000;
    private int foregroundColor = 0xEEFFFFFF;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        setResult(RESULT_CANCELED);
        
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

//        getFragmentManager()
//                .beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();

        setContentView(R.layout.prefs_layout);
        
        
        findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                cancel();
            }
            
        });
        
        
        findViewById(R.id.okButton).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                save();
            }
            
        });
        
        PreferenceCategory externalCategory = (PreferenceCategory)findPreference("external_category");
        PreferenceScreen exthelp = (PreferenceScreen)findPreference("external_help");

        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        final SharedPreferences.Editor editor = settings.edit();

        ((ColorPickerPreference)findPreference("background")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            //@Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                backgroundColor = Integer.valueOf(String.valueOf(newValue));
                preference.setSummary(
                        getString(
                                R.string.pref_background_sum, 
                                ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)))
                                )
                );
                editor.putInt("background", backgroundColor);
                editor.apply();
                return true;
            }

        });
        
        ((ColorPickerPreference)findPreference("foreground")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            //@Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                foregroundColor = Integer.valueOf(String.valueOf(newValue));
                preference.setSummary(
                        getString(
                                R.string.pref_foreground_sum, 
                                ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)))
                                )
                );
                editor.putInt("foreground", foregroundColor);
                editor.commit();
                return true;
            }

        }); 
        
        
        backgroundColor         = settings.getInt("background", 0x80000000);
        foregroundColor         = settings.getInt("foreground", 0xEEFFFFFF);
        
        ((ColorPickerPreference)findPreference("background")).setSummary(getString(R.string.pref_background_sum, ColorPickerPreference.convertToARGB(backgroundColor)));
        ((ColorPickerPreference)findPreference("foreground")).setSummary(getString(R.string.pref_foreground_sum, ColorPickerPreference.convertToARGB(foregroundColor)));

        // Preinstalled
        sources = new ArrayList<>();

        sources.add("src_bible_en");
        sources.add("src_bible_ru");
        sources.add("src_bible_cn");

        sources.add("src_classics_biter");

        String state = Environment.getExternalStorageState();
        
        if( Environment.MEDIA_MOUNTED.equals(state) || 
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {
            // 
            String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String wqpath = sdpath + File.separator + DOTPATH;
            File appdir = new File(wqpath);
            
            int total = 0;
            if( appdir.exists() && appdir.isDirectory() ) {
                File[] files = appdir.listFiles();

                if( files.length == 0 ) {
                    exthelp.setEnabled(false);
                    exthelp.setTitle(getString(R.string.no_quotes_installed));              
                    exthelp.setSummary(getString(R.string.put_quotes, wqpath));                 
                } else {
                    // Update new checkboxes
                    for( File file : files ) {
                        if( file.canRead() ) {
                            //file.
                            try {
                                BufferedReader in = new BufferedReader(new FileReader(file));
                                String title = in.readLine();
                                String description = in.readLine();
                                
                                if( !title.trim().equals("") && !description.trim().equals("") ) {
                                    
                                    String key = file.getName();
                                    CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
                                    checkBoxPreference.setChecked(false);
                                    checkBoxPreference.setEnabled(true);
                                    checkBoxPreference.setTitle(title);
                                    checkBoxPreference.setKey(key);
                                    checkBoxPreference.setSummary(description);
                                    externalCategory.addPreference(checkBoxPreference);
                                    //
                                    sources.add(key);
                                    total++;
                                }
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                            }
                        }
                    }

                    if( total > 0 ) {
                        exthelp.setEnabled(false);
                        exthelp.setTitle(getString(R.string.found_quotes, total));
                        exthelp.setSummary(getString(R.string.put_quotes, wqpath));
                    } else {
                        exthelp.setEnabled(false);
                        exthelp.setTitle(getString(R.string.no_quotes_installed));              
                        exthelp.setSummary(getString(R.string.put_quotes, wqpath)); 
                    }

                }
            } else {

                String filekey = "MarkTwain.txt";
                if( appdir.mkdir() ) {
                    
                    File template = new File(appdir, filekey);
                    try {
                        OutputStream o = new FileOutputStream(template);
                        InputStream i = getApplicationContext().getResources().openRawResource(R.raw.src_mark_twain);
                        int r;
                        byte []b = new byte[1024];
                        while( (r = i.read(b)) >= 0 ) {
                            o.write(b, 0, r);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                exthelp.setEnabled(false);
                exthelp.setTitle(getString(R.string.installed_quotes));
                exthelp.setSummary(getString(R.string.put_quotes, wqpath));

                CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
                checkBoxPreference.setChecked(false);
                checkBoxPreference.setEnabled(true);
                checkBoxPreference.setKey(filekey);
                checkBoxPreference.setTitle("Mark Twain");              
                checkBoxPreference.setSummary("Mark Twain Quotes");                 
                externalCategory.addPreference(checkBoxPreference);
                sources.add(filekey);
            }


        } else {
            exthelp.setEnabled(false);
            exthelp.setTitle(getString(R.string.no_external_storage));
            exthelp.setSummary(getString(R.string.requires_external));
        }
        
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    private void save() {

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "Invalid widget id!");
            cancel();
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();

        boolean b, e = false;
        int i = 0;

        for (String key: sources) {
            b = sharedPrefs.getBoolean(key, false);
            if( b ) {
                String config_key = appWidgetId + "_use_source_" + i;
                editor.putString(config_key, key);
                ++i;
            }
            e |= b;
        }
        editor.putInt(appWidgetId + "_total_source_total", i);

        // None selected
        if( !e ) {
            showWarning();
        } else {
            // We need an Editor object to make preference changes.
            // All objects are from android.context.Context

            editor.putInt(appWidgetId + "_background", backgroundColor);
            editor.putInt(appWidgetId + "_foreground", foregroundColor);

            // Commit the edits!
            editor.apply();

            UpdateService.updateAppWidget(SettingsActivity.this, appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();

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
