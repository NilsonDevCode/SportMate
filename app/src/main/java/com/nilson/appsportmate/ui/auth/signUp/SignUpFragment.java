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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentSignUpBinding;
import com.nilson.appsportmate.features.auth.presentacion.RegisterPresenter;
import com.nilson.appsportmate.features.auth.presentacion.RegisterView;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.features.townhall.ui.GestionDeportesAyuntamientoActivity;
import com.nilson.appsportmate.MainActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment implements RegisterView {

    private FragmentSignUpBinding binding;
    private RegisterPresenter presenter;

    private TextInputEditText etAlias, etPassword, etPassword2, etNombre, etApellidos,
            etComunidad, etProvincia, etCiudad, etPuebloUsuario, etNumero, etAyuntamientoUsuario, etPuebloAyto;
    private TextInputLayout layoutNombre, layoutApellidos, layoutNumero;
    private TextView tvTituloForm;
    private Spinner spComunidad, spProvincia, spCiudad, spPuebloUsuario;
    private View blocUsuario, blocAyuntamiento;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<DocumentSnapshot> comunidadesDocs = new ArrayList<>();
    private final List<DocumentSnapshot> provinciasDocs  = new ArrayList<>();
    private final List<DocumentSnapshot> ciudadesDocs    = new ArrayList<>();
    private final List<DocumentSnapshot> pueblosDocs     = new ArrayList<>();

    private String comunidadIdSel, provinciaIdSel, ciudadIdSel, puebloIdSel, ayuntamientoIdSel;

    private boolean aliasUpdating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tvTituloForm          = binding.tvTituloForm;
        etAlias               = binding.etAlias;
        etPassword            = binding.etPassword;
        etPassword2           = binding.etPassword2;
        etNombre              = binding.etNombre;
        etApellidos           = binding.etApellidos;
        etComunidad           = binding.etDescripcionEvento;
        etProvincia           = binding.etReglasEvento;
        etCiudad              = binding.etMateriales;
        etNumero              = binding.etNumero;

        layoutNombre          = binding.layoutNombre;
        layoutApellidos       = binding.layoutApellidos;
        layoutNumero          = binding.layoutNumero;

        spComunidad           = binding.spComunidad;
        spProvincia           = binding.spProvincia;
        spCiudad              = binding.spCiudad;
        spPuebloUsuario       = binding.spPuebloUsuario;

        blocUsuario           = binding.blocUsuario;
        blocAyuntamiento      = binding.blocAyuntamiento;

        etPuebloUsuario       = binding.etPuebloUsuario;
        etAyuntamientoUsuario = binding.etAyuntamientoUsuario;
        etPuebloAyto          = binding.etPuebloAyto;

        presenter = new RegisterPresenter(this, requireContext());

        configurarValidacionesTiempoReal();
        configurarSpinners();

        binding.rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = binding.getRoot().findViewById(checkedId);
            String rolRaw = rb != null ? String.valueOf(rb.getTag()) : "usuario";
            applyRolUI(rolRaw);
        });

        binding.btnRegistrar.setOnClickListener(v -> presenter.onRegisterClicked());

        // Cargar comunidades al inicio
        cargarComunidades();
    }

    // ====== Spinners y cargas ======

    private void configurarSpinners() {
        spComunidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= comunidadesDocs.size()) return;
                DocumentSnapshot d = comunidadesDocs.get(position);
                comunidadIdSel = d.getId();
                etComunidad.setText(d.getString("nombre"));
                cargarProvincias(comunidadIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= provinciasDocs.size()) return;
                DocumentSnapshot d = provinciasDocs.get(position);
                provinciaIdSel = d.getId();
                etProvincia.setText(d.getString("nombre"));
                cargarCiudades(provinciaIdSel);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spCiudad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= ciudadesDocs.size()) return;
                DocumentSnapshot d = ciudadesDocs.get(position);
                ciudadIdSel = d.getId();
                etCiudad.setText(d.getString("nombre"));
                if (blocUsuario.getVisibility() == View.VISIBLE) {
                    cargarPueblosPorCiudad(ciudadIdSel);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spPuebloUsuario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= pueblosDocs.size()) return;
                DocumentSnapshot d = pueblosDocs.get(position);
                puebloIdSel = d.getId();
                etPuebloUsuario.setText(d.getString("nombre"));

                String creadorUid = d.getString("createdByUid");
                ayuntamientoIdSel = creadorUid != null ? creadorUid : "";
                if (ayuntamientoIdSel == null || ayuntamientoIdSel.isEmpty()) {
                    etAyuntamientoUsuario.setText("");
                } else {
                    db.collection("ayuntamientos").document(ayuntamientoIdSel)
                            .get()
                            .addOnSuccessListener(doc -> {
                                String nombreAyto = doc.getString("razonSocial");
                                if (nombreAyto == null || nombreAyto.isEmpty()) {
                                    nombreAyto = doc.getString("nombre");
                                }
                                etAyuntamientoUsuario.setText(nombreAyto != null ? nombreAyto : "(Ayuntamiento)");
                            })
                            .addOnFailureListener(e -> etAyuntamientoUsuario.setText("(Ayuntamiento)"));
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarComunidades() {
        db.collection("comunidades").orderBy("nombre").get().addOnSuccessListener(snap -> {
            comunidadesDocs.clear();
            List<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                comunidadesDocs.add(d);
                nombres.add(d.getString("nombre"));
            }
            spComunidad.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!comunidadesDocs.isEmpty()) spComunidad.setSelection(0);
        }).addOnFailureListener(e -> toast("Error cargando comunidades"));
    }

    private void cargarProvincias(String comunidadId) {
        Query q = db.collection("provincias").whereEqualTo("comunidadId", comunidadId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            provinciasDocs.clear();
            List<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                provinciasDocs.add(d);
                nombres.add(d.getString("nombre"));
            }
            spProvincia.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!provinciasDocs.isEmpty()) spProvincia.setSelection(0);
        }).addOnFailureListener(e -> toast("Error cargando provincias"));
    }

    private void cargarCiudades(String provinciaId) {
        Query q = db.collection("ciudades").whereEqualTo("provinciaId", provinciaId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            ciudadesDocs.clear();
            List<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                ciudadesDocs.add(d);
                nombres.add(d.getString("nombre"));
            }
            spCiudad.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!ciudadesDocs.isEmpty()) spCiudad.setSelection(0);
        }).addOnFailureListener(e -> toast("Error cargando ciudades"));
    }

    private void cargarPueblosPorCiudad(String ciudadId) {
        Query q = db.collection("pueblos").whereEqualTo("ciudadId", ciudadId).orderBy("nombre");
        q.get().addOnSuccessListener(snap -> {
            pueblosDocs.clear();
            List<String> nombres = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                pueblosDocs.add(d);
                nombres.add(d.getString("nombre"));
            }
            spPuebloUsuario.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!pueblosDocs.isEmpty()) spPuebloUsuario.setSelection(0);
            else {
                etPuebloUsuario.setText("");
                etAyuntamientoUsuario.setText("");
                ayuntamientoIdSel = null;
            }
        }).addOnFailureListener(e -> toast("Error cargando pueblos"));
    }

    private void configurarValidacionesTiempoReal() {
        etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (aliasUpdating) return;
                aliasUpdating = true;
                String input = s.toString();
                if (!input.isEmpty()) {
                    String fixed = input.substring(0,1).toUpperCase() + input.substring(1);
                    if (!fixed.equals(input)) {
                        etAlias.setText(fixed);
                        etAlias.setSelection(fixed.length());
                    }
                }
                String err = AuthAliasHelper.getAliasValidationError(etAlias.getText() == null ? "" : etAlias.getText().toString());
                etAlias.setError(err);
                aliasUpdating = false;
            }
        });

        etPassword2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String p1 = etPassword.getText() == null ? "" : etPassword.getText().toString();
                String p2 = s.toString();
                etPassword2.setError(p2.equals(p1) ? null : "Las contraseñas no coinciden");
            }
        });
    }

    private void applyRolUI(String rolRaw) {
        boolean esUsuario = "usuario".equals(rolRaw);
        tvTituloForm.setText(esUsuario ? "Crear cuenta" : "Formulario Ayuntamiento");
        layoutApellidos.setVisibility(esUsuario ? View.VISIBLE : View.GONE);
        layoutNumero.setVisibility(esUsuario ? View.GONE : View.VISIBLE);
        blocUsuario.setVisibility(esUsuario ? View.VISIBLE : View.GONE);
        blocAyuntamiento.setVisibility(esUsuario ? View.GONE : View.VISIBLE);
    }

    // ===== RegisterView =====
    @Override public String getAliasInput() { return text(etAlias); }
    @Override public String getPassword1() { return text(etPassword); }
    @Override public String getPassword2() { return text(etPassword2); }
    @Override public String getNombre()    { return text(etNombre); }
    @Override public String getApellidos() { return text(etApellidos); }
    @Override public String getComunidad() { return text(etComunidad); }
    @Override public String getProvincia() { return text(etProvincia); }
    @Override public String getCiudad()    { return text(etCiudad); }

    @Override public String getPueblo() {
        // usuario -> etPuebloUsuario ; ayuntamiento -> etPuebloAyto
        int checkedId = binding.rgRol.getCheckedRadioButtonId();
        RadioButton rb = binding.getRoot().findViewById(checkedId);
        String rol = rb != null ? String.valueOf(rb.getTag()) : "usuario";
        if ("ayuntamiento".equals(rol)) {
            return text(etPuebloAyto);
        } else {
            return text(etPuebloUsuario);
        }
    }

    @Override public String getRazonSocial() { return text(etNumero); }

    @Override public String getRol() {
        int checkedId = binding.rgRol.getCheckedRadioButtonId();
        if (checkedId == -1) return "usuario";
        RadioButton rb = binding.getRoot().findViewById(checkedId);
        return rb != null ? String.valueOf(rb.getTag()).trim().toLowerCase() : "usuario";
    }

    @Override public String getAyuntamientoSeleccionadoId() {
        return ayuntamientoIdSel == null ? "" : ayuntamientoIdSel;
    }

    // IDs seleccionados (requeridos por tu presenter)
    @Override public String getComunidadIdSel() { return comunidadIdSel == null ? "" : comunidadIdSel; }
    @Override public String getProvinciaIdSel() { return provinciaIdSel == null ? "" : provinciaIdSel; }
    @Override public String getCiudadIdSel()    { return ciudadIdSel == null ? "" : ciudadIdSel; }

    @Override public void mostrarErrorAlias(String msg) { etAlias.setError(msg); etAlias.requestFocus(); }
    @Override public void mostrarErrorPassword(String msg) { etPassword.setError(msg); etPassword.requestFocus(); }
    @Override public void mostrarErrorNombre(String msg) { etNombre.setError(msg); etNombre.requestFocus(); }
    @Override public void mostrarErrorApellidos(String msg) { etApellidos.setError(msg); etApellidos.requestFocus(); }
    @Override public void mostrarErrorRazonSocial(String msg) { etNumero.setError(msg); etNumero.requestFocus(); }

    @Override public void mostrarMensaje(String msg) { toast(msg); }

    @Override public void navegarAyuntamiento() {
        if (!isAdded()) return;
        startActivity(new Intent(requireContext(), GestionDeportesAyuntamientoActivity.class));
        requireActivity().finish();
    }

    @Override public void navegarUsuario() {
        if (!isAdded()) return;
        startActivity(new Intent(requireContext(), MainActivity.class));
        requireActivity().finish();
    }

    // ===== Helpers =====
    private String text(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
