package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentEventosDisponiblesUserPrivateBinding;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate.EventosDisponiblesUserPrivateAdapter;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.menuBase.BaseUserPrivateFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EventosDisponilblesUserPrivateFragment
        extends BaseUserPrivateFragment
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

    // ==============================
    // LAYOUT
    // ==============================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEventosDisponiblesUserPrivateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // ==============================
    // ON VIEW CREATED
    // ==============================
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 MENÚ COMÚN (BASE)
        configurarMenuPrivado(binding.toolbar);

        // ------------------------------------------------------
        // VIEWMODEL + FIRESTORE
        // ------------------------------------------------------
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

        // ------------------------------------------------------
        // RECYCLERVIEW
        // ------------------------------------------------------
        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosDisponiblesUserPrivateAdapter(new ArrayList<>(), this);
        binding.rvDisponibles.setAdapter(adapter);

        // ------------------------------------------------------
        // INIT / LOAD
        // ------------------------------------------------------
        if (puebloId != null) {
            vm.init(uid, alias, puebloId);
            vm.activarListeners();
        } else if (uid != null) {
            cargarPuebloDesdeFirestore(uid);
        }

        observeUiState();

        // ------------------------------------------------------
        // BOTÓN SALIR
        // ------------------------------------------------------
        binding.btnSalir.setOnClickListener(v -> {
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.inicioFragment, true)
                    .build();

            Navigation.findNavController(v)
                    .navigate(R.id.action_global_inicioFragment, null, opts);
        });
    }

    // ==============================
    // FIRESTORE FALLBACK
    // ==============================
    private void cargarPuebloDesdeFirestore(@NonNull String uidLocal) {
        db.collection("usuarios")
                .document(uidLocal)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String puebloIdDoc     = safe(doc.getString("puebloId"));
                    String puebloNombreDoc = safe(doc.getString("puebloNombre"));

                    if (puebloIdDoc == null) return;

                    puebloId     = puebloIdDoc;
                    puebloNombre = puebloNombreDoc;
                    lastPuebloId = puebloIdDoc;

                    Preferencias.guardarPuebloId(requireContext(), puebloIdDoc);
                    Preferencias.guardarPuebloNombre(
                            requireContext(),
                            puebloNombreDoc != null ? puebloNombreDoc : ""
                    );

                    binding.tvPuebloNombre.setText(
                            puebloNombreDoc != null ? puebloNombreDoc : "—"
                    );

                    vm.init(uid, alias, puebloIdDoc);
                    vm.activarListeners();
                });
    }

    // ==============================
    // OBSERVER
    // ==============================
    private void observeUiState() {
        vm.uiState.observe(getViewLifecycleOwner(), state -> {

            if (state == null) return;

            if (state.message != null) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
            }

            adapter.update(state.disponibles != null
                    ? state.disponibles
                    : Collections.emptyList());

            boolean vacio = state.disponibles == null || state.disponibles.isEmpty();
            binding.tvEmptyDisponibles.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvDisponibles.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    // ==============================
    // LISTENERS
    // ==============================
    @Override
    public void onApuntarse(Map<String, Object> evento) {
        vm.apuntarse(evento);
    }

    @Override
    public void onDesapuntarse(Map<String, Object> evento) {
        // No implementado
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vm != null) vm.detenerListeners();
        binding = null;
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
