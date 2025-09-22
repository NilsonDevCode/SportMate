package com.nilson.appsportmate;

import android.app.Application;
import android.util.Log;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class SportMate extends Application {

    public SportMate() {
        Log.i("SportMate", "Running from the main class of the app");
    }
}
