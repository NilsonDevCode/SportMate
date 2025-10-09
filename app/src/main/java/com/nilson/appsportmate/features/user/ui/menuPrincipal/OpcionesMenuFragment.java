package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentInicioBinding;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores.DeporteApuntadoAdapter;

public class OpcionesMenuFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private DeporteApuntadoAdapter adapter;

    // Selector de imagen para "Cambiar foto de perfil"
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        // RecyclerView
        binding.rvDeportesApuntados.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DeporteApuntadoAdapter(new DeporteApuntadoAdapter.Listener() {
            @Override
            public void onItemClick(InicioUiState.DeporteUi item) {
                // Click corto (opcional)
                Toast.makeText(requireContext(),
                        item.nombreDeporte + " - " + item.ayuntamiento,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(InicioUiState.DeporteUi item) {
                // Long-press -> confirmar desapuntarse
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Desapuntarse")
                        .setMessage("¿Quieres desapuntarte de \"" + item.nombreDeporte + "\"?")
                        .setPositiveButton("Sí", (d, w) -> viewModel.desapuntarse(item.docId, item.aytoId))
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        binding.rvDeportesApuntados.setAdapter(adapter);

        binding.rvDeportesApuntados.setAdapter(adapter);

        // Toolbar: listener del menú (el menú ya está en el XML con app:menu)
        binding.toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);

        // Lanzador para seleccionar imagen de galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onProfileImagePicked
        );

        // Botón "Ver deportes disponibles" -> navegar
        binding.btnVerDeportes.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_inicioFragment_to_deportesDisponiblesFragment)
        );

        // Observers
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }

            adapter.submit(state.deportes);
            binding.tvEmpty.setVisibility(state.deportes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Cargar datos
        viewModel.cargarDeportesApuntados();
    }

    /** Manejo de clics del menú de la Toolbar */
    private boolean onToolbarMenuItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cerrarSesion) {
            // Salir = cerrar sesión y volver a Auth
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(requireView())
                    .navigate(R.id.authFragment);
            return true;

        } else if (id == R.id.action_cambiar_foto_perfil) {
            // Abre selector de imagen (galería)
            pickImageLauncher.launch("image/*");
            return true;

        } else if (id == R.id.action_cambiar_ayuntamiento) {
            // Navegar al flujo de selección de nuevo ayuntamiento
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_inicioFragment_to_seleccionarNuevoAyuntamientoFragment);
            return true;
        }

        return false;
    }

    /** Resultado de seleccionar foto de perfil */
    private void onProfileImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), "No se seleccionó imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        // Mostrar de inmediato en la UI
        binding.imgFotoPerfil.setImageURI(uri);

        // TODO: mover a ViewModel de "Perfil":
        // - Subir a Firebase Storage
        // - Guardar URL en Firestore (usuarios/{uid}/fotoUrl)
        // - Refrescar UI tras persistir
        Toast.makeText(requireContext(), "Foto seleccionada (pendiente subir y guardar)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
