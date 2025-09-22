package com.nilson.appsportmate.caracteristicas.usuario.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.adaptadores.DeportesDisponiblesAdapter;
import com.nilson.appsportmate.comun.utilidades.Preferencias;
import com.nilson.appsportmate.caracteristicas.principal.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Estructura esperada:
// deportes_ayuntamiento/{ayuntamientoId}/lista/{docId} (plazasDisponibles:int, ...)
// └── inscritos/{uid} (uid, alias, ts)
// usuarios/{uid}/inscripciones/{docId} (ayuntamientoId, nombre, fecha, hora, ...)

public class DeportesDisponiblesActivity extends AppCompatActivity
        implements DeportesDisponiblesAdapter.Listener {

    private TextView tvEmptyDisponibles, tvEmptyMis;
    private RecyclerView rvDisponibles, rvMis;
    private Button btnSalir;

    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String uid;
    private String alias;

    private final List<Map<String, Object>> listaDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> listaMis = new ArrayList<>();

    private DeportesDisponiblesAdapter adapterDisponibles;
    private DeportesDisponiblesAdapter adapterMis;

    // Evitar dobles taps mientras hay una operación en curso
    private boolean accionEnProgreso = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deportes_disponibles);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Deportes Disponibles");
        }

        tvEmptyDisponibles = findViewById(R.id.tvEmptyDisponibles);
        tvEmptyMis = findViewById(R.id.tvEmptyMis);
        rvDisponibles = findViewById(R.id.rvDisponibles);
        rvMis = findViewById(R.id.rvMisDeportes);
        btnSalir = findViewById(R.id.btnSalir);

        rvDisponibles.setLayoutManager(new LinearLayoutManager(this));
        rvMis.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        String extra = getIntent().getStringExtra("ayuntamientoId");
        ayuntamientoId = (extra != null && !extra.isEmpty()) ? extra : Preferencias.obtenerAyuntamientoId(this);

        uid = Preferencias.obtenerUid(this);
        alias = Preferencias.obtenerAlias(this);

        adapterDisponibles = new DeportesDisponiblesAdapter(listaDisponibles, false, this);
        adapterMis = new DeportesDisponiblesAdapter(listaMis, true, this);

        rvDisponibles.setAdapter(adapterDisponibles);
        rvMis.setAdapter(adapterMis);

        cargarDisponibles();
        cargarMisDeportes();

        btnSalir.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void cargarDisponibles() {
        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            tvEmptyDisponibles.setText("No tienes ayuntamiento asignado.");
            tvEmptyDisponibles.setVisibility(View.VISIBLE);
            rvDisponibles.setVisibility(View.GONE);
            return;
        }

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .get(Source.SERVER) // <— fuerza servidor (evita cache vieja)
                .addOnSuccessListener(query -> {
                    listaDisponibles.clear();
                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
                        m.put("idDoc", d.getId());
                        listaDisponibles.add(m);
                    }
                    adapterDisponibles.notifyDataSetChanged();
                    tvEmptyDisponibles.setVisibility(listaDisponibles.isEmpty() ? View.VISIBLE : View.GONE);
                    rvDisponibles.setVisibility(listaDisponibles.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando disponibles: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarMisDeportes() {
        if (uid == null || uid.isEmpty()) {
            tvEmptyMis.setText("Inicia sesión para ver tus deportes.");
            tvEmptyMis.setVisibility(View.VISIBLE);
            rvMis.setVisibility(View.GONE);
            return;
        }

        db.collection("usuarios")
                .document(uid)
                .collection("inscripciones")
                .get(Source.SERVER) // <— fuerza servidor
                .addOnSuccessListener(query -> {
                    listaMis.clear();

                    if (query.isEmpty()) {
                        adapterMis.notifyDataSetChanged();
                        tvEmptyMis.setVisibility(View.VISIBLE);
                        rvMis.setVisibility(View.GONE);
                        return;
                    }

                    // Verificación de existencia del evento y limpieza de inscripciones huérfanas
                    List<Map<String, Object>> tmp = new ArrayList<>();
                    WriteBatch batchDelete = db.batch();
                    final int total = query.size();
                    final int[] procesados = {0};

                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) {
                            procesados[0]++;
                            if (procesados[0] == total) finProcesadoMis(tmp, batchDelete);
                            continue;
                        }

                        String idDoc = d.getId();
                        String aytoIdInscripcion = valueOf(m.get("ayuntamientoId"));
                        if (aytoIdInscripcion == null || aytoIdInscripcion.isEmpty()) {
                            aytoIdInscripcion = ayuntamientoId; // fallback
                        }

                        m.put("idDoc", idDoc);

                        final String aytoFinal = aytoIdInscripcion;
                        db.collection("deportes_ayuntamiento")
                                .document(aytoFinal)
                                .collection("lista")
                                .document(idDoc)
                                .get(Source.SERVER)
                                .addOnSuccessListener(evSnap -> {
                                    if (evSnap.exists()) {
                                        // El evento aún existe → mostrar
                                        tmp.add(m);
                                    } else {
                                        // El evento ya no existe → borrar inscripción huérfana
                                        DocumentReference refUserIns = d.getReference();
                                        batchDelete.delete(refUserIns);
                                    }
                                    procesados[0]++;
                                    if (procesados[0] == total) finProcesadoMis(tmp, batchDelete);
                                })
                                .addOnFailureListener(e -> {
                                    // Si falla la comprobación, no mostramos ese item
                                    procesados[0]++;
                                    if (procesados[0] == total) finProcesadoMis(tmp, batchDelete);
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando mis deportes: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void finProcesadoMis(List<Map<String, Object>> tmp, WriteBatch batchDelete) {
        // Ejecuta el borrado de huérfanos si hay
        batchDelete.commit().addOnCompleteListener(task -> {
            listaMis.clear();
            listaMis.addAll(tmp);
            adapterMis.notifyDataSetChanged();

            boolean vacio = listaMis.isEmpty();
            tvEmptyMis.setVisibility(vacio ? View.VISIBLE : View.GONE);
            rvMis.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    private static String valueOf(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }

    /* ========== Callbacks de Adapter ========== */

    @Override
    public void onApuntarse(Map<String, Object> deporte) {
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para inscribirte", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accionEnProgreso) return; // evita doble click rápido
        accionEnProgreso = true;

        String docId = String.valueOf(deporte.get("idDoc"));
        DocumentReference refDeporte = db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refDeporte.collection("inscritos").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones").document(docId);

        db.runTransaction(tx -> {
            // 1) Comprobar plazas (server)
            DocumentSnapshot snapDeporte = tx.get(refDeporte);
            Long plazas = snapDeporte.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");

            // 2) Comprobar si YA está inscrito
            DocumentSnapshot snapInscrito = tx.get(refInscrito);
            if (snapInscrito.exists()) throw new IllegalStateException("YA_INSCRITO");

            // 3) Proceder: decremento y escribo
            tx.update(refDeporte, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(deporte);
            copia.remove("idDoc");
            copia.put("idDoc", docId);
            copia.put("ayuntamientoId", ayuntamientoId);
            tx.set(refUser, copia);

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Inscripción realizada", Toast.LENGTH_SHORT).show();
            cargarDisponibles();
            cargarMisDeportes();
            accionEnProgreso = false;
        }).addOnFailureListener(e -> {
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("YA_INSCRITO")) {
                Toast.makeText(this, "Solo puedes apuntarte una vez a esta actividad.", Toast.LENGTH_SHORT).show();
            } else if (code.contains("NO_PLAZAS")) {
                Toast.makeText(this, "No hay plazas disponibles.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo inscribir: " + code, Toast.LENGTH_SHORT).show();
            }
            accionEnProgreso = false;
        });
    }

    @Override
    public void onDesapuntarse(Map<String, Object> deporte) {
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para continuar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accionEnProgreso) return; // evita doble click
        accionEnProgreso = true;

        String docId = String.valueOf(deporte.get("idDoc"));
        DocumentReference refDeporte = db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refDeporte.collection("inscritos").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones").document(docId);

        db.runTransaction(tx -> {
            DocumentSnapshot snapDeporte = tx.get(refDeporte);
            Long plazas = snapDeporte.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;

            // Solo incremento si realmente estaba inscrito
            DocumentSnapshot snapInscrito = tx.get(refInscrito);
            if (!snapInscrito.exists()) throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refDeporte, "plazasDisponibles", plazas + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Te has desapuntado", Toast.LENGTH_SHORT).show();
            cargarDisponibles();
            cargarMisDeportes();
            accionEnProgreso = false;
        }).addOnFailureListener(e -> {
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("NO_ESTABA_INSCRITO")) {
                Toast.makeText(this, "No estabas inscrito en esta actividad.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al desapuntarte: " + code, Toast.LENGTH_SHORT).show();
            }
            accionEnProgreso = false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
