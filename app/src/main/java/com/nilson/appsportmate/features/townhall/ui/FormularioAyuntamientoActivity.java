package com.nilson.appsportmate.features.townhall.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.modelos.Ayuntamiento;

import java.util.HashMap;
import java.util.Map;

public class FormularioAyuntamientoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etNumero, etComunidad, etProvincia,
            etCiudad, etPueblo, etLocalidad, etUid;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;
    private String ayuntamientoId; // para editar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_ayuntamiento);

        // Referencias a los campos
        etNombre = findViewById(R.id.etNombre);
        etNumero = findViewById(R.id.etNumero);
        etComunidad = findViewById(R.id.etDescripcionEvento);
        etProvincia = findViewById(R.id.etReglasEvento);
        etCiudad = findViewById(R.id.etMateriales);
        etPueblo = findViewById(R.id.etUrlPueblo);
        etLocalidad = findViewById(R.id.etLocalidad);
        etUid = findViewById(R.id.etUid);

        btnGuardar = findViewById(R.id.btnGuardar);

        db = FirebaseFirestore.getInstance();

        // Revisar si venimos a editar
        ayuntamientoId = getIntent().getStringExtra("ayuntamientoId");
        if (ayuntamientoId != null) {
            cargarDatos(ayuntamientoId);
        }

        btnGuardar.setOnClickListener(v -> guardarDatos());
    }

    private void cargarDatos(String id) {
        db.collection("ayuntamientos").document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    Ayuntamiento ayuntamiento = doc.toObject(Ayuntamiento.class);
                    if (ayuntamiento != null) {
                        etUid.setText(ayuntamiento.getUid());
                        etNombre.setText(ayuntamiento.getNombre());
                        etNumero.setText(ayuntamiento.getNumero());
                        etComunidad.setText(ayuntamiento.getComunidad());
                        etProvincia.setText(ayuntamiento.getProvincia());
                        etCiudad.setText(ayuntamiento.getCiudad());
                        etPueblo.setText(ayuntamiento.getPueblo());
                        etLocalidad.setText(ayuntamiento.getLocalidad());
                    }
                });
    }

    private void guardarDatos() {
        String uid = String.valueOf(etUid.getText()).trim();
        String nombre = String.valueOf(etNombre.getText()).trim();
        String numero = String.valueOf(etNumero.getText()).trim();
        String comunidad = String.valueOf(etComunidad.getText()).trim();
        String provincia = String.valueOf(etProvincia.getText()).trim();
        String ciudad = String.valueOf(etCiudad.getText()).trim();
        String pueblo = String.valueOf(etPueblo.getText()).trim();
        String localidad = String.valueOf(etLocalidad.getText()).trim();

        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Nombre requerido"); return; }
        if (TextUtils.isEmpty(numero)) { etNumero.setError("Número requerido"); return; }

        Map<String, Object> datos = new HashMap<>();
        datos.put("uid", uid);
        datos.put("nombre", nombre);
        datos.put("numero", numero);
        datos.put("comunidad", comunidad);
        datos.put("provincia", provincia);
        datos.put("ciudad", ciudad);
        datos.put("pueblo", pueblo);
        datos.put("localidad", localidad);

        String docId = (ayuntamientoId != null) ? ayuntamientoId : uid;

        db.collection("ayuntamientos").document(docId).set(datos)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
