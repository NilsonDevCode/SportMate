package com.nilson.appsportmate.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.test.espresso.IdlingResource;

public class ToastIdlingResource implements IdlingResource {

    private final Activity activity;
    private final String expectedText;
    private ResourceCallback callback;

    public ToastIdlingResource(Activity activity, String expectedText) {
        this.activity = activity;
        this.expectedText = expectedText;
    }

    @Override
    public String getName() {
        return "ToastIdlingResource:" + expectedText;
    }

    @Override
    public boolean isIdleNow() {
        Toast toast = Toast.makeText(activity, expectedText, Toast.LENGTH_SHORT);
        if (toast != null && callback != null) {
            callback.onTransitionToIdle();
        }
        return toast != null;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}
