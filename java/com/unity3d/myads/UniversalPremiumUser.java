package com.unity3d.myads;

import android.content.SharedPreferences;

public class UniversalPremiumUser {
    private final SharedPreferences sharedPreferences;

    public UniversalPremiumUser(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean b() {
        boolean value = sharedPreferences.getBoolean("isNoads", false);
        return value;
    }
}
