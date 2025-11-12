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
 * ✅ Conjunto de pruebas instrumentadas del LoginFragment.
 *
 * Estas pruebas evalúan el comportamiento general del formulario de inicio de sesión
 * con Firebase activo, verificando la interacción del usuario con la interfaz
 * y la validación de los campos principales antes del envío.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    /**
     * Inicializa el entorno de pruebas y asegura
     * que Firebase esté habilitado para los casos reales.
     */
    @Before
    public void setUp() {
        hiltRule.inject();
        com.nilson.appsportmate.ui.auth.login.LoginFragment.disableFirebaseForTest = false;
    }

    /**
     * Limpia el entorno de pruebas tras cada ejecución,
     * restaurando la configuración por defecto.
     */
    @After
    public void tearDown() {
        com.nilson.appsportmate.ui.auth.login.LoginFragment.disableFirebaseForTest = false;
    }

    /**
     * Verifica que el formulario de inicio de sesión permite introducir correctamente
     * un alias y una contraseña, y que ambos valores se muestran de forma correcta
     * en los campos correspondientes tras pulsar el botón de login.
     *
     * Este test comprueba la interacción básica del usuario con la interfaz,
     * sin validar aún la conexión real con Firebase.
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

        onView(withId(R.id.etAlias)).perform(typeText("Nilson"));
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.etAlias)).check(matches(withText("Nilson")));
        onView(withId(R.id.etPassword)).check(matches(withText("123456")));
    }

    /**
     * Comprueba que al intentar iniciar sesión sin escribir un alias,
     * la aplicación muestra un mensaje de error visual en el campo correspondiente.
     *
     * Este test garantiza que la validación de campos funciona correctamente
     * antes de realizar la autenticación.
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

        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etAlias)).check(matches(hasErrorText("Alias requerido")));
    }
}
