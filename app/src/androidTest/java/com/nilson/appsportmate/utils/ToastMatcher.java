package com.nilson.appsportmate.utils;

import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * ✅ ToastMatcher compatible con Android 11+ y Android 12+.
 * Reconoce tanto TYPE_APPLICATION_PANEL como TYPE_BASE_APPLICATION.
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;

        // 🧩 En Android 12+ el Toast se muestra como BASE_APPLICATION
        if (type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                || type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                || type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) {

            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();

            // Los Toasts clásicos o modernos cumplen que ambos tokens son iguales
            if (windowToken == appToken) {
                return true;
            }
        }
        return false;
    }
}
