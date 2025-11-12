package com.nilson.appsportmate;

import android.app.Application;
import android.content.Context;
import androidx.test.runner.AndroidJUnitRunner;
import dagger.hilt.android.testing.HiltTestApplication;

public class HiltTestRunner extends AndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, HiltTestApplication.class.getName(), context);
    }

    @Override
    public void onStart() {
        // 🚀 Desactiva animaciones SOLO durante los tests instrumentados
        try {
            getUiAutomation().executeShellCommand("settings put global window_animation_scale 0");
            getUiAutomation().executeShellCommand("settings put global transition_animation_scale 0");
            getUiAutomation().executeShellCommand("settings put global animator_duration_scale 0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onStart();
    }

    @Override
    public void finish(int resultCode, android.os.Bundle results) {
        // 🔁 Reactiva animaciones al finalizar los tests (1x por defecto)
        try {
            getUiAutomation().executeShellCommand("settings put global window_animation_scale 1");
            getUiAutomation().executeShellCommand("settings put global transition_animation_scale 1");
            getUiAutomation().executeShellCommand("settings put global animator_duration_scale 1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.finish(resultCode, results);
    }
}
