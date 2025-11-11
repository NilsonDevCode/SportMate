package com.nilson.appsportmate.features.auth.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.core.app.ApplicationProvider;

import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ActivityScenario;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nilson.appsportmate.HiltTestActivity;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.ui.auth.login.LoginFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

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

    /**
     * ✅ Verifica que los campos se llenen correctamente
     * y que el botón de login responda sin errores.
     */
    @Test
    public void ingresarAliasYPassword_yClickLogin_verificaInputCorrecto() {
        ActivityScenario<HiltTestActivity> activityScenario =
                ActivityScenario.launch(HiltTestActivity.class);

        activityScenario.onActivity(activity -> {
            // 1️⃣ Creamos el NavController de prueba
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            // 2️⃣ Creamos el fragmento
            LoginFragment fragment = new LoginFragment();

            // 3️⃣ Asignamos el NavController antes de mostrar la vista
            fragment.getViewLifecycleOwnerLiveData().observeForever(owner -> {
                if (owner != null && fragment.getView() != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController);
                }
            });

            // 4️⃣ Insertamos el fragmento en la actividad
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        // 5️⃣ Interacción
        onView(withId(R.id.etAlias)).perform(typeText("Nilson"));
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // 6️⃣ Verificación
        onView(withId(R.id.etAlias)).check(matches(withText("Nilson")));
        onView(withId(R.id.etPassword)).check(matches(withText("123456")));
    }

    /**
     * ✅ Verifica que se muestre un error visual si el alias está vacío.
     */
    @Test
    public void ingresarAliasVacio_muestraErrorEnAlias() {
        ActivityScenario<HiltTestActivity> activityScenario =
                ActivityScenario.launch(HiltTestActivity.class);

        activityScenario.onActivity(activity -> {
            // mismo setup que el test anterior
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

        // ✅ Verifica el error en el EditText
        onView(withId(R.id.etAlias))
                .check(matches(hasErrorText("Alias requerido")));
    }

    /**
     * ✅ Verifica que se muestre el mensaje en el TextView (tvMensaje)
     * cuando el alias o la contraseña son incorrectos.
     */
    @Test
    public void ingresarAliasIncorrecto_oPasswordIncorrecta_muestraErrorLogin() {
        ActivityScenario<HiltTestActivity> activityScenario =
                ActivityScenario.launch(HiltTestActivity.class);

        activityScenario.onActivity(activity -> {
            // 1️⃣ Configuramos NavController igual que los otros tests
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            LoginFragment fragment = new LoginFragment();
            fragment.getViewLifecycleOwnerLiveData().observeForever(owner -> {
                if (owner != null && fragment.getView() != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController);
                }
            });

            // 2️⃣ Insertamos el fragmento en la actividad de test
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        // 3️⃣ Interacción simulada con datos incorrectos
        onView(withId(R.id.etAlias)).perform(typeText("usuarioInvalido"));
        onView(withId(R.id.etPassword)).perform(typeText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // ✅ 4️⃣ Verifica que aparezca el mensaje en el TextView
        onView(withId(R.id.tvMensaje))
                .check(matches(isDisplayed()))
                .check(matches(withText("Alias o contraseña incorrectos")));
    }
}
