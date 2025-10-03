package com.nilson.appsportmate.features.townhall.ui.gestiondeportes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.ActivityGestionDeportesAyuntamientoBinding;

/**
 * Fragment conectado a tu layout "gestionDeportesAyuntamiento" (ids de los inputs y botones).
 * Este nombre de ViewBinding debe coincidir con el nombre real generado por tu layout.
 * Si tu archivo XML se llama "gestion_deportes_ayuntamiento.xml", el binding sería
 * "GestionDeportesAyuntamientoBinding". Ajusta el import si tu nombre difiere.
 */
public class GestionDeportesAyuntamientoFragment extends Fragment {

    private ActivityGestionDeportesAyuntamientoBinding binding;
    private GestionDeportesAyuntamientoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityGestionDeportesAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GestionDeportesAyuntamientoViewModel.class);

        setupPickers();
        setupClicks();
        observeUi();

        viewModel.init();
    }

    private void setupPickers() {
        // Selector de fecha
        binding.etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) -> {
                String mm = String.format("%02d", month + 1);
                String dd = String.format("%02d", dayOfMonth);
                binding.etFecha.setText(year + "-" + mm + "-" + dd);
            }, y, m, d).show();
        });

        // Selector de hora
        binding.etHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hh = c.get(Calendar.HOUR_OF_DAY);
            int mm = c.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(), (picker, hourOfDay, minute) -> {
                String H = String.format("%02d", hourOfDay);
                String M = String.format("%02d", minute);
                binding.etHora.setText(H + ":" + M);
            }, hh, mm, true).show();
        });
    }


    private void setupClicks() {
        // Crear evento
        binding.btnCrearEvento.setOnClickListener(v -> {
            viewModel.crearEvento(
                    txt(binding.etNombreDeporte),
                    txt(binding.etCantidadJugadores),
                    txt(binding.etFecha),
                    txt(binding.etHora),
                    txt(binding.etDescripcionEvento),
                    txt(binding.etReglasEvento),
                    txt(binding.etMateriales),
                    txt(binding.etUrlPueblo)
            );
        });

        // Navegar a gestión (lista) de eventos
        binding.btnGestionEventos.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_gestionDeportesAyuntamientoFragment_to_gestionEventosMasPlazasFragment)
        );

        // Logout
        binding.btnLogout.setOnClickListener(v -> viewModel.logout());
    }

    private void observeUi() {
        viewModel.ui.observe(getViewLifecycleOwner(), state -> {
            // loading → deshabilitar botón crear para evitar dobles clics
            binding.btnCrearEvento.setEnabled(!state.loading);
            binding.btnGestionEventos.setEnabled(!state.loading);
            binding.btnLogout.setEnabled(!state.loading);

            if (state.message != null) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
                if ("Evento creado".equals(state.message)) {
                    // Limpia campos básicos tras crear
                    binding.etNombreDeporte.setText("");
                    binding.etCantidadJugadores.setText("");
                    // opcional: deja fecha/hora/desc si quieres
                }
            }
            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.navigateToGestionEventos.observe(getViewLifecycleOwner(), go -> {
            if (go != null && go) {
                // Si ya tienes un destino en el NavGraph:
                // Navigation.findNavController(requireView()).navigate(R.id.action_gestionDeportes_to_gestionEventosListado);
                // De momento, mostramos un mensaje:
                Toast.makeText(requireContext(), "Abrir pantalla de gestión/listado de eventos", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.navigateAfterLogout.observe(getViewLifecycleOwner(), go -> {
            if (go != null && go) {
                // Vuelve a Auth/Login según tu NavGraph:
                // Navigation.findNavController(requireView()).navigate(R.id.action_gestionDeportes_to_authFragment);
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.authFragment);
            }
        });
    }

    private String txt(@Nullable android.widget.TextView tv) {
        if (tv == null || tv.getText() == null) return "";
        return tv.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
