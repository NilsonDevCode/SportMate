package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentEventosDisponiblesUserPrivateBinding;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate.EventosDisponiblesUserPrivateAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EventosDisponilblesUserPrivateFragment extends Fragment
        implements EventosDisponiblesUserPrivateAdapter.Listener {

    private static final String TAG = "EventosPrivadosFrag";

    private FragmentEventosDisponiblesUserPrivateBinding binding;
    private EventosDisponiblesUserPrivateViewModel vm;
    private EventosDisponiblesUserPrivateAdapter adapter;

    private FirebaseFirestore db;

    private String uid;
    private String alias;
    private String puebloId;
    private String puebloNombre;
    private String lastPuebloId;

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
        vm = new ViewModelProvider(this).get(EventosDisponiblesUserPrivateViewModel.class);

        if (getContext() != null) {
            uid          = Preferencias.obtenerUid(getContext());
            alias        = Preferencias.obtenerAlias(getContext());
            puebloId     = Preferencias.obtenerPuebloId(getContext());
            puebloNombre = Preferencias.obtenerPuebloNombre(getContext());
            lastPuebloId = puebloId;
        }

        Log.e(TAG, "onViewCreated → uid=" + uid +
                " alias=" + alias +
                " puebloId=" + puebloId +
                " puebloNombre=" + puebloNombre);

        binding.tvPuebloNombre.setText(
                (puebloNombre != null && !puebloNombre.isEmpty()) ? puebloNombre : "—"
        );

        // RecyclerView
        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosDisponiblesUserPrivateAdapter(
                new ArrayList<>(),
                this
        );
        binding.rvDisponibles.setAdapter(adapter);

        // 🔥 INIT / LOAD
        if (puebloId != null) {
            // Caso normal: ya hay pueblo guardado en Preferencias
            vm.init(uid, alias, puebloId);
            vm.loadAll();
        } else {
            // ⚠ Fallback: leer pueblo desde Firestore (usuarios/{uid})
            if (uid != null) {
                cargarPuebloDesdeFirestore(uid);
            } else {
                Log.e(TAG, "uid es NULL, no se puede cargar pueblo");
            }
        }

        observeUiState();

        binding.btnSalir.setOnClickListener(v -> {
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.inicioFragment, true)
                    .build();

            Navigation.findNavController(v)
                    .navigate(R.id.action_global_inicioFragment, null, opts);
        });
    }

    /**
     * Fallback cuando puebloId en Preferencias viene null.
     * Lee usuarios/{uid} y rellena puebloId + puebloNombre.
     */
    private void cargarPuebloDesdeFirestore(@NonNull String uidLocal) {
        Log.e(TAG, "cargarPuebloDesdeFirestore() uid=" + uidLocal);

        db.collection("usuarios")
                .document(uidLocal)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        Log.e(TAG, "Usuario no encontrado en Firestore para uid=" + uidLocal);
                        return;
                    }

                    String puebloIdDoc     = safe(doc.getString("puebloId"));
                    String puebloNombreDoc = safe(doc.getString("puebloNombre"));

                    Log.e(TAG, "Firestore usuario → puebloId=" + puebloIdDoc +
                            " puebloNombre=" + puebloNombreDoc);

                    if (puebloIdDoc == null) {
                        Log.e(TAG, "El documento de usuario no tiene puebloId, no se puede filtrar.");
                        return;
                    }

                    // Actualizar estado interno
                    puebloId     = puebloIdDoc;
                    puebloNombre = puebloNombreDoc;
                    lastPuebloId = puebloIdDoc;

                    // Guardar en Preferencias para futuras pantallas
                    if (getContext() != null) {
                        Preferencias.guardarPuebloId(getContext(), puebloIdDoc);
                        Preferencias.guardarPuebloNombre(getContext(),
                                puebloNombreDoc != null ? puebloNombreDoc : "");
                    }

                    // Actualizar UI
                    binding.tvPuebloNombre.setText(
                            (puebloNombreDoc != null && !puebloNombreDoc.isEmpty())
                                    ? puebloNombreDoc
                                    : "—"
                    );

                    // Ahora sí, inicializar VM con el pueblo correcto
                    vm.init(uid, alias, puebloIdDoc);
                    vm.loadAll();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error leyendo usuario para obtener puebloId", e);
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() != null) {
            String nuevoId     = Preferencias.obtenerPuebloId(getContext());
            String nuevoNombre = Preferencias.obtenerPuebloNombre(getContext());

            Log.e(TAG, "onResume → nuevoPuebloId=" + nuevoId +
                    " lastPuebloId=" + lastPuebloId);

            // Actualizar encabezado
            binding.tvPuebloNombre.setText(
                    (nuevoNombre != null && !nuevoNombre.isEmpty()) ? nuevoNombre : "—"
            );

            // Avisar al ViewModel si ha cambiado
            vm.ensurePuebloId(nuevoId);
            lastPuebloId = nuevoId;
        }
    }

    // ==========================================================
    // OBSERVAR UI STATE
    // ==========================================================
    private void observeUiState() {
        vm.uiState.observe(getViewLifecycleOwner(), state -> {

            Log.e(TAG, "UI STATE → " + state);

            if (state == null) return;

            if (state.message != null) {
                Log.e(TAG, "Mensaje recibido: " + state.message);
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
                vm.consumeMessage();
            }

            if (state.disponibles != null) {
                Log.e(TAG, "Actualizando adapter con " + state.disponibles.size() + " items");
                adapter.update(state.disponibles);
            } else {
                adapter.update(Collections.emptyList());
            }

            boolean vacio = state.disponibles == null || state.disponibles.isEmpty();
            binding.tvEmptyDisponibles.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvDisponibles.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    // ==========================================================
    // LISTENERS
    // ==========================================================
    @Override
    public void onApuntarse(Map<String, Object> evento) {
        Log.e(TAG, "onApuntarse evento=" + evento);
        vm.apuntarse(evento);
    }

    @Override
    public void onDesapuntarse(Map<String, Object> evento) {
        Log.e(TAG, "onDesapuntarse evento=" + evento);
        vm.desapuntarse(evento);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView()");
        binding = null;
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
