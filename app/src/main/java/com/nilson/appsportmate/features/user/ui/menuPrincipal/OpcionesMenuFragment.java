package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentInicioBinding;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores.DeporteApuntadoAdapter;

public class OpcionesMenuFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private DeporteApuntadoAdapter adapter;

    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String lastAyuntamientoId;

    // Picker para "Cambiar foto de perfil" (abre galería)
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        // ===== Toolbar: manejar menú aquí =====
        // Asegúrate de que en el XML el toolbar tiene app:menu="@menu/menu_inicio"
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_cambiar_foto_perfil) {
                // Abre selector de imagen
                pickImage.launch("image/*");
                return true;
            } else if (id == R.id.action_cambiar_ayuntamiento) {
                // Navega al selector de ayuntamiento (acción global o local, ya existe en tu nav_graph)
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_global_seleccionarNuevoAyuntamientoFragment);
                return true;
            }
            return false;
        });

        // ===== RecyclerView
        binding.rvDeportesApuntados.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DeporteApuntadoAdapter(new DeporteApuntadoAdapter.Listener() {
            @Override
            public void onItemClick(InicioUiState.DeporteUi item) {
                Toast.makeText(requireContext(),
                        item.nombreDeporte + " - " + item.ayuntamiento,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(InicioUiState.DeporteUi item) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Desapuntarse")
                        .setMessage("¿Quieres desapuntarte de \"" + item.nombreDeporte + "\"?")
                        .setPositiveButton("Sí", (d, w) -> viewModel.desapuntarse(item.docId, item.aytoId))
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        binding.rvDeportesApuntados.setAdapter(adapter);

        // Botón: Ver deportes disponibles
        binding.btnVerDeportes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_global_deportesDisponiblesFragment)
        );

        // Observers
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.error != null && !state.error.isEmpty()) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }
            if (state.message != null && !state.message.isEmpty()) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
            }

            adapter.submit(state.deportes);
            binding.tvEmpty.setVisibility(
                    state.deportes == null || state.deportes.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        // Ayto desde preferencias y nombre en cabecera
        if (getContext() != null) {
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(getContext());
            lastAyuntamientoId = ayuntamientoId;
        }
        cargarNombreAyuntamiento();

        // Cargar deportes del usuario
        viewModel.cargarDeportesApuntados();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            String nuevo = Preferencias.obtenerAyuntamientoId(getContext());
            if (nuevo == null ? lastAyuntamientoId != null : !nuevo.equals(lastAyuntamientoId)) {
                ayuntamientoId = nuevo;
                lastAyuntamientoId = nuevo;
                cargarNombreAyuntamiento();
            }
        }
    }

    /** Pinta el nombre del ayuntamiento en la cabecera. */
    private void cargarNombreAyuntamiento() {
        if (!isAdded()) return;

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            binding.tvAytoTitulo.setText("Sin ayuntamiento asignado");
            binding.tvAytoNombre.setText("—");
            return;
        }

        binding.tvAytoTitulo.setText("Ayuntamiento actual");

        // Prefill rápido desde preferencias
        if (getContext() != null) {
            String cache = Preferencias.obtenerAyuntamientoNombre(getContext());
            if (cache != null && !cache.trim().isEmpty()) {
                binding.tvAytoNombre.setText(cache);
            } else {
                binding.tvAytoNombre.setText("—");
            }
        }

        // Refresco desde Firestore
        db.collection("ayuntamientos")
                .document(ayuntamientoId)
                .get(Source.DEFAULT)
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = extraerNombreAyuntamiento(doc);
                    binding.tvAytoNombre.setText(nombre);

                    if (getContext() != null && nombre != null && !nombre.trim().isEmpty()
                            && !"(desconocido)".equals(nombre)) {
                        Preferencias.guardarAyuntamientoNombre(getContext(), nombre);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    binding.tvAytoTitulo.setText("Error al cargar");
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

    private void onImagePicked(@Nullable Uri uri) {
        if (!isAdded()) return;
        if (uri == null) {
            Toast.makeText(requireContext(), "No se seleccionó imagen.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Aquí ya tienes la URI de la foto. Sube a tu Storage o muéstrala.
        // Ejemplo rápido: ponerla en la ImageView (si quieres previsualizar):
        binding.imgFotoPerfil.setImageURI(uri);
        Toast.makeText(requireContext(), "Foto seleccionada.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
