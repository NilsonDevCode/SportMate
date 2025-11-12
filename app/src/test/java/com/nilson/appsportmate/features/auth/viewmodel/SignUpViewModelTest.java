package com.nilson.appsportmate.features.auth.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.ui.auth.signUp.SignUpViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * ✅ Pruebas unitarias de SignUpViewModel (caja blanca)
 *
 * Evalúa las validaciones y comportamiento lógico
 * del método onRegisterClicked() sin tocar Firebase real.
 */
@SuppressWarnings("unchecked")
public class SignUpViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private Task<SignInMethodQueryResult> mockTaskFetch; // simula consulta de alias

    private SignUpViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new SignUpViewModel(mockAuth, mockDb); // ✅ usa constructor con mocks

        when(mockAuth.fetchSignInMethodsForEmail(anyString())).thenReturn(mockTaskFetch);
        when(mockTaskFetch.addOnSuccessListener(any())).thenReturn(mockTaskFetch);
        when(mockTaskFetch.addOnFailureListener(any())).thenReturn(mockTaskFetch);
    }

    @Test
    public void aliasVacio_muestraErrorAlias() {
        viewModel.onRegisterClicked(
                mockContext,
                "", "123456", "123456", "Nilson", "Cursodam",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                null, "usuario", "aytoId", "comId", "provId", "ciuId"
        );
        assertEquals("Alias requerido", viewModel.getEAlias().getValue());
    }

    @Test
    public void passwordVacia_muestraErrorPassword() {
        viewModel.onRegisterClicked(
                mockContext,
                "NilDev", "", "", "Nilson", "Cursodam",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                null, "usuario", "aytoId", "comId", "provId", "ciuId"
        );
        assertEquals("Contraseña requerida", viewModel.getEPassword().getValue());
    }

    @Test
    public void contraseñasNoCoinciden_muestraErrorPassword() {
        viewModel.onRegisterClicked(
                mockContext,
                "NilDev", "123456", "abcdef", "Nilson", "Cursodam",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                null, "usuario", "aytoId", "comId", "provId", "ciuId"
        );
        assertEquals("Las contraseñas no coinciden", viewModel.getEPassword().getValue());
    }

    @Test
    public void nombreVacio_muestraErrorNombre() {
        viewModel.onRegisterClicked(
                mockContext,
                "NilDev", "123456", "123456", "", "Cursodam",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                null, "usuario", "aytoId", "comId", "provId", "ciuId"
        );
        assertEquals("Nombre requerido", viewModel.getENombre().getValue());
    }

    @Test
    public void ayuntamientoSinRazonSocial_muestraErrorRazon() {
        viewModel.onRegisterClicked(
                mockContext,
                "NilAyto", "123456", "123456", "Ayto Central", "",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                "", "ayuntamiento", null, "comId", "provId", "ciuId"
        );
        assertEquals("Razón social requerida", viewModel.getERazon().getValue());
    }

    @Test
    public void datosValidos_noHayErroresIniciales() {
        viewModel.onRegisterClicked(
                mockContext,
                "NilDev", "123456", "123456", "Nilson", "Cursodam",
                "Comunidad", "Provincia", "Ciudad", "Pueblo",
                "Razon", "usuario", "aytoId", "comId", "provId", "ciuId"
        );
        assertNull(viewModel.getEAlias().getValue());
        assertNull(viewModel.getEPassword().getValue());
        assertNull(viewModel.getENombre().getValue());
        assertNull(viewModel.getEApellidos().getValue());
    }
}
