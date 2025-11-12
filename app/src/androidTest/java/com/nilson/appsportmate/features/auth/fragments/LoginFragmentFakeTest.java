package com.nilson.appsportmate.features.auth.fragments;

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
import com.nilson.appsportmate.features.auth.login.FakeLoginViewModel;
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * ✅ Prueba unitaria aislada del LoginFragment.
 *
 * En este test se utiliza un ViewModel falso (FakeLoginViewModel)
 * que permite verificar el comportamiento del fragmento
 * sin necesidad de conectarse a Firebase.
 *
 * El objetivo es comprobar que, ante credenciales erróneas,
 * la interfaz muestra el mensaje de error de forma correcta.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFragmentFakeTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // Subclase del fragmento que inyecta un ViewModel falso
    public static class LoginFragmentFake extends LoginFragment {
        @Override
        public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
            this.viewModel = new FakeLoginViewModel();
            super.onViewCreated(view, savedInstanceState);
        }
    }

    /**
     * Se ejecuta antes de cada test.
     * Activa el modo de prueba para evitar inicializar Firebase.
     */
    @Before
    public void setUp() {
        hiltRule.inject();
        com.nilson.appsportmate.ui.auth.login.LoginFragment.disableFirebaseForTest = true;
    }

    /**
     * Se ejecuta después de cada test.
     * Restaura el comportamiento normal del fragmento
     * reactivando la inicialización de Firebase.
     */
    @After
    public void tearDown() {
        com.nilson.appsportmate.ui.auth.login.LoginFragment.disableFirebaseForTest = false;
    }

    /**
     * Evalúa el comportamiento del fragmento cuando se introducen credenciales incorrectas.
     *
     * Utiliza un ViewModel simulado (FakeLoginViewModel) para reproducir
     * una respuesta de error sin depender de Firebase ni de conexión real.
     *
     * El objetivo es confirmar que el mensaje de error se muestra en pantalla
     * y que la interfaz reacciona de forma adecuada ante un inicio de sesión fallido.
     */
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

        onView(withId(R.id.etAlias)).perform(replaceText("usuarioInvalido"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(600));
        onView(withId(R.id.tvMensaje)).check(matches(isDisplayed()));
    }
}
