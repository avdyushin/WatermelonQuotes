package com.devfruit.watermelon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

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
}
