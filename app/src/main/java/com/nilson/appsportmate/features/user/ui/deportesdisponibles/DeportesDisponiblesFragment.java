package com.nilson.appsportmate.features.user.ui.deportesdisponibles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.databinding.ActivityDeportesDisponiblesBinding;
import com.nilson.appsportmate.features.townhall.adaptadores.DeportesDisponiblesAdapter;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment que muestra deportes disponibles y permite apuntarse/desapuntarse.
 * Usa el layout activity_deportes_disponibles.xml con ViewBinding:
 *   ActivityDeportesDisponiblesBinding
 *
 * Reutiliza tu adapter existente:
 *   com.nilson.appsportmate.features.townhall.adaptadores.DeportesDisponiblesAdapter
 *
 * Estructura esperada en Firestore:
 * - deportes_ayuntamiento/{ayuntamientoId}/lista/{docId}
 *      fields: plazasDisponibles:int, ... (y demás info del evento)
 *      - inscritos/{uid}  => { uid, alias, ts }
 * - usuarios/{uid}/inscripciones/{docId} => copia de datos del evento + ayuntamientoId
 */
public class DeportesDisponiblesFragment extends Fragment implements DeportesDisponiblesAdapter.Listener {

    private ActivityDeportesDisponiblesBinding binding;

    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String uid;
    private String alias;

    private final List<Map<String, Object>> listaDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> listaMis = new ArrayList<>();

    private DeportesDisponiblesAdapter adapterDisponibles;
    private DeportesDisponiblesAdapter adapterMis;

    // Evita dobles taps mientras hay una operación en curso
    private boolean accionEnProgreso = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityDeportesDisponiblesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Preferencias
        if (getContext() != null) {
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(getContext());
            uid            = Preferencias.obtenerUid(getContext());
            alias          = Preferencias.obtenerAlias(getContext());
        }

        // RecyclerViews
        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMisDeportes.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapterDisponibles = new DeportesDisponiblesAdapter(listaDisponibles, false, this);
        adapterMis         = new DeportesDisponiblesAdapter(listaMis, true, this);

        binding.rvDisponibles.setAdapter(adapterDisponibles);
        binding.rvMisDeportes.setAdapter(adapterMis);

        // Cargar datos
        cargarDisponibles();
        cargarMisDeportes();

        // Botón salir (cierra la Activity host)
        binding.btnSalir.setOnClickListener(v -> requireActivity().finish());
    }

    /* ============================
       Carga de datos disponibles
       ============================ */
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
                .get(Source.SERVER) // fuerza servidor para evitar caché vieja
                .addOnSuccessListener(query -> {
                    listaDisponibles.clear();
                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
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
                    Toast.makeText(requireContext(),
                            "Error cargando disponibles: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /* ============================
       Carga de "mis deportes"
       ============================ */
    private void cargarMisDeportes() {
        if (uid == null || uid.isEmpty()) {
            binding.tvEmptyMis.setText("Inicia sesión para ver tus deportes.");
            binding.tvEmptyMis.setVisibility(View.VISIBLE);
            binding.rvMisDeportes.setVisibility(View.GONE);
            return;
        }

        db.collection("usuarios")
                .document(uid)
                .collection("inscripciones")
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {
                    listaMis.clear();

                    if (query.isEmpty()) {
                        adapterMis.notifyDataSetChanged();
                        binding.tvEmptyMis.setVisibility(View.VISIBLE);
                        binding.rvMisDeportes.setVisibility(View.GONE);
                        return;
                    }

                    // Verifica que cada inscripción siga existiendo en el origen; si no, la limpia.
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
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Error cargando mis deportes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void finProcesadoMis(List<Map<String, Object>> tmp, WriteBatch batchDelete) {
        batchDelete.commit().addOnCompleteListener(task -> {
            listaMis.clear();
            listaMis.addAll(tmp);
            adapterMis.notifyDataSetChanged();

            boolean vacio = listaMis.isEmpty();
            binding.tvEmptyMis.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvMisDeportes.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    private static String valueOf(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }

    /* ============================
       Callbacks del Adapter (Listener)
       ============================ */

    @Override
    public void onApuntarse(Map<String, Object> deporte) {
        if (!isAdded()) return;

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Inicia sesión para inscribirte", Toast.LENGTH_SHORT).show();
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
            // 1) Comprobar plazas
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
            if (!isAdded()) return;
            Toast.makeText(requireContext(), "Inscripción realizada", Toast.LENGTH_SHORT).show();
            cargarDisponibles();
            cargarMisDeportes();
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
    public void onDesapuntarse(Map<String, Object> deporte) {
        if (!isAdded()) return;

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Inicia sesión para continuar", Toast.LENGTH_SHORT).show();
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
            if (!isAdded()) return;
            Toast.makeText(requireContext(), "Te has desapuntado", Toast.LENGTH_SHORT).show();
            cargarDisponibles();
            cargarMisDeportes();
            accionEnProgreso = false;
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("NO_ESTABA_INSCRITO")) {
                Toast.makeText(requireContext(), "No estabas inscrito en esta actividad.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Error al desapuntarte: " + code, Toast.LENGTH_SHORT).show();
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
