package com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentFormAyuntamientoBinding;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.MainActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FormAytoFragment extends Fragment {

    private FragmentFormAyuntamientoBinding binding;
    private FormAytoViewModel viewModel;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DocumentSnapshot> comunidadesDocs = new ArrayList<>();
    private final List<DocumentSnapshot> provinciasDocs  = new ArrayList<>();
    private final List<DocumentSnapshot> ciudadesDocs    = new ArrayList<>();

    private String comunidadIdSel, provinciaIdSel, ciudadIdSel;
    private boolean aliasUpdating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFormAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FormAytoViewModel.class);

        configurarAlias();
        configurarSpinners();
        cargarComunidades();

        binding.btnRegistrar.setOnClickListener(v ->
                viewModel.onRegisterClicked(
                        requireContext(),
                        getTxt(binding.etAlias),
                        getTxt(binding.etPassword),
                        getTxt(binding.etPassword2),
                        getTxt(binding.etNombre),
                        "", // apellidos eliminado
                        getTxt(binding.etDescripcionEvento),
                        getTxt(binding.etReglasEvento),
                        getTxt(binding.etMateriales),
                        getTxt(binding.etPuebloAyto), // pueblo AYUNTAMIENTO
                        getTxt(binding.etNumero),     // razón social
                        "ayuntamiento", // rol fijo
                        "", // ayuntamientoIdSel no aplica aquí
                        comunidadIdSel,
                        provinciaIdSel,
                        ciudadIdSel
                )
        );

        // Observers
        viewModel.getEAlias().observe(getViewLifecycleOwner(), e -> {
            if (e != null) binding.etAlias.setError(e);
        });

        viewModel.getEPassword().observe(getViewLifecycleOwner(), e -> {
            if (e != null) binding.layoutPassword.setError(e);
        });

        viewModel.getENombre().observe(getViewLifecycleOwner(), e -> {
            if (e != null) binding.etNombre.setError(e);
        });

        viewModel.getERazon().observe(getViewLifecycleOwner(), e -> {
            if (e != null) binding.etNumero.setError(e);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.consumeMessage();
            }
        });

        // Navegación
        viewModel.getNavAyto().observe(getViewLifecycleOwner(), go -> {
            if (go != null && go) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.gestionDeportesAyuntamientoFragment);
                viewModel.consumeNavAyto();
            }
        });
    }

    private void configurarAlias() {
        binding.etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (aliasUpdating) return;
                aliasUpdating = true;

                String input = s.toString();
                if (!input.isEmpty()) {
                    String fixed = input.substring(0, 1).toUpperCase() + input.substring(1);
                    if (!fixed.equals(input)) {
                        binding.etAlias.setText(fixed);
                        binding.etAlias.setSelection(fixed.length());
                    }
                }

                String err = AuthAliasHelper.getAliasValidationError(getTxt(binding.etAlias));
                binding.etAlias.setError(err);
                aliasUpdating = false;
            }
        });
    }

    private void configurarSpinners() {
        binding.spComunidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= comunidadesDocs.size()) return;
                DocumentSnapshot d = comunidadesDocs.get(position);
                comunidadIdSel = d.getId();
                binding.etDescripcionEvento.setText(d.getString("nombre"));
                cargarProvincias(comunidadIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= provinciasDocs.size()) return;
                DocumentSnapshot d = provinciasDocs.get(position);
                provinciaIdSel = d.getId();
                binding.etReglasEvento.setText(d.getString("nombre"));
                cargarCiudades(provinciaIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spCiudad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= ciudadesDocs.size()) return;
                DocumentSnapshot d = ciudadesDocs.get(position);
                ciudadIdSel = d.getId();
                binding.etMateriales.setText(d.getString("nombre"));
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarComunidades() {
        db.collection("comunidades").orderBy("nombre").get()
                .addOnSuccessListener(snap -> {
                    comunidadesDocs.clear();
                    ArrayList<String> nombres = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        comunidadesDocs.add(d);
                        nombres.add(d.getString("nombre"));
                    }
                    binding.spComunidad.setAdapter(new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            nombres
                    ));
                });
    }

    private void cargarProvincias(String comunidadId) {
        db.collection("provincias")
                .whereEqualTo("comunidadId", comunidadId)
                .orderBy("nombre")
                .get()
                .addOnSuccessListener(snap -> {
                    provinciasDocs.clear();
                    ArrayList<String> nombres = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        provinciasDocs.add(d);
                        nombres.add(d.getString("nombre"));
                    }
                    binding.spProvincia.setAdapter(new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            nombres
                    ));
                });
    }

    private void cargarCiudades(String provinciaId) {
        db.collection("ciudades")
                .whereEqualTo("provinciaId", provinciaId)
                .orderBy("nombre")
                .get()
                .addOnSuccessListener(snap -> {
                    ciudadesDocs.clear();
                    ArrayList<String> nombres = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        ciudadesDocs.add(d);
                        nombres.add(d.getString("nombre"));
                    }
                    binding.spCiudad.setAdapter(new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            nombres
                    ));
                });
    }

    private String getTxt(com.google.android.material.textfield.TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
