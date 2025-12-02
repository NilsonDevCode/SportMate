package com.nilson.appsportmate.features.auth.login;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nilson.appsportmate.HiltTestActivity;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * Tests instrumentados del LoginFragment.
 * Verifica interacción de usuario real con el formulario.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void setUp() {
        hiltRule.inject();
        LoginFragment.disableFirebaseForTest = false;   // Firebase real activado
    }

    @After
    public void tearDown() {
        LoginFragment.disableFirebaseForTest = false;
    }

    /**
     * Verifica que alias y password se introducen correctamente
     * y permanecen visibles tras pulsar login.
     */
    @Test
    public void ingresarAliasYPassword_yClickLogin_verificaInputCorrecto() {

        ActivityScenario<HiltTestActivity> scenario =
                ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {

            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());

            navController.setGraph(R.navigation.nav_graph);

            LoginFragment fragment = new LoginFragment();

            // ⭐ EXACTAMENTE igual que antes, adaptado al fragment actual
            fragment.getViewLifecycleOwnerLiveData().observeForever(owner -> {
                if (owner != null && fragment.getView() != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController);
                }
            });

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        // Escribir alias y password como antes
        onView(withId(R.id.etAlias)).perform(typeText("Nilson"));
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard());

        // Pulsar login
        onView(withId(R.id.btnLogin)).perform(click());

        // Validar que siguen escritos tal cual
        onView(withId(R.id.etAlias)).check(matches(withText("Nilson")));
        onView(withId(R.id.etPassword)).check(matches(withText("123456")));
    }

    /**
     * Valida que si alias está vacío, se muestra el error correspondiente.
     */
    @Test
    public void ingresarAliasVacio_muestraErrorEnAlias() {

        ActivityScenario<HiltTestActivity> scenario =
                ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {

            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());

            navController.setGraph(R.navigation.nav_graph);

            LoginFragment fragment = new LoginFragment();

            // ⭐ Mismo patrón exacto del test original
            fragment.getViewLifecycleOwnerLiveData().observeForever(owner -> {
                if (owner != null && fragment.getView() != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController);
                }
            });

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        // No escribimos nada → click directo
        onView(withId(R.id.btnLogin)).perform(click());

        // Esperamos el error EXACTAMENTE igual que antes
        onView(withId(R.id.etAlias)).check(matches(hasErrorText("Alias requerido")));
    }
}
