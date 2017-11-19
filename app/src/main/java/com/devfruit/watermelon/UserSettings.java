package com.devfruit.watermelon;

import android.content.Context;
import android.content.SharedPreferences;

class UserSettings {

    private static final String SETTINGS = "watermelonQuotes";

    static SharedPreferences shared(Context context) {
        return context.getSharedPreferences(SETTINGS, 0);
    }
}
