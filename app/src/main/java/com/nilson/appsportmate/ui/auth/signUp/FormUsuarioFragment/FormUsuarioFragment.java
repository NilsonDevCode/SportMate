package com.nilson.appsportmate.ui.auth.signUp.FormUsuarioFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;


import com.nilson.appsportmate.databinding.FragmentFormUsuarioBinding;

public class FormUsuarioFragment extends Fragment {

    private FragmentFormUsuarioBinding binding;
    private FormUsuarioViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFormUsuarioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FormUsuarioViewModel.class);

        setupListeners();
        setupObservers();
        cargarSpinnersEjemplo();
        }

    private void setupListeners() {

        binding.etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.setAlias(s.toString());
            }
        });

        binding.etNombre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.setNombre(s.toString());
            }
        });

        binding.etApellidos.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.setApellidos(s.toString());
            }
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.setPassword(s.toString());
            }
        });

        binding.etPassword2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.setPassword2(s.toString());
            }
        });

        binding.btnRegistrar.setOnClickListener(v -> viewModel.registrarUsuario());
    }


    private void setupObservers() {
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {

            if (state.errorMessage != null) {
                Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show();
            }

            if (state.registroExitoso) {
                Toast.makeText(requireContext(), "Usuario registrado correctamente", Toast.LENGTH_LONG).show();
                // aquí puedes navegar al siguiente fragment
            }

            binding.btnRegistrar.setEnabled(!state.loading);
        });
    }

    private void cargarSpinnersEjemplo() {
        // Esto es solo temporal: remplazar por tus datos reales
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Elemento 1", "Elemento 2"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spComunidad.setAdapter(adapter);
        binding.spProvincia.setAdapter(adapter);
        binding.spCiudad.setAdapter(adapter);
        binding.spPueblo.setAdapter(adapter);
    }
}

