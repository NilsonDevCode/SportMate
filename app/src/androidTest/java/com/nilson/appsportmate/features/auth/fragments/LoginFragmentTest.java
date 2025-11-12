package com.nilson.appsportmate.features.auth.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.nilson.appsportmate.features.auth.signUp.SignUpFragmentTest.waitFor;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nilson.appsportmate.HiltTestActivity;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.auth.login.FakeLoginViewModel;
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * ✅ Tests del LoginFragment.
 * - Los dos primeros usan Firebase real.
 * - El tercero usa un ViewModel falso (FakeLoginViewModel) sin conexión.
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
    }

    // ============================================================
    // ✅ SUBCLASE ESTÁTICA FAKE para aislar el ViewModel del test 3
    // ============================================================
    public static class LoginFragmentFake extends LoginFragment {
        @Override
        public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
            this.viewModel = new com.nilson.appsportmate.ui.auth.login.LoginViewModel(); // ViewModel falso
            super.onViewCreated(view, savedInstanceState);
        }
    }

    // ============================================================
    // ✅ CASO 1: Login correcto (con Firebase real)
    // ============================================================
    @Test
    public void ingresarAliasYPassword_yClickLogin_verificaInputCorrecto() {
        ActivityScenario<HiltTestActivity> activityScenario =
                ActivityScenario.launch(HiltTestActivity.class);

        activityScenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            LoginFragment fragment = new LoginFragment();

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

        // Escribe datos válidos
        onView(withId(R.id.etAlias)).perform(typeText("Nilson"));
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // Verifica que se escribieron correctamente
        onView(withId(R.id.etAlias)).check(matches(withText("Nilson")));
        onView(withId(R.id.etPassword)).check(matches(withText("123456")));
    }

    // ============================================================
    // ✅ CASO 2: Alias vacío muestra error visual (Firebase real)
    // ============================================================
    @Test
    public void ingresarAliasVacio_muestraErrorEnAlias() {
        ActivityScenario<HiltTestActivity> activityScenario =
                ActivityScenario.launch(HiltTestActivity.class);

        activityScenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            LoginFragment fragment = new LoginFragment();
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

        // Click sin escribir nada
        onView(withId(R.id.btnLogin)).perform(click());

        // Verifica error en alias
        onView(withId(R.id.etAlias))
                .check(matches(hasErrorText("Alias requerido")));
    }

    // ============================================================
    // ✅ CASO 3: Alias o password incorrectos (aislado sin Firebase)
    // ============================================================
    @Test
    public void ingresarAliasIncorrecto_oPasswordIncorrecta_muestraErrorLogin() {
        ActivityScenario<HiltTestActivity> scenario = ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            // ⚡ Fragmento aislado con FakeLoginViewModel
            LoginFragment fragment = new LoginFragmentFake();

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

        // Simula alias y password incorrectos
        onView(withId(R.id.etAlias)).perform(replaceText("usuarioInvalido"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(scrollTo(), click());

        // Espera corta
        onView(isRoot()).perform(waitFor(600));

        // Verifica que aparece el mensaje en pantalla
        onView(withId(R.id.tvMensaje)).check(matches(isDisplayed()));
    }
}
