package com.nilson.appsportmate.features.townhall.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.auth.ui.LoginActivity;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GestionDeportesAyuntamientoActivity extends AppCompatActivity {

    private EditText etNombreDeporte, etCantidadJugadores, etFecha, etHora,
            etDescripcionEvento, etReglasEvento, etMateriales, etUrlPueblo;

    private MaterialButton btnCrearEvento;
    private MaterialButton btnGestionEventos;

    private String ayuntamientoId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_deportes_ayuntamiento);

        db = FirebaseFirestore.getInstance();
        ayuntamientoId = Preferencias.obtenerAyuntamientoId(this);

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            Toast.makeText(this, "Error: ayuntamiento_id no encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etNombreDeporte      = findViewById(R.id.etNombreDeporte);
        etCantidadJugadores  = findViewById(R.id.etCantidadJugadores);
        etFecha              = findViewById(R.id.etFecha);
        etHora               = findViewById(R.id.etHora);
        etDescripcionEvento  = findViewById(R.id.etDescripcionEvento);
        etReglasEvento       = findViewById(R.id.etReglasEvento);
        etMateriales         = findViewById(R.id.etMateriales);
        etUrlPueblo          = findViewById(R.id.etUrlPueblo);

        btnCrearEvento       = findViewById(R.id.btnCrearEvento);
        btnGestionEventos    = findViewById(R.id.btnGestionEventos);

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());

        btnCrearEvento.setOnClickListener(view -> crearDeporte());

        btnGestionEventos.setOnClickListener(v -> {
            Intent i = new Intent(this, GestionEventosMasPlazasActivity.class);
            i.putExtra("ayuntamientoId", ayuntamientoId);
            startActivity(i);
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Preferencias.guardarRol(this, null);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void crearDeporte() {
        String nombre       = txt(etNombreDeporte);
        String cantidadStr  = txt(etCantidadJugadores);
        String fecha        = txt(etFecha);
        String hora         = txt(etHora);
        String descripcion  = txt(etDescripcionEvento);
        String reglas       = txt(etReglasEvento);
        String materiales   = txt(etMateriales);
        String urlPueblo    = txt(etUrlPueblo);

        if (nombre.isEmpty() || cantidadStr.isEmpty() || fecha.isEmpty() || hora.isEmpty()
                || descripcion.isEmpty() || reglas.isEmpty() || materiales.isEmpty()
                || urlPueblo.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            etCantidadJugadores.setError("Número inválido");
            return;
        }

        Map<String, Object> deporte = new HashMap<>();
        deporte.put("nombre", nombre);
        deporte.put("plazasDisponibles", cantidad);
        deporte.put("fecha", fecha);
        deporte.put("hora", hora);
        deporte.put("descripcion", descripcion);
        deporte.put("reglas", reglas);
        // claves reales que consumirá el adapter:
        deporte.put("materiales", materiales);
        deporte.put("urlPueblo", urlPueblo);
        deporte.put("ayuntamientoId", ayuntamientoId);

        String docId = generarDocId(nombre, fecha, hora);

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId)
                .set(deporte)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Evento creado correctamente.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, GestionEventosMasPlazasActivity.class);
                    i.putExtra("ayuntamientoId", ayuntamientoId);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al crear el evento: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    public static String generarDocId(String nombre, String fecha, String hora) {
        return nombre.replace(" ", "_") + "_" +
                fecha.replace("/", "_") + "_" +
                hora.replace(":", "_");
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void mostrarTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) ->
                        etHora.setText(String.format("%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        );
        dialog.show();
    }

    private static String txt(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
