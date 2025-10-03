package com.nilson.appsportmate.features.townhall.ui.formularioayuntamiento;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nilson.appsportmate.databinding.ActivityFormularioAyuntamientoBinding;

/**
 * Fragment conectado al layout que ya tienes (formularioAyuntamiento).
 * IDs usados: etNombre, etNumero, etDescripcionEvento, etReglasEvento,
 * etMateriales, etUrlPueblo, etLocalidad, etUid, btnGuardar
 */
public class FormularioAyuntamientoFragment extends Fragment {

    private ActivityFormularioAyuntamientoBinding binding;
    private FormularioAyuntamientoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityFormularioAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FormularioAyuntamientoViewModel.class);

        setupClicks();
        observeUi();

        viewModel.init(); // Pone el UID (si hay sesión)
    }

    private void setupClicks() {
        binding.btnGuardar.setOnClickListener(v -> {
            String nombre       = getTxt(binding.etNombre);
            String razonSocial  = getTxt(binding.etNumero);
            String comunidad    = getTxt(binding.etDescripcionEvento);
            String provincia    = getTxt(binding.etReglasEvento);
            String ciudad       = getTxt(binding.etMateriales);
            String pueblo       = getTxt(binding.etUrlPueblo);
            String localidad    = getTxt(binding.etLocalidad);

            viewModel.guardar(nombre, razonSocial, comunidad, provincia, ciudad, pueblo, localidad);
        });
    }

    private void observeUi() {
        viewModel.ui.observe(getViewLifecycleOwner(), state -> {
            // Mostrar UID (solo lectura) si lo hay
            if (state.uid != null) {
                binding.etUid.setText(state.uid);
            }

            // Loading: deshabilitar el botón para evitar dobles clics
            binding.btnGuardar.setEnabled(!state.loading);

            if (state.message != null) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
            }
            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTxt(@Nullable android.widget.TextView tv) {
        if (tv == null || tv.getText() == null) return "";
        return tv.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
