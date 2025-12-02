package com.nilson.appsportmate.features.auth.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment.FormAytoViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class FormAytoViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private Context mockContext;
    @Mock private FirebaseAuth mockAuth;
    @Mock private FirebaseFirestore mockDb;
    @Mock private Task<SignInMethodQueryResult> mockFetchTask;

    private FormAytoViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 🔥 Igual que en LoginViewModelTest: desactivar Log.d() en tests
        System.setProperty("unitTest", "true");

        viewModel = new FormAytoViewModel(mockAuth, mockDb);

        // Mock básico para fetchSignInMethodsForEmail()
        when(mockAuth.fetchSignInMethodsForEmail(anyString())).thenReturn(mockFetchTask);
        when(mockFetchTask.addOnSuccessListener(any())).thenReturn(mockFetchTask);
        when(mockFetchTask.addOnFailureListener(any())).thenReturn(mockFetchTask);
    }

    // ---------------------------------------------------------
    // VALIDACIONES
    // ---------------------------------------------------------

    @Test
    public void aliasVacio_muestraErrorAlias() {
        viewModel.onRegisterClicked(
                mockContext,
                "",                  // alias vacío
                "123456", "123456",
                "Ayto", "", "Com", "Prov", "Ciu",
                "PuebloX", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Alias requerido", viewModel.getEAlias().getValue());
    }

    @Test
    public void passwordVacia_muestraErrorPassword() {
        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev", "", "",
                "Ayto", "", "Com", "Prov", "Ciu",
                "PuebloX", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Contraseña requerida", viewModel.getEPassword().getValue());
    }

    @Test
    public void contraseñasNoCoinciden_muestraErrorPassword() {
        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "abcdef",
                "Ayto", "", "Com", "Prov", "Ciu",
                "PuebloX", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Las contraseñas no coinciden", viewModel.getEPassword().getValue());
    }

    @Test
    public void nombreVacio_muestraErrorNombre() {
        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "123456",
                "", "", "Com", "Prov", "Ciu",
                "PuebloX", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Nombre requerido", viewModel.getENombre().getValue());
    }

    @Test
    public void razonSocialVacia_muestraErrorRazon() {
        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "123456",
                "Ayto", "", "Com", "Prov", "Ciu",
                "PuebloX", "", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Razón social requerida", viewModel.getERazon().getValue());
    }

    @Test
    public void puebloVacio_muestraErrorMensaje() {
        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "123456",
                "Ayto", "", "Com", "Prov", "Ciu",
                "", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Debes crear un pueblo", viewModel.getMessage().getValue());
    }

    // ---------------------------------------------------------
    // ALIAS EXISTENTE
    // ---------------------------------------------------------

    @Test
    public void aliasExistente_muestraErrorAlias() {

        // Simular que Firebase devuelve un alias existente
        doAnswer(invocation -> {
            OnSuccessListener<SignInMethodQueryResult> listener = invocation.getArgument(0);
            SignInMethodQueryResult result = mock(SignInMethodQueryResult.class);
            when(result.getSignInMethods()).thenReturn(Collections.singletonList("password"));
            listener.onSuccess(result);
            return mockFetchTask;
        }).when(mockFetchTask).addOnSuccessListener(any());

        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "123456",
                "Ayto", "", "Com", "Prov", "Ciu",
                "Pueblo", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        assertEquals("Alias ya está en uso", viewModel.getEAlias().getValue());
        verify(mockAuth, never()).createUserWithEmailAndPassword(anyString(), anyString());
    }

    // ---------------------------------------------------------
    // REGISTRO CORRECTO
    // ---------------------------------------------------------

    @Test
    public void registroCorrecto_creaUsuarioFirebase() {

        // Simular alias NO existente
        doAnswer(invocation -> {
            OnSuccessListener<SignInMethodQueryResult> listener = invocation.getArgument(0);
            SignInMethodQueryResult result = mock(SignInMethodQueryResult.class);
            when(result.getSignInMethods()).thenReturn(Collections.emptyList());
            listener.onSuccess(result);
            return mockFetchTask;
        }).when(mockFetchTask).addOnSuccessListener(any());

        // Mock: createUserWithEmailAndPassword()
        Task<AuthResult> mockCreate = mock(Task.class);
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockCreate);

        when(mockCreate.addOnSuccessListener(any())).thenReturn(mockCreate);
        when(mockCreate.addOnFailureListener(any())).thenReturn(mockCreate);

        viewModel.onRegisterClicked(
                mockContext,
                "AytoDev",
                "123456", "123456",
                "Ayuntamiento Central", "",
                "Com", "Prov", "Ciu",
                "PuebloX", "Razon", "ayto",
                "", "comId", "provId", "ciuId"
        );

        // Verifica la llamada a Firebase
        verify(mockAuth).createUserWithEmailAndPassword(anyString(), anyString());

        // Ningún error marcado
        assertNull(viewModel.getEAlias().getValue());
        assertNull(viewModel.getEPassword().getValue());
        assertNull(viewModel.getENombre().getValue());
        assertNull(viewModel.getERazon().getValue());
    }
}
