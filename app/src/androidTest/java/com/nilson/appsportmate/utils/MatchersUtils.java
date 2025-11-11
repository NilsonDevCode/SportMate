package com.nilson.appsportmate.utils;

import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher personalizado para verificar errores en TextInputLayout.
 */
public class MatchersUtils {

    public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) return false;

                CharSequence error = ((TextInputLayout) view).getError();
                if (error == null) return false;

                String hintError = error.toString();
                return expectedErrorText.equals(hintError);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Esperaba error: " + expectedErrorText);
            }
        };
    }
}
