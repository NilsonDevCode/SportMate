package com.nilson.appsportmate.features.user.ui.deportesdisponibles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.ActivityDeportesDisponiblesBinding;
import com.nilson.appsportmate.features.townhall.adaptadores.DeportesDisponiblesAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeportesDisponiblesFragment extends Fragment implements DeportesDisponiblesAdapter.Listener {

    private ActivityDeportesDisponiblesBinding binding;

    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String uid;
    private String alias;

    private String lastAyuntamientoId;

    private final List<Map<String, Object>> listaDisponibles = new ArrayList<>();
    private DeportesDisponiblesAdapter adapterDisponibles;

    private boolean accionEnProgreso = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityDeportesDisponiblesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getContext() != null) {
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(getContext());
            uid            = Preferencias.obtenerUid(getContext());
            alias          = Preferencias.obtenerAlias(getContext());
            lastAyuntamientoId = ayuntamientoId;
        }

        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterDisponibles = new DeportesDisponiblesAdapter(listaDisponibles, this);
        binding.rvDisponibles.setAdapter(adapterDisponibles);

        // cargar datos
        cargarNombreAyuntamiento();
        cargarDisponibles();

        binding.btnSalir.setOnClickListener(v -> {
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.deportesDisponiblesFragment, true)
                    .build();
            Navigation.findNavController(v).navigate(R.id.action_global_inicioFragment, null, opts);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            String nuevo = Preferencias.obtenerAyuntamientoId(getContext());
            if (nuevo == null ? lastAyuntamientoId != null : !nuevo.equals(lastAyuntamientoId)) {
                ayuntamientoId = nuevo;
                lastAyuntamientoId = nuevo;

                // refrescar encabezado y listas
                cargarNombreAyuntamiento();
                listaDisponibles.clear();
                adapterDisponibles.notifyDataSetChanged();
                cargarDisponibles();
            }
        }
    }

    /* ===== Encabezado: nombre del ayuntamiento ===== */
    private void cargarNombreAyuntamiento() {
        if (!isAdded()) return;

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            binding.tvAytoNombre.setText("Sin ayuntamiento");
            return;
        }

        db.collection("ayuntamientos")
                .document(ayuntamientoId)
                .get(Source.SERVER)
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = extraerNombreAyuntamiento(doc);
                    binding.tvAytoNombre.setText(nombre);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    binding.tvAytoNombre.setText("(desconocido)");
                });
    }

    private String extraerNombreAyuntamiento(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return "(desconocido)";
        Object n1 = doc.get("nombre");
        Object n2 = doc.get("razonSocial");
        String nom = n1 != null ? String.valueOf(n1) : null;
        if (nom == null || nom.trim().isEmpty() || "null".equalsIgnoreCase(nom)) {
            nom = n2 != null ? String.valueOf(n2) : "(desconocido)";
        }
        return nom;
    }

    /* ===== Carga de disponibles ===== */
    private void cargarDisponibles() {
        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            binding.tvEmptyDisponibles.setText("No tienes ayuntamiento asignado.");
            binding.tvEmptyDisponibles.setVisibility(View.VISIBLE);
            binding.rvDisponibles.setVisibility(View.GONE);
            return;
        }

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {
                    listaDisponibles.clear();
                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
                        m = new HashMap<>(m);
                        m.put("idDoc", d.getId());
                        listaDisponibles.add(m);
                    }
                    adapterDisponibles.notifyDataSetChanged();

                    boolean vacio = listaDisponibles.isEmpty();
                    binding.tvEmptyDisponibles.setVisibility(vacio ? View.VISIBLE : View.GONE);
                    binding.rvDisponibles.setVisibility(vacio ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Error cargando disponibles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /* ===== Listener del adapter ===== */
    @Override
    public void onApuntarse(Map<String, Object> deporte) {
        if (!isAdded()) return;

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Inicia sesión para inscribirte", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accionEnProgreso) return;
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
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");

            DocumentSnapshot snapInscrito = tx.get(refInscrito);
            if (snapInscrito.exists()) throw new IllegalStateException("YA_INSCRITO");

            tx.update(refDeporte, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(deporte);
            copia.put("idDoc", docId);
            copia.put("ayuntamientoId", ayuntamientoId);
            tx.set(refUser, copia);

            return null;
        }).addOnSuccessListener(unused -> {
            if (!isAdded()) return;
            Toast.makeText(requireContext(), "Inscripción realizada", Toast.LENGTH_SHORT).show();
            adapterDisponibles.markApuntado(docId); // pinta “Apuntado”
            cargarDisponibles();                    // refresca plazas
            accionEnProgreso = false;
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("YA_INSCRITO")) {
                Toast.makeText(requireContext(), "Solo puedes apuntarte una vez a esta actividad.", Toast.LENGTH_SHORT).show();
            } else if (code.contains("NO_PLAZAS")) {
                Toast.makeText(requireContext(), "No hay plazas disponibles.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No se pudo inscribir: " + code, Toast.LENGTH_SHORT).show();
            }
            accionEnProgreso = false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
