package com.nilson.appsportmate.features.user.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.modelos.UsuarioAyuntamiento;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FormularioUsuarioAyuntamientoActivity extends AppCompatActivity {

    // Inputs
    private TextInputEditText etAlias, etNombre, etApellidos, etComunidad,
            etProvincia, etCiudad, etPueblo, etUid, etId;

    private Spinner spinnerAyuntamientos;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;

    private final List<String> ayuntamientoIds = new ArrayList<>();
    private final List<String> ayuntamientoNombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_usuario_ayuntamiento);

        // Referencias a los EditText
        etAlias = findViewById(R.id.etAlias);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etComunidad = findViewById(R.id.etDescripcionEvento);
        etProvincia = findViewById(R.id.etReglasEvento);
        etCiudad = findViewById(R.id.etMateriales);
        etPueblo = findViewById(R.id.etUrlPueblo);
        etUid = findViewById(R.id.etUid);
        etId = findViewById(R.id.etId);

        spinnerAyuntamientos = findViewById(R.id.spinnerAyuntamientos);
        btnGuardar = findViewById(R.id.btnGuardar);

        db = FirebaseFirestore.getInstance();

        // Prellenar UID desde preferencias
        String uidPref = Preferencias.obtenerUid(this);
        if (uidPref != null) etUid.setText(uidPref);

        // Prellenar ID único si no existe
        if (etId.getText() == null || etId.getText().toString().isEmpty()) {
            etId.setText(UUID.randomUUID().toString());
        }

        cargarAyuntamientos();

        btnGuardar.setOnClickListener(v -> guardarUsuario());
    }

    private void cargarAyuntamientos() {
        db.collection("ayuntamientos").get()
                .addOnSuccessListener(query -> {
                    ayuntamientoIds.clear();
                    ayuntamientoNombres.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        ayuntamientoIds.add(doc.getId());
                        String nombre = doc.getString("nombre");
                        ayuntamientoNombres.add(nombre != null ? nombre : doc.getId());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            ayuntamientoNombres
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAyuntamientos.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando ayuntamientos", Toast.LENGTH_SHORT).show());
    }

    private void guardarUsuario() {
        int pos = spinnerAyuntamientos.getSelectedItemPosition();
        if (pos == -1) {
            Toast.makeText(this, "Selecciona un ayuntamiento", Toast.LENGTH_SHORT).show();
            return;
        }
        String ayuntamientoId = ayuntamientoIds.get(pos);

        // Recoger valores de los campos
        String alias = etAlias.getText() != null ? etAlias.getText().toString().trim().toLowerCase() : "";
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String apellidos = etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
        String comunidad = etComunidad.getText() != null ? etComunidad.getText().toString().trim() : "";
        String provincia = etProvincia.getText() != null ? etProvincia.getText().toString().trim() : "";
        String ciudad = etCiudad.getText() != null ? etCiudad.getText().toString().trim() : "";
        String pueblo = etPueblo.getText() != null ? etPueblo.getText().toString().trim() : "";
        String uid = etUid.getText() != null ? etUid.getText().toString().trim() : "";
        String id = etId.getText() != null ? etId.getText().toString().trim() : "";

        if (alias.isEmpty() || nombre.isEmpty() || apellidos.isEmpty() || uid.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el modelo
        UsuarioAyuntamiento usuario = new UsuarioAyuntamiento(
                id, nombre, apellidos, alias, comunidad, provincia, ciudad, pueblo, uid, ayuntamientoId
        );

        // Guardar en Firestore (documento = UID)
        db.collection("usuarios")
                .document(uid)
                .set(usuario)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Usuario guardado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
