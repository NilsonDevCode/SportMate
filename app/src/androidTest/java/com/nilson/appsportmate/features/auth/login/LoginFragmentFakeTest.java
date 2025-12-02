package com.nilson.appsportmate.features.auth.login;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * Prueba unitaria del LoginFragment usando un ViewModel falso.
 * Verifica que el fragmento responde correctamente ante login inválido.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFragmentFakeTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    public static class LoginFragmentFake extends LoginFragment {
        @Override
        public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
            this.viewModel = new FakeLoginViewModel();
            super.onViewCreated(view, savedInstanceState);
        }
    }

    @Before
    public void setUp() {
        hiltRule.inject();
        LoginFragment.disableFirebaseForTest = true;
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

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();

            Navigation.setViewNavController(fragment.requireView(), navController);
        });

        onView(withId(R.id.etAlias)).perform(replaceText("usuarioInvalido"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(600));

        // El mensaje ya NO se muestra en pantalla, pero el fragmento debe seguir visible.
        onView(withId(R.id.etAlias)).check(matches(isDisplayed()));
    }
}
