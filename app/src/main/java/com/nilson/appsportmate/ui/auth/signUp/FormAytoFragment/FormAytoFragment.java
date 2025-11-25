package com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nilson.appsportmate.databinding.FragmentFormAyuntamientoBinding;

public class FormAytoFragment extends Fragment {

    private FragmentFormAyuntamientoBinding binding;
    private FormAytoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFormAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(FormAytoViewModel.class);

        setupListeners();
        setupObservers();
        cargarSpinnersEjemplo();
    }

    private void setupListeners() {

        binding.etAlias.addTextChangedListener(textWatcher(s -> viewModel.setAlias(s)));
        binding.etRazonSocial.addTextChangedListener(textWatcher(s -> viewModel.setRazonSocial(s)));
        binding.etPuebloNuevo.addTextChangedListener(textWatcher(s -> viewModel.setPuebloNuevo(s)));

        binding.etPassword.addTextChangedListener(textWatcher(s -> viewModel.setPassword(s)));
        binding.etPassword2.addTextChangedListener(textWatcher(s -> viewModel.setPassword2(s)));

        binding.btnRegistrar.setOnClickListener(v -> viewModel.registrar());
    }

    private TextWatcher textWatcher(TextConsumer consumer) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { consumer.accept(s.toString()); }
        };
    }

    private interface TextConsumer { void accept(String text); }

    private void setupObservers() {
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {

            if (state.errorMessage != null) {
                Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show();
            }

            if (state.registroExitoso) {
                Toast.makeText(requireContext(), "Ayuntamiento registrado", Toast.LENGTH_LONG).show();
                // TODO navegar a siguiente pantalla
            }

            binding.btnRegistrar.setEnabled(!state.loading);
        });
    }

    private void cargarSpinnersEjemplo() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Elemento 1", "Elemento 2"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spComunidad.setAdapter(adapter);
        binding.spProvincia.setAdapter(adapter);
        binding.spCiudad.setAdapter(adapter);
    }
}

