package com.devfruit.watermelon;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

class Analytics {
    static void logSourceCount(Context context, int count) {
        FirebaseAnalytics fa = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.QUANTITY, count);
        fa.logEvent("Selected_Source_Count", bundle);
    }
}
