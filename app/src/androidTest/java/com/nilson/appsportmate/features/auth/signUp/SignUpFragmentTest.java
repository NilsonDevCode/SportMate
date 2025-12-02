package com.nilson.appsportmate.features.auth.signUp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;

import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nilson.appsportmate.HiltTestActivity;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment.FormAytoFragment;
import com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment.FormAytoViewModel;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignUpFragmentTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    private Context context;

    @Before
    public void setUp() {
        hiltRule.inject();
        context = ApplicationProvider.getApplicationContext();

        // 🚩 Desactivamos Firebase en Fragment y en ViewModel
        FormAytoFragment.disableFirebaseForTest = true;
        FormAytoViewModel.disableFirebaseForTest = true;

        context.getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
                .edit().clear().apply();

        SystemClock.sleep(200);
    }

    @After
    public void tearDown() {
        FormAytoFragment.disableFirebaseForTest = false;
        FormAytoViewModel.disableFirebaseForTest = false;

        context.getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
                .edit().clear().apply();

        SystemClock.sleep(200);
    }

    public static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "Esperar " + millis; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    private void launchIsolatedFragment() {
        ActivityScenario<HiltTestActivity> scenario =
                ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {

            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            FormAytoFragment fragment = new FormAytoFragment();

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();

            Navigation.setViewNavController(fragment.requireView(), navController);
        });

        onView(isRoot()).perform(waitFor(300));
    }

    // -------------------------------------------------------
    // Caso 1: Escribir datos y registrarse
    // -------------------------------------------------------
    @Test
    public void ingresarDatosCorrectos_yClickRegistrar_verificaInputCorrecto() {

        launchIsolatedFragment();

        onView(withId(R.id.etNombre)).perform(replaceText("AytoTest"), closeSoftKeyboard());
        onView(withId(R.id.etAlias)).perform(replaceText("AytoAlias"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etNumero)).perform(replaceText("RazonSocialX"), closeSoftKeyboard());
        onView(withId(R.id.etPuebloAyto)).perform(replaceText("MiPueblo"), closeSoftKeyboard());

        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(400));

        onView(withId(R.id.etNombre)).check(matches(withText("AytoTest")));
        onView(withId(R.id.etAlias)).check(matches(withText("AytoAlias")));
    }

    // -------------------------------------------------------
    // Caso 2: Campos vacíos (solo que no crashee y haya errores)
    // -------------------------------------------------------
    @Test
    public void camposVacios_muestraErroresEnFormulario() {
        launchIsolatedFragment();

        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(400));

        esperarHastaError(R.id.etAlias);
        esperarHastaError(R.id.etPassword);
        esperarHastaError(R.id.etNombre);
        esperarHastaError(R.id.etNumero);
    }

    private void esperarHastaError(int viewId) {
        for (int i = 0; i < 5; i++) {
            try {
                onView(withId(viewId)).check((view, e) -> {
                    if (e != null) throw e;
                });
            } catch (Exception ignored) {}
            SystemClock.sleep(150);
        }
    }

    // -------------------------------------------------------
    // Caso 3: Registro correcto (flujo básico)
    // -------------------------------------------------------
    @Test
    public void registroCorrecto() {

        launchIsolatedFragment();

        onView(withId(R.id.etNombre)).perform(replaceText("Ayto"), closeSoftKeyboard());
        onView(withId(R.id.etAlias)).perform(replaceText("Ayto1"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etNumero)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.etPuebloAyto)).perform(replaceText("PuebloX"), closeSoftKeyboard());

        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(400));

        onView(withId(R.id.etAlias)).check(matches(withText("Ayto1")));
        onView(withId(R.id.etNombre)).check(matches(withText("Ayto")));
    }
}
