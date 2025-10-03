package com.nilson.appsportmate.ui.auth.signUp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
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
import com.nilson.appsportmate.databinding.FragmentSignUpBinding;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.MainActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private SignUpViewModel viewModel;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DocumentSnapshot> comunidadesDocs = new ArrayList<>();
    private final List<DocumentSnapshot> provinciasDocs  = new ArrayList<>();
    private final List<DocumentSnapshot> ciudadesDocs    = new ArrayList<>();
    private final List<DocumentSnapshot> pueblosDocs     = new ArrayList<>();

    private String comunidadIdSel, provinciaIdSel, ciudadIdSel, ayuntamientoIdSel;
    private boolean aliasUpdating = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // ===== Alias =====
        binding.etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (aliasUpdating) return;
                aliasUpdating = true;
                String input = s.toString();
                if (!input.isEmpty()) {
                    String fixed = input.substring(0,1).toUpperCase() + input.substring(1);
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

        // ===== Toggle UI por rol =====
        binding.rgRol.setOnCheckedChangeListener((g, id) -> {
            RadioButton rb = binding.getRoot().findViewById(id);
            boolean esUsuario = (rb == null) || "usuario".equals(String.valueOf(rb.getTag()));
            binding.layoutApellidos.setVisibility(esUsuario ? View.VISIBLE : View.GONE);
            binding.layoutNumero.setVisibility(esUsuario ? View.GONE : View.VISIBLE);
            binding.blocUsuario.setVisibility(esUsuario ? View.VISIBLE : View.GONE);
            binding.blocAyuntamiento.setVisibility(esUsuario ? View.GONE : View.VISIBLE);
        });

        // ===== Botón registrar =====
        binding.btnRegistrar.setOnClickListener(v ->
                viewModel.onRegisterClicked(
                        requireContext(),
                        getTxt(binding.etAlias),
                        getTxt(binding.etPassword),
                        getTxt(binding.etPassword2),
                        getTxt(binding.etNombre),
                        getTxt(binding.etApellidos),
                        getTxt(binding.etDescripcionEvento),   // comunidadNombre
                        getTxt(binding.etReglasEvento),        // provinciaNombre
                        getTxt(binding.etMateriales),          // ciudadNombre
                        binding.blocUsuario.getVisibility() == View.VISIBLE
                                ? getTxt(binding.etPuebloUsuario)
                                : getTxt(binding.etPuebloAyto),
                        getTxt(binding.etNumero),              // razón social
                        getRolLower(),
                        getAyuntamientoSeleccionadoId(),
                        comunidadIdSel == null ? "" : comunidadIdSel,
                        provinciaIdSel == null ? "" : provinciaIdSel,
                        ciudadIdSel == null ? "" : ciudadIdSel
                )
        );

        // ===== Observers =====
        viewModel.getEAlias().observe(getViewLifecycleOwner(), e -> { if (e != null) { binding.etAlias.setError(e); binding.etAlias.requestFocus(); }});
        viewModel.getEPassword().observe(getViewLifecycleOwner(), e -> { if (e != null) { binding.etPassword.setError(e); binding.etPassword.requestFocus(); }});
        viewModel.getENombre().observe(getViewLifecycleOwner(), e -> { if (e != null) { binding.etNombre.setError(e); binding.etNombre.requestFocus(); }});
        viewModel.getEApellidos().observe(getViewLifecycleOwner(), e -> { if (e != null) { binding.etApellidos.setError(e); binding.etApellidos.requestFocus(); }});
        viewModel.getERazon().observe(getViewLifecycleOwner(), e -> { if (e != null) { binding.etNumero.setError(e); binding.etNumero.requestFocus(); }});

        // Mensajes
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && isAdded()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.consumeMessage();
            }
        });

        // ===== Navegación =====
        viewModel.getNavAyto().observe(getViewLifecycleOwner(), go -> {
            if (go != null && go && isAdded()) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.gestionDeportesAyuntamientoFragment);
                viewModel.consumeNavAyto();
            }
        });

        viewModel.getNavUser().observe(getViewLifecycleOwner(), go -> {
            if (go != null && go && isAdded()) {
                startActivity(new Intent(requireContext(), MainActivity.class));
                requireActivity().finish();
                viewModel.consumeNavUser();
            }
        });

        // ===== Carga en cascada =====
        configurarSpinners();
        cargarComunidades();
    }

    // ===== Spinners =====
    private void configurarSpinners() {
        binding.spComunidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= comunidadesDocs.size()) return;
                DocumentSnapshot d = comunidadesDocs.get(position);
                comunidadIdSel = d.getId();
                binding.etDescripcionEvento.setText(safe(d.getString("nombre")));
                cargarProvincias(comunidadIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= provinciasDocs.size()) return;
                DocumentSnapshot d = provinciasDocs.get(position);
                provinciaIdSel = d.getId();
                binding.etReglasEvento.setText(safe(d.getString("nombre")));
                cargarCiudades(provinciaIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spCiudad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= ciudadesDocs.size()) return;
                DocumentSnapshot d = ciudadesDocs.get(position);
                ciudadIdSel = d.getId();
                binding.etMateriales.setText(safe(d.getString("nombre")));
                if (binding.blocUsuario.getVisibility() == View.VISIBLE) {
                    cargarPueblosPorCiudad(ciudadIdSel);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spPuebloUsuario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= pueblosDocs.size()) return;
                DocumentSnapshot d = pueblosDocs.get(position);

                // Nombre del pueblo
                binding.etPuebloUsuario.setText(safe(d.getString("nombre")));

                // 1) Si el pueblo ya trae el nombre del ayuntamiento denormalizado, úsalo directo
                String aytoNombreDenorm = trimOrNull(d.getString("ayuntamientoNombre"));
                String aytoId = trimOrNull(d.getString("ayuntamientoId"));
                String creadorUid = trimOrNull(d.getString("createdByUid")); // compatibilidad

                if (aytoId == null) aytoId = creadorUid; // fallback a createdByUid
                ayuntamientoIdSel = (aytoId != null) ? aytoId : "";

                if (aytoNombreDenorm != null) {
                    binding.etAyuntamientoUsuario.setText(aytoNombreDenorm);
                    return;
                }

                // 2) Si no hay nombre denormalizado, resolver leyendo el doc de ayuntamientos
                if (ayuntamientoIdSel == null || ayuntamientoIdSel.isEmpty()) {
                    binding.etAyuntamientoUsuario.setText("");
                } else {
                    FirebaseFirestore.getInstance()
                            .collection("ayuntamientos").document(ayuntamientoIdSel).get()
                            .addOnSuccessListener(doc -> {
                                // PRIORIDAD: nombre -> razonSocial -> alias
                                String nombreAyto = trimOrNull(doc.getString("nombre"));
                                if (nombreAyto == null) nombreAyto = trimOrNull(doc.getString("razonSocial"));
                                if (nombreAyto == null) nombreAyto = trimOrNull(doc.getString("alias"));

                                binding.etAyuntamientoUsuario.setText(
                                        nombreAyto != null ? nombreAyto : "(Ayuntamiento)"
                                );
                            })
                            .addOnFailureListener(e -> binding.etAyuntamientoUsuario.setText("(Ayuntamiento)"));
                }
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
                        nombres.add(safe(d.getString("nombre")));
                    }
                    binding.spComunidad.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, nombres));
                    if (!comunidadesDocs.isEmpty()) binding.spComunidad.setSelection(0);
                })
                .addOnFailureListener(e -> toast("Error cargando comunidades"));
    }

    private void cargarProvincias(String comunidadId) {
        Query q = db.collection("provincias").whereEqualTo("comunidadId", comunidadId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            provinciasDocs.clear();
            ArrayList<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                provinciasDocs.add(d);
                nombres.add(safe(d.getString("nombre")));
            }
            binding.spProvincia.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!provinciasDocs.isEmpty()) binding.spProvincia.setSelection(0);
        }).addOnFailureListener(e -> toast("Error cargando provincias"));
    }

    private void cargarCiudades(String provinciaId) {
        Query q = db.collection("ciudades").whereEqualTo("provinciaId", provinciaId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            ciudadesDocs.clear();
            ArrayList<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                ciudadesDocs.add(d);
                nombres.add(safe(d.getString("nombre")));
            }
            binding.spCiudad.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!ciudadesDocs.isEmpty()) binding.spCiudad.setSelection(0);
        }).addOnFailureListener(e -> toast("Error cargando ciudades"));
    }

    private void cargarPueblosPorCiudad(String ciudadId) {
        Query q = db.collection("pueblos").whereEqualTo("ciudadId", ciudadId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            pueblosDocs.clear();
            ArrayList<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                pueblosDocs.add(d);
                nombres.add(safe(d.getString("nombre")));
            }
            binding.spPuebloUsuario.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!pueblosDocs.isEmpty()) binding.spPuebloUsuario.setSelection(0);
            else {
                binding.etPuebloUsuario.setText("");
                binding.etAyuntamientoUsuario.setText("");
                ayuntamientoIdSel = null;
            }
        }).addOnFailureListener(e -> toast("Error cargando pueblos"));
    }

    private String getTxt(com.google.android.material.textfield.TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private String getRolLower() {
        int checkedId = binding.rgRol.getCheckedRadioButtonId();
        if (checkedId == -1) return "usuario";
        RadioButton rb = binding.getRoot().findViewById(checkedId);
        return rb != null ? String.valueOf(rb.getTag()).trim().toLowerCase() : "usuario";
    }

    private String getAyuntamientoSeleccionadoId() {
        return ayuntamientoIdSel == null ? "" : ayuntamientoIdSel;
    }

    private void toast(String msg) { if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
