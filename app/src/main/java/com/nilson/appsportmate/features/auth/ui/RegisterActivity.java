package com.nilson.appsportmate.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.auth.presentacion.RegisterPresenter;
import com.nilson.appsportmate.features.auth.presentacion.RegisterView;
import com.nilson.appsportmate.features.townhall.ui.GestionDeportesAyuntamientoActivity;
import com.nilson.appsportmate.MainActivity;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements RegisterView {

    private TextInputEditText etAlias, etPassword, etPassword2, etNombre, etApellidos, etComunidad,
            etProvincia, etCiudad, etPueblo, etNumero, etPuebloUsuario, etAyuntamientoUsuario, etPuebloAyto;
    private TextInputLayout layoutNombre, layoutApellidos, layoutNumero;
    private RadioGroup rgRol;
    private MaterialButton btnRegistrar;
    private Spinner spinnerAyuntamientos; // oculto
    private TextView tvTituloForm;

    // Spinners
    private Spinner spComunidad, spProvincia, spCiudad, spPuebloUsuario;

    // Bloques
    private View blocUsuario, blocAyuntamiento;

    private RegisterPresenter presenter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Soporte
    private final List<DocumentSnapshot> comunidadesDocs = new ArrayList<>();
    private final List<DocumentSnapshot> provinciasDocs = new ArrayList<>();
    private final List<DocumentSnapshot> ciudadesDocs = new ArrayList<>();
    private final List<DocumentSnapshot> pueblosDocs = new ArrayList<>();

    // IDs seleccionados
    private String comunidadIdSel, provinciaIdSel, ciudadIdSel, puebloIdSel, ayuntamientoIdSel;

    private boolean aliasUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sign_up);

        tvTituloForm         = findViewById(R.id.tvTituloForm);
        etAlias              = findViewById(R.id.etAlias);
        etPassword           = findViewById(R.id.etPassword);
        etPassword2          = findViewById(R.id.etPassword2);
        etNombre             = findViewById(R.id.etNombre);
        etApellidos          = findViewById(R.id.etApellidos);
        etComunidad          = findViewById(R.id.etDescripcionEvento);
        etProvincia          = findViewById(R.id.etReglasEvento);
        etCiudad             = findViewById(R.id.etMateriales);
        // etPueblo             = findViewById(R.id.etUrlPueblo);
        etNumero             = findViewById(R.id.etNumero);
        layoutNombre         = findViewById(R.id.layoutNombre);
        layoutApellidos      = findViewById(R.id.layoutApellidos);
        layoutNumero         = findViewById(R.id.layoutNumero);
        rgRol                = findViewById(R.id.rgRol);
        btnRegistrar         = findViewById(R.id.btnRegistrar);
        spinnerAyuntamientos = findViewById(R.id.spinnerAyuntamientos);

        spComunidad          = findViewById(R.id.spComunidad);
        spProvincia          = findViewById(R.id.spProvincia);
        spCiudad             = findViewById(R.id.spCiudad);
        spPuebloUsuario      = findViewById(R.id.spPuebloUsuario);

        blocUsuario          = findViewById(R.id.blocUsuario);
        blocAyuntamiento     = findViewById(R.id.blocAyuntamiento);

        etPuebloUsuario      = findViewById(R.id.etPuebloUsuario);
        etAyuntamientoUsuario= findViewById(R.id.etAyuntamientoUsuario);
        etPuebloAyto         = findViewById(R.id.etPuebloAyto);

        presenter = new RegisterPresenter(this, this);

        configurarValidacionesTiempoReal();
        configurarSpinners();

        rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            String rolRaw = rb != null ? String.valueOf(rb.getTag()) : "usuario";
            applyRolUI(rolRaw);
        });

        btnRegistrar.setOnClickListener(v -> presenter.onRegisterClicked());

        // Cargar comunidades
        cargarComunidades();
    }

    private void configurarSpinners() {
        spComunidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= comunidadesDocs.size()) return;
                DocumentSnapshot d = comunidadesDocs.get(position);
                comunidadIdSel = d.getId(); // ID documento
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
                if (ayuntamientoIdSel.isEmpty()) {
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
            spComunidad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!comunidadesDocs.isEmpty()) spComunidad.setSelection(0);
        }).addOnFailureListener(e -> Toast.makeText(this, "Error cargando comunidades", Toast.LENGTH_SHORT).show());
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
            spProvincia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!provinciasDocs.isEmpty()) spProvincia.setSelection(0);
        }).addOnFailureListener(e -> Toast.makeText(this, "Error cargando provincias", Toast.LENGTH_SHORT).show());
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
            spCiudad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!ciudadesDocs.isEmpty()) spCiudad.setSelection(0);
        }).addOnFailureListener(e -> Toast.makeText(this, "Error cargando ciudades", Toast.LENGTH_SHORT).show());
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
            spPuebloUsuario.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombres));
            if (!pueblosDocs.isEmpty()) spPuebloUsuario.setSelection(0);
            else {
                etPuebloUsuario.setText("");
                etAyuntamientoUsuario.setText("");
                ayuntamientoIdSel = null;
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error cargando pueblos", Toast.LENGTH_SHORT).show());
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
        spinnerAyuntamientos.setVisibility(View.GONE);
    }

    // ==== RegisterView ====
    @Override public String getAliasInput() { return etAlias.getText() == null ? "" : etAlias.getText().toString().trim(); }
    @Override public String getPassword1() { return etPassword.getText() == null ? "" : etPassword.getText().toString().trim(); }
    @Override public String getPassword2() { return etPassword2.getText() == null ? "" : etPassword2.getText().toString().trim(); }
    @Override public String getNombre() { return etNombre.getText() == null ? "" : etNombre.getText().toString().trim(); }
    @Override public String getApellidos() { return etApellidos.getText() == null ? "" : etApellidos.getText().toString().trim(); }
    @Override public String getComunidad() { return etComunidad.getText() == null ? "" : etComunidad.getText().toString().trim(); }
    @Override public String getProvincia() { return etProvincia.getText() == null ? "" : etProvincia.getText().toString().trim(); }
    @Override public String getCiudad() { return etCiudad.getText() == null ? "" : etCiudad.getText().toString().trim(); }
    @Override public String getPueblo() {
        int checkedId = rgRol.getCheckedRadioButtonId();
        RadioButton rb = findViewById(checkedId);
        String rol = rb != null ? String.valueOf(rb.getTag()) : "usuario";
        if ("ayuntamiento".equals(rol)) {
            return etPuebloAyto.getText() == null ? "" : etPuebloAyto.getText().toString().trim();
        } else {
            return etPuebloUsuario.getText() == null ? "" : etPuebloUsuario.getText().toString().trim();
        }
    }
    @Override public String getRazonSocial() { return etNumero.getText() == null ? "" : etNumero.getText().toString().trim(); }
    @Override public String getRol() {
        int checkedId = rgRol.getCheckedRadioButtonId();
        if (checkedId == -1) return "usuario";
        RadioButton rb = findViewById(checkedId);
        return rb != null ? String.valueOf(rb.getTag()).trim().toLowerCase() : "usuario";
    }
    @Override public String getAyuntamientoSeleccionadoId() {
        return ayuntamientoIdSel == null ? "" : ayuntamientoIdSel;
    }

    // >>> Métodos nuevos requeridos por RegisterView (IDs seleccionados)
    @Override public String getComunidadIdSel() { return comunidadIdSel == null ? "" : comunidadIdSel; }
    @Override public String getProvinciaIdSel() { return provinciaIdSel == null ? "" : provinciaIdSel; }
    @Override public String getCiudadIdSel()    { return ciudadIdSel == null ? "" : ciudadIdSel; }

    @Override public void mostrarErrorAlias(String msg) { etAlias.setError(msg); etAlias.requestFocus(); }
    @Override public void mostrarErrorPassword(String msg) { etPassword.setError(msg); etPassword.requestFocus(); }
    @Override public void mostrarErrorNombre(String msg) { etNombre.setError(msg); etNombre.requestFocus(); }
    @Override public void mostrarErrorApellidos(String msg) { etApellidos.setError(msg); etApellidos.requestFocus(); }
    @Override public void mostrarErrorRazonSocial(String msg) { etNumero.setError(msg); etNumero.requestFocus(); }

    @Override public void mostrarMensaje(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    @Override public void navegarAyuntamiento() {
        startActivity(new Intent(this, GestionDeportesAyuntamientoActivity.class));
        finish();
    }

    @Override public void navegarUsuario() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
