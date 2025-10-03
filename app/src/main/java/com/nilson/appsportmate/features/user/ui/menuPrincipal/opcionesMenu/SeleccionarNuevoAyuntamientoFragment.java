package com.nilson.appsportmate.features.user.ui.menuPrincipal.opcionesMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nilson.appsportmate.databinding.FragmentSeleccionarNuevoAyuntamientoBinding;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarNuevoAyuntamientoFragment extends Fragment {

    private FragmentSeleccionarNuevoAyuntamientoBinding binding;
    private SeleccionarNuevoAyuntamientoViewModel vm;

    /** Flag para evitar disparar listeners durante setup/precarga */
    private boolean settingUp = false;

    /** Evita reconstruir adapters si las listas no cambiaron */
    private int lastComCount = -1, lastProvCount = -1, lastCiuCount = -1, lastPueCount = -1;

    /** Solo hacemos la precarga por IDs una vez */
    private boolean didPreload = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSeleccionarNuevoAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(SeleccionarNuevoAyuntamientoViewModel.class);

        setupSpinners();
        setupClicks();
        observeUi();

        vm.cargarComunidades();
    }

    private void setupSpinners() {
        binding.spComunidad.setAdapter(emptyAdapter());
        binding.spProvincia.setAdapter(emptyAdapter());
        binding.spCiudad.setAdapter(emptyAdapter());
        binding.spPueblo.setAdapter(emptyAdapter());

        binding.spComunidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingUp) return;
                vm.onComunidadSelected(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingUp) return;
                vm.onProvinciaSelected(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spCiudad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingUp) return;
                vm.onCiudadSelected(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spPueblo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingUp) return;
                vm.onPuebloSelected(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClicks() {
        binding.btnGuardar.setOnClickListener(v -> vm.guardarSeleccion());
        binding.toolbarSeleccionAyto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void observeUi() {
        vm.ui.observe(getViewLifecycleOwner(), s -> {
            binding.progress.setVisibility(s.loading ? View.VISIBLE : View.GONE);

            if (s.error != null) {
                Toast.makeText(requireContext(), s.error, Toast.LENGTH_SHORT).show();
            }
            if ("Guardado".equals(s.message)) {
                Toast.makeText(requireContext(), "Ayuntamiento actualizado", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
                return;
            }

            // 1) Actualiza adapters SOLO si cambian las listas (por tamaño)
            settingUp = true;
            try {
                if (s.comunidades.size() != lastComCount) {
                    setAdapterNoSelect(binding.spComunidad, mapNombres(s.comunidades));
                    lastComCount = s.comunidades.size();
                }
                if (s.provincias.size() != lastProvCount) {
                    setAdapterNoSelect(binding.spProvincia, mapNombres(s.provincias));
                    lastProvCount = s.provincias.size();
                }
                if (s.ciudades.size() != lastCiuCount) {
                    setAdapterNoSelect(binding.spCiudad, mapNombres(s.ciudades));
                    lastCiuCount = s.ciudades.size();
                }
                if (s.pueblos.size() != lastPueCount) {
                    setAdapterNoSelect(binding.spPueblo, mapPueblosNombres(s.pueblos));
                    lastPueCount = s.pueblos.size();
                }

                // 2) Precarga por IDs (si el ViewModel las establece). Se hace UNA vez.
                if (!didPreload) {
                    if (trySelectById(binding.spComunidad, mapIds(s.comunidades), s.comunidadIdSel)) {
                        // nada
                    }
                    if (trySelectById(binding.spProvincia, mapIds(s.provincias), s.provinciaIdSel)) {
                        // nada
                    }
                    if (trySelectById(binding.spCiudad, mapIds(s.ciudades), s.ciudadIdSel)) {
                        // nada
                    }
                    if (trySelectById(binding.spPueblo, mapPuebloIds(s.pueblos), s.puebloIdSel)) {
                        // nada
                    }
                    didPreload = true; // evita repetir
                }
            } finally {
                settingUp = false;
            }

            // 3) Pinta SOLO los textos de lectura (no tocar spinners aquí)
            binding.etPuebloNombre.setText(s.puebloNombre);
            binding.etAytoNombre.setText(s.ayuntamientoNombre);
        });
    }

    /* Helpers */

    private ArrayAdapter<String> emptyAdapter() {
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
    }

    private void setAdapterNoSelect(Spinner sp, ArrayList<String> data) {
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, data);
        sp.setAdapter(ad);
        // IMPORTANTE: no hacemos setSelection aquí
    }

    private ArrayList<String> mapNombres(List<SeleccionarNuevoAyuntamientoUiState.Opcion> lista) {
        ArrayList<String> r = new ArrayList<>();
        for (SeleccionarNuevoAyuntamientoUiState.Opcion o : lista) r.add(o.nombre);
        return r;
    }

    private ArrayList<String> mapIds(List<SeleccionarNuevoAyuntamientoUiState.Opcion> lista) {
        ArrayList<String> r = new ArrayList<>();
        for (SeleccionarNuevoAyuntamientoUiState.Opcion o : lista) r.add(o.id);
        return r;
    }

    private ArrayList<String> mapPueblosNombres(List<SeleccionarNuevoAyuntamientoUiState.PuebloOpcion> lista) {
        ArrayList<String> r = new ArrayList<>();
        for (SeleccionarNuevoAyuntamientoUiState.PuebloOpcion o : lista) r.add(o.nombre);
        return r;
    }

    private ArrayList<String> mapPuebloIds(List<SeleccionarNuevoAyuntamientoUiState.PuebloOpcion> lista) {
        ArrayList<String> r = new ArrayList<>();
        for (SeleccionarNuevoAyuntamientoUiState.PuebloOpcion o : lista) r.add(o.id);
        return r;
    }

    /** Selecciona por ID si existe en la lista; NO dispara listeners por el guard settingUp */
    private boolean trySelectById(Spinner sp, List<String> ids, String targetId) {
        if (targetId == null || targetId.isEmpty() || ids == null) return false;
        int idx = -1;
        for (int i = 0; i < ids.size(); i++) {
            if (targetId.equals(ids.get(i))) { idx = i; break; }
        }
        if (idx >= 0) {
            sp.setSelection(idx, false); // no animación, y con settingUp=true no dispara lógica
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
