package com.nilson.appsportmate.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthLocalDataSource {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_UID = "uid";
    private static final String KEY_ALIAS = "alias";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sharedPreferences;

    public AuthLocalDataSource(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String uid, String alias, String role) {
        sharedPreferences.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_ALIAS, alias)
                .putString(KEY_ROLE, role)
                .apply();
    }
}
