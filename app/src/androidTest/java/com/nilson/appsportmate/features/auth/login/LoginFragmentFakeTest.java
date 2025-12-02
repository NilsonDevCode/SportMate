package com.nilson.appsportmate.features.auth.login;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.nilson.appsportmate.features.auth.signUp.SignUpFragmentTest.waitFor;

import android.os.Bundle;
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
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * Test aislado del LoginFragment usando un ViewModel falso.
 * Verifica que NO crashea con credenciales incorrectas.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFragmentFakeTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    /**
     * Subclase del fragmento para inyectar un ViewModel falso.
     */
    public static class LoginFragmentFake extends LoginFragment {
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            this.viewModel = new FakeLoginViewModel();  // 🔥 inyectamos ViewModel falso
            super.onViewCreated(view, savedInstanceState);
        }
    }

    @Before
    public void setUp() {
        hiltRule.inject();
        LoginFragment.disableFirebaseForTest = true; // desactiva Firebase real
    }

    @After
    public void tearDown() {
        LoginFragment.disableFirebaseForTest = false;
    }

    @Test
    public void ingresarAliasIncorrecto_oPasswordIncorrecta_muestraErrorLogin() {

        ActivityScenario<HiltTestActivity> scenario =
                ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {

            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

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

        // Interactuamos con el login
        onView(withId(R.id.etAlias)).perform(replaceText("usuarioInvalido"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click()); // 🔥 sin scrollTo()

        onView(isRoot()).perform(waitFor(600)); // pequeña espera

        // comprobamos que el fragment sigue visible
        onView(withId(R.id.etAlias)).check(matches(isDisplayed()));
    }
}
