package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.nilson.appsportmate.databinding.FragmentEventosDisponiblesUserPrivateBinding;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.eventosAdapterPrivate.EventosDisponiblesUserPrivateAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventosDisponilblesUserPrivateFragment extends Fragment
        implements EventosDisponiblesUserPrivateAdapter.Listener {

    private FragmentEventosDisponiblesUserPrivateBinding binding;

    private FirebaseFirestore db;

    private String uid;
    private String alias;
    private String localidad;

    private final List<Map<String, Object>> listaDisponibles = new ArrayList<>();
    private EventosDisponiblesUserPrivateAdapter adapter;

    private boolean accionEnProgreso = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // NECESARIO PARA QUE EL TOOLBAR ENVÍE LOS EVENTOS DE MENÚ
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentEventosDisponiblesUserPrivateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getContext() != null) {
            uid       = Preferencias.obtenerUid(getContext());
            alias     = Preferencias.obtenerAlias(getContext());
            localidad = Preferencias.obtenerLocalidad(getContext());
        }

        // Nombre del pueblo
        binding.tvPuebloNombre.setText(localidad != null ? localidad : "—");

        // RecyclerView
        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosDisponiblesUserPrivateAdapter(listaDisponibles, this);
        binding.rvDisponibles.setAdapter(adapter);

        // Cargar eventos privados de todos los usuarios
        cargarEventosDisponibles();

        // Botón salir
        binding.btnSalir.setOnClickListener(v -> {
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.inicioFragment, true)
                    .build();

            Navigation.findNavController(v)
                    .navigate(R.id.action_global_inicioFragment, null, opts);
        });
    }

    // ==========================================================
    // OPCIÓN DEL MENÚ (CREAR EVENTO PRIVADO)
    // ==========================================================
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_crear_evento_privado) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_crearEventoUserPrivateFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==========================================================
    // CARGAR EVENTOS
    // ==========================================================
    private void cargarEventosDisponibles() {
        listaDisponibles.clear();

        db.collection("eventos_user_private")
                .get(Source.SERVER)
                .addOnSuccessListener(owners -> {

                    if (!isAdded()) return;

                    for (DocumentSnapshot ownerDoc : owners) {
                        String ownerId = ownerDoc.getId();

                        ownerDoc.getReference()
                                .collection("lista")
                                .get(Source.SERVER)
                                .addOnSuccessListener(lista -> {

                                    for (DocumentSnapshot d : lista.getDocuments()) {

                                        Map<String, Object> m = d.getData();
                                        if (m == null) continue;

                                        m = new HashMap<>(m);
                                        m.put("idDoc", d.getId());
                                        m.put("ownerId", ownerId);

                                        listaDisponibles.add(m);
                                    }

                                    adapter.notifyDataSetChanged();

                                    boolean vacio = listaDisponibles.isEmpty();
                                    binding.tvEmptyDisponibles.setVisibility(vacio ? View.VISIBLE : View.GONE);
                                    binding.rvDisponibles.setVisibility(vacio ? View.GONE : View.VISIBLE);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Error cargando eventos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================================
    // APUNTARSE
    // ==========================================================
    @Override
    public void onApuntarse(Map<String, Object> evento) {
        if (!isAdded()) return;
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Inicia sesión para participar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accionEnProgreso) return;
        accionEnProgreso = true;

        String docId = String.valueOf(evento.get("idDoc"));
        String ownerId = String.valueOf(evento.get("ownerId"));

        DocumentReference refEvento = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refEvento
                .collection("inscritos_privados")
                .document(uid);

        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(docId);

        db.runTransaction(tx -> {

            DocumentSnapshot snapEvt = tx.get(refEvento);
            Long plazas = snapEvt.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");

            if (tx.get(refInscrito).exists())
                throw new IllegalStateException("YA_INSCRITO");

            tx.update(refEvento, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(evento);
            copia.put("idDoc", docId);
            copia.put("ownerId", ownerId);
            tx.set(refUser, copia);

            return null;
        }).addOnSuccessListener(unused -> {
            if (!isAdded()) return;

            Toast.makeText(requireContext(), "Inscripción completada", Toast.LENGTH_SHORT).show();
            adapter.markApuntado(docId);
            cargarEventosDisponibles();

            accionEnProgreso = false;

        }).addOnFailureListener(e -> {
            if (!isAdded()) return;

            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("YA_INSCRITO"))
                Toast.makeText(requireContext(), "Ya estás inscrito.", Toast.LENGTH_SHORT).show();
            else if (msg.contains("NO_PLAZAS"))
                Toast.makeText(requireContext(), "No hay plazas disponibles.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(requireContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();

            accionEnProgreso = false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
