package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentInicioBinding;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores.DeporteApuntadoAdapter;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private DeporteApuntadoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        // RecyclerView
        binding.rvDeportesApuntados.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DeporteApuntadoAdapter(item ->
                Toast.makeText(requireContext(),
                        item.nombreDeporte + " - " + item.ayuntamiento,
                        Toast.LENGTH_SHORT).show()
        );
        binding.rvDeportesApuntados.setAdapter(adapter);

        // Navegar a "DeportesDisponiblesFragment" al pulsar el botón
        binding.btnVerDeportes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_global_deportesDisponiblesFragment)
        );
        // Si en tu nav_graph añadiste una action específica desde Inicio -> DeportesDisponibles,
        // usa ese id en lugar del global:
        // Navigation.findNavController(v).navigate(R.id.action_inicioFragment_to_deportesDisponiblesFragment);

        // Observers
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.error != null && !state.error.isEmpty()) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }

            adapter.submit(state.deportes);
            binding.tvEmpty.setVisibility(state.deportes == null || state.deportes.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });

        // Cargar datos
        viewModel.cargarDeportesApuntados();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
