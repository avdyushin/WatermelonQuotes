package com.devfruit.watermelon;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {

    private final static String TAG = SettingsFragment.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        checkPermission();
        updateBiterSelected();
    }

    private void updateBiterSelected() {
        MultiSelectListPreference preference = (MultiSelectListPreference)findPreference("biter_quotes_key");
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object selected) {
                if (selected instanceof Set) {
                    //noinspection unchecked
                    updateSummary(preference, (Set<String>)selected);
                }
                return true;
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        Set<String> selected = prefs.getStringSet("biter_quotes_key", null);
        updateSummary(preference, selected);
    }

    private void updateSummary(Preference preference, Set<String> values) {
        if (values.isEmpty()) {
            preference.setSummary(R.string.ru_biter_sum);
        } else {
            preference.setSummary(getNamesFromSelectedValues(values).toString());
        }
    }

    private List<String> getNamesFromSelectedValues(Set<String> values) {
        String [] biter_values = getResources().getStringArray(R.array.biter_values);
        String [] biter_names = getResources().getStringArray(R.array.biter_names);
        List<String> result = new ArrayList<>();
        for (String v: values) {
            int index = Arrays.asList(biter_values).indexOf(v);
            if (index != -1) {
                result.add(biter_names[index]);
            }
        }
        return result;
    }

    final static int REQUEST = 1;

    private void updateExternalSources(boolean hasStorage, boolean granted) {
        PreferenceScreen external_sources = (PreferenceScreen)findPreference("external_sources");
        Log.d(TAG, "Updated " + external_sources.toString());
        Log.d(TAG, "Params with " + hasStorage + ", " + granted);
        if (hasStorage) {
            if (granted) {
                ArrayList<QuoteSource> installed = ExternalSourceProvider.userInstalledQuotes();
                int total = installed.size();
                if (total == 0 ) {
                    external_sources.setTitle(R.string.not_found);
                } else {
                    external_sources.setTitle(getResources().getQuantityString(R.plurals.found_quotes, total, total));
                }
                external_sources.setSummary(getString(R.string.put_quotes, ExternalSourceProvider.QUOTES_PATH));
                createCheckBoxes(installed);
            } else {
                external_sources.setTitle(R.string.no_permissions);
                external_sources.setSummary(R.string.requires_external);
            }
        } else {
            external_sources.setTitle(R.string.no_external_storage);
            external_sources.setSummary(R.string.requires_external);
        }
    }

    private void createCheckBoxes(ArrayList<QuoteSource> sources) {
        PreferenceCategory category = (PreferenceCategory)findPreference("external_category");
        for (QuoteSource source: sources) {
            CheckBoxPreference boxPreference = new CheckBoxPreference(this.getActivity().getBaseContext());
            boxPreference.setKey(source.key);
            boxPreference.setTitle(source.title);
            boxPreference.setSummary(source.description);
            boxPreference.setDefaultValue(false);
            category.addPreference(boxPreference);
        }
    }

    private void checkPermission() {

        Log.d(TAG,"check permissions");

        String state = Environment.getExternalStorageState();
        if ( state.equals(Environment.MEDIA_MOUNTED) ||
                state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) ) {

            Log.d(TAG,"Found card");

            String permissions = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(
                    this.getActivity().getApplicationContext(), permissions
                ) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG,"No permissions, will ask");
                FragmentCompat.requestPermissions(
                        this, new String[] { permissions }, REQUEST
                );
            } else {
                Log.d(TAG,"Yes, we have");
                updateExternalSources(true, true);
            }

        } else {
            Log.d(TAG,"NO SD CARD");
            updateExternalSources(false, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST: {
                boolean granted = (
                        (grantResults.length >= 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                );
                if (grantResults.length >= 1) {
                    Log.d(TAG,"results " + grantResults[0] + ", " + PackageManager.PERMISSION_GRANTED);
                } else {
                    Log.d(TAG,"length < 1");
                }
                updateExternalSources(true, granted);
                break;
            }
        }
    }
}
