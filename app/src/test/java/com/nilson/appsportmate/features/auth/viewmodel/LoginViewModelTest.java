package com.nilson.appsportmate.features.auth.viewmodel;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.nilson.appsportmate.ui.auth.login.LoginViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test de CAJA BLANCA: valida la lógica interna del ViewModel sin Android real.
 */
public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LoginViewModel();
    }

    @Test
    public void aliasVacio_muestraErrorAlias() {
        viewModel.onLoginClicked("", "123456", mockContext);
        String error = viewModel.getErrorAlias().getValue();
        assertEquals("Alias requerido", error);
    }

    @Test
    public void passwordVacia_muestraErrorPassword() {
        viewModel.onLoginClicked("Nilson", "", mockContext);
        String error = viewModel.getErrorPassword().getValue();
        assertEquals("Contraseña requerida", error);
    }

    @Test
    public void aliasYPasswordValidos_noHayErrores() {
        viewModel.onLoginClicked("Nilson", "123456", mockContext);
        assertNull(viewModel.getErrorAlias().getValue());
        assertNull(viewModel.getErrorPassword().getValue());
    }
}
