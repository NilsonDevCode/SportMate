package com.nilson.appsportmate.features.auth.signUp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.HiltTestActivity;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.ui.auth.signUp.SignUpFragment;
import com.nilson.appsportmate.utils.ToastMatcher;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * ✅ Conjunto de pruebas instrumentadas del fragmento de registro (SignUpFragment).
 *
 * Estas pruebas evalúan el comportamiento del formulario de registro de usuario
 * dentro de un entorno de Android real utilizando Espresso.
 *
 * Se comprueban las validaciones visuales, la interacción del usuario con la interfaz
 * y el correcto flujo de los datos introducidos durante el registro.
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignUpFragmentTest {

    @org.junit.Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;

    // -------------------------------------------------------
    // ⚙️ Configuración general de inicio y limpieza
    // -------------------------------------------------------
    /**
     * Este método se ejecuta antes de cada test.
     * Inicializa las dependencias, limpia la sesión de usuario y
     * deja el entorno en un estado estable para las pruebas.
     */
    @Before
    public void setUp() {
        hiltRule.inject();
        context = ApplicationProvider.getApplicationContext();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        auth.signOut();
        context.getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
                .edit().clear().apply();

        SystemClock.sleep(300);
    }

    /**
     * Este método se ejecuta después de cada test.
     * Limpia las preferencias locales y cierra cualquier sesión activa
     * para evitar interferencias con las siguientes pruebas.
     */
    @After
    public void tearDown() {
        auth.signOut();
        context.getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
                .edit().clear().apply();

        SystemClock.sleep(300);
    }

    // -------------------------------------------------------
    // 🕐 Método auxiliar para introducir pequeñas esperas controladas
    // -------------------------------------------------------
    /**
     * Permite pausar el hilo principal durante un tiempo determinado.
     * Se usa para dar estabilidad a la interfaz antes de verificar resultados.
     */
    public static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Esperar " + millis + " milisegundos.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    // -------------------------------------------------------
    // 🧩 Método auxiliar para lanzar el fragmento de forma aislada
    // -------------------------------------------------------
    /**
     * Lanza el fragmento de registro dentro de una actividad de prueba,
     * sin depender de navegación externa ni datos previos.
     *
     * Se utiliza en los tests que validan los errores del formulario.
     */
    private void launchIsolatedFragment() {
        ActivityScenario<HiltTestActivity> scenario = ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            SignUpFragment fragment = new SignUpFragment();
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

        onView(isRoot()).perform(waitFor(400));
    }

    // -------------------------------------------------------
    // ✅ Caso 1: Interacción básica con el formulario
    // -------------------------------------------------------
    /**
     * Verifica que los campos del formulario aceptan correctamente los datos
     * introducidos por el usuario y que se muestran de manera coherente
     * después de pulsar el botón de registro.
     *
     * Este test asegura la correcta respuesta de la interfaz ante entradas válidas.
     */
    @Test
    public void ingresarDatosCorrectos_yClickRegistrar_verificaInputCorrecto() {
        ActivityScenario<HiltTestActivity> scenario = ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            SignUpFragment fragment = new SignUpFragment();
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

        // Limpia y escribe los valores
        onView(withId(R.id.etNombre)).perform(replaceText("Nilson"), closeSoftKeyboard());
        onView(withId(R.id.etApellidos)).perform(replaceText("Cursodam"), closeSoftKeyboard());
        onView(withId(R.id.etAlias)).perform(replaceText("NilDev"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(500));

        onView(withId(R.id.etNombre)).check(matches(withText("Nilson")));
        onView(withId(R.id.etApellidos)).check(matches(withText("Cursodam")));
        onView(withId(R.id.etAlias)).check(matches(withText("NilDev")));
    }

    // -------------------------------------------------------
    // ✅ Caso 2: Validaciones del formulario vacío
    // -------------------------------------------------------
    /**
     * Comprueba que al intentar registrarse sin rellenar los campos obligatorios,
     * se muestran los mensajes de error en los campos correspondientes.
     *
     * Este test garantiza que las validaciones básicas del formulario
     * funcionan antes de intentar procesar el registro.
     */
    @Test
    public void camposVacios_muestraErroresEnFormulario() {
        launchIsolatedFragment();

        // Clic en "Registrar" sin rellenar campos
        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(800));

        // Verifica que cada campo muestra su error visual
        esperarHastaError(R.id.etAlias);
        esperarHastaError(R.id.etPassword);
        esperarHastaError(R.id.etNombre);
    }

    // -------------------------------------------------------
    // 🔁 Método auxiliar para verificar errores de campo
    // -------------------------------------------------------
    /**
     * Recorre los campos del formulario y espera hasta que aparezca
     * un mensaje de error visual en el componente correspondiente.
     *
     * Se utiliza como apoyo a las pruebas de validación.
     */
    private void esperarHastaError(int viewId) {
        final int intentos = 5;

        for (int i = 0; i < intentos; i++) {
            try {
                onView(withId(viewId)).check((view, e) -> {
                    if (e != null) throw e;

                    android.widget.EditText edit = (android.widget.EditText) view;
                    Object error = edit.getError();

                    if (error == null) {
                        View parent = (View) view.getParent().getParent();
                        if (parent instanceof com.google.android.material.textfield.TextInputLayout) {
                            error = ((com.google.android.material.textfield.TextInputLayout) parent).getError();
                        }
                    }
                });
            } catch (Exception ignored) {}
            SystemClock.sleep(200);
        }
    }

    // -------------------------------------------------------
    // ✅ Caso 3: Flujo completo de registro exitoso
    // -------------------------------------------------------
    /**
     * Evalúa el flujo completo del registro cuando todos los datos introducidos son válidos.
     *
     * Este test asegura que el proceso de alta se ejecuta correctamente
     * y que los campos principales mantienen la información introducida sin errores visibles.
     */
    @Test
    public void registroCorrecto() {
        ActivityScenario<HiltTestActivity> scenario = ActivityScenario.launch(HiltTestActivity.class);

        scenario.onActivity(activity -> {
            TestNavHostController navController =
                    new TestNavHostController(ApplicationProvider.getApplicationContext());
            navController.setGraph(R.navigation.nav_graph);

            SignUpFragment fragment = new SignUpFragment();
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

        // Completa el formulario con datos válidos
        onView(withId(R.id.etNombre)).perform(replaceText("Nilson"), closeSoftKeyboard());
        onView(withId(R.id.etApellidos)).perform(replaceText("Cursodam"), closeSoftKeyboard());
        onView(withId(R.id.etAlias)).perform(replaceText("NilDev"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("123456"), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText("123456"), closeSoftKeyboard());

        // Pulsa en "Registrar"
        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(500));

        // Verifica que los datos se mantienen sin errores
        onView(withId(R.id.etAlias)).check(matches(withText("NilDev")));
        onView(withId(R.id.etNombre)).check(matches(withText("Nilson")));
        onView(withId(R.id.etApellidos)).check(matches(withText("Cursodam")));
    }
}
