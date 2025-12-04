package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.os.Bundle;
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

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentEventosDisponiblesUserPrivateBinding;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.eventosAdapterPrivate.EventosDisponiblesUserPrivateAdapter;

import java.util.Map;
import java.util.Collections;

public class EventosDisponilblesUserPrivateFragment extends Fragment
        implements EventosDisponiblesUserPrivateAdapter.Listener {

    private FragmentEventosDisponiblesUserPrivateBinding binding;

    private EventosDisponiblesUserPrivateViewModel vm;
    private EventosDisponiblesUserPrivateAdapter adapter;

    private String uid, alias, puebloId, puebloNombre;
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

        vm = new ViewModelProvider(this).get(EventosDisponiblesUserPrivateViewModel.class);

        if (getContext() != null) {
            uid          = Preferencias.obtenerUid(getContext());
            alias        = Preferencias.obtenerAlias(getContext());
            puebloId     = Preferencias.obtenerPuebloId(getContext());       // ID REAL
            puebloNombre = Preferencias.obtenerPuebloNombre(getContext());   // visible
            lastPuebloId = puebloId;
        }

        binding.tvPuebloNombre.setText(puebloNombre != null ? puebloNombre : "—");

        // RecyclerView
        binding.rvDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventosDisponiblesUserPrivateAdapter(
                vm.uiState.getValue() != null ?
                        vm.uiState.getValue().disponibles :
                        Collections.emptyList(),
                this
        );

        binding.rvDisponibles.setAdapter(adapter);

        // INIT y cargar datos
        vm.init(uid, alias, puebloId);
        vm.loadAll();

        observeUi();

        binding.btnSalir.setOnClickListener(v -> {
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.inicioFragment, true)
                    .build();
            Navigation.findNavController(v)
                    .navigate(R.id.action_global_inicioFragment, null, opts);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() != null) {
            String nuevoPuebloId     = Preferencias.obtenerPuebloId(getContext());
            String nuevoPuebloNombre = Preferencias.obtenerPuebloNombre(getContext());

            // Si el pueblo cambia, recargar
            if (nuevoPuebloId == null ? lastPuebloId != null :
                    !nuevoPuebloId.equals(lastPuebloId)) {

                puebloId     = nuevoPuebloId;
                puebloNombre = nuevoPuebloNombre;
                lastPuebloId = nuevoPuebloId;

                binding.tvPuebloNombre.setText(
                        puebloNombre != null ? puebloNombre : "—"
                );

                vm.init(uid, alias, puebloId);
                vm.loadAll();
            }
        }
    }

    // ==========================================================
    // OBSERVAR UI STATE
    // ==========================================================
    private void observeUi() {
        vm.uiState.observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.message != null) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
                vm.consumeMessage();
            }

            adapter.update(state.disponibles != null ? state.disponibles : Collections.emptyList());

            boolean vacio = state.disponibles == null || state.disponibles.isEmpty();
            binding.tvEmptyDisponibles.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvDisponibles.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    // ==========================================================
    // LISTENER — APUNTARSE / DESAPUNTARSE
    // ==========================================================
    @Override
    public void onApuntarse(Map<String, Object> evento) {
        vm.apuntarse(evento);
    }

    @Override
    public void onDesapuntarse(Map<String, Object> evento) {
        vm.desapuntarse(evento);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
