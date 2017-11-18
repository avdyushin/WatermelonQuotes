package com.devfruit.watermelon;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by avdyushin on 18/11/2017.
 */

public class UserSettings {

    private static final String SETTINGS = "watermelonQuotes";

    public static SharedPreferences shared(Context context) {
        return context.getSharedPreferences(SETTINGS, 0);
    }
}
