package com.nilson.appsportmate.features.auth.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.ui.auth.login.LoginViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private Task<AuthResult> mockTask;

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 🔥 Desactivar Log.d() durante tests unitarios
        System.setProperty("unitTest", "true");

        // ViewModel con Firebase mockeado
        viewModel = new LoginViewModel(mockAuth, mockDb);

        // Simula signInWithEmailAndPassword()
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockTask);

        // Evita NullPointer en listeners
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);
    }

    @Test
    public void aliasVacio_muestraErrorAlias() {
        viewModel.onLoginClicked("", "123456", mockContext);
        assertEquals("Alias requerido", viewModel.getErrorAlias().getValue());
    }

    @Test
    public void passwordVacia_muestraErrorPassword() {
        viewModel.onLoginClicked("Nilson", "", mockContext);
        assertEquals("Contraseña requerida", viewModel.getErrorPassword().getValue());
    }

    @Test
    public void aliasYPasswordValidos_noHayErrores() {
        viewModel.onLoginClicked("Nilson", "123456", mockContext);

        assertNull(viewModel.getErrorAlias().getValue());
        assertNull(viewModel.getErrorPassword().getValue());
    }

    @Test
    public void loginCorrecto_invocaFirebaseAuth() {
        viewModel.onLoginClicked("Nilson", "123456", mockContext);

        verify(mockAuth).signInWithEmailAndPassword(anyString(), anyString());
    }
}
