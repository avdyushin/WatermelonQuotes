package com.devfruit.watermelon;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {
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
                    updateSummary(preference, (Set<String>) selected);
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
            preference.setSummary("");
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
        MultiSelectListPreference help = (MultiSelectListPreference) findPreference("external_sources");
        Log.d("TAG", "Updated " + help.toString());
        Log.d("TAG", "Params " + hasStorage + ", " + granted);
        if (hasStorage) {
            if (granted) {
                ArrayList<QuoteSource> installed = FilesProvider.userInstalledQuotes();
                int total = installed.size();
                if (total > 0) {
                    help.setTitle("Place your files into: " + FilesProvider.QUOTES_PATH);
                    help.setSummary(getString(R.string.found_quotes, total));
                } else {
                    help.setTitle("Place your files into: " + FilesProvider.QUOTES_PATH);
                    help.setSummary(R.string.no_quotes_installed);
                }
            } else {
                help.setTitle("You didn't grant permissions to read external storage");
                help.setSummary(R.string.no_quotes_installed);
            }
        } else {
            help.setTitle("You have not external storage");
            help.setSummary(R.string.no_quotes_installed);
        }
    }

    private void checkPermission() {
        String state = Environment.getExternalStorageState();
        if ( state.equals(Environment.MEDIA_MOUNTED) ||
                state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) ) {

            String permissions = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(
                    this.getActivity().getApplicationContext(), permissions
                ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this.getActivity(), new String[] { permissions }, REQUEST
                );
            } else {
                updateExternalSources(true, true);
            }

        } else {
            updateExternalSources(false, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST: {
                boolean granted = (
                        (grantResults.length > 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                );
                updateExternalSources(true, granted);
                break;
            }
        }
    }
}
