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
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentInicioBinding;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores.DeporteApuntadoAdapter;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private DeporteApuntadoAdapter adapter;

    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String lastAyuntamientoId;

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

        cargarFotoPerfil();

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_cambiar_foto_perfil) {
                pickImage.launch("image/*");
                return true;
            } else if (id == R.id.action_cambiar_ayuntamiento) {
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_global_seleccionarNuevoAyuntamientoFragment);
                return true;
            } else if (id == R.id.action_cerrarSesion) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Seguro que quieres cerrar sesión?")
                        .setPositiveButton("Sí, salir", (d, w) -> cerrarSesion())
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            }
            return false;
        });

        pintarSaludo();

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

        binding.btnVerDeportes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_global_deportesDisponiblesFragment)
        );

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

        if (getContext() != null) {
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(getContext());
            lastAyuntamientoId = ayuntamientoId;
        }
        cargarNombreAyuntamiento();
        viewModel.cargarDeportesApuntados();
    }

    @Override
    public void onResume() {
        super.onResume();
        pintarSaludo();
        if (getContext() != null) {
            String nuevo = Preferencias.obtenerAyuntamientoId(getContext());
            if (nuevo == null ? lastAyuntamientoId != null : !nuevo.equals(lastAyuntamientoId)) {
                ayuntamientoId = nuevo;
                lastAyuntamientoId = nuevo;
                cargarNombreAyuntamiento();
            }
        }
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (!isAdded()) return;
        if (uri == null) {
            Toast.makeText(requireContext(), "No se seleccionó imagen.", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.imgFotoPerfil.setImageURI(uri);
        viewModel.subirFotoPerfilUsuario(uri,
                () -> Toast.makeText(requireContext(), "Foto subida correctamente.", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show());
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        if (getContext() != null) Preferencias.borrarTodo(getContext());
        NavOptions opts = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.authFragment, null, opts);
    }

    private void pintarSaludo() {
        if (binding == null || getContext() == null) return;

        String nombre = Preferencias.obtenerNombreUsuario(getContext());

        if (nombre == null || nombre.trim().isEmpty() || "usuario".equalsIgnoreCase(nombre)) {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("usuarios")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String n = null;
                            if (doc.exists()) {
                                Object nom = doc.get("nombre") != null ? doc.get("nombre") : doc.get("alias");
                                n = nom != null ? nom.toString().trim() : null;
                            }
                            if (n != null && !n.isEmpty()) {
                                Preferencias.guardarNombreUsuario(requireContext(), n);
                                binding.tvInicio.setText("Inicio\nBienvenido, " + n);
                            } else {
                                binding.tvInicio.setText("Inicio\nBienvenido, usuario");
                            }
                        })
                        .addOnFailureListener(e ->
                                binding.tvInicio.setText("Inicio\nBienvenido, usuario"));
            } else {
                binding.tvInicio.setText("Inicio\nBienvenido, usuario");
            }
        } else {
            binding.tvInicio.setText("Inicio\nBienvenido, " + nombre);
        }
    }

    private void cargarFotoPerfil() {
        if (!isAdded() || getContext() == null) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String fotoUrl = Preferencias.obtenerFotoUrlUsuario(requireContext());

        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            Glide.with(this).load(fotoUrl).into(binding.imgFotoPerfil);
        } else {
            FirebaseFirestore.getInstance().collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Object urlObj = doc.get("fotoUrl");
                            if (urlObj != null) {
                                String url = urlObj.toString().trim();
                                if (!url.isEmpty()) {
                                    Glide.with(this).load(url).into(binding.imgFotoPerfil);
                                    Preferencias.guardarFotoUrlUsuario(requireContext(), url);
                                }
                            }
                        }
                    });
        }
    }

    private void cargarNombreAyuntamiento() {
        if (!isAdded()) return;

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            binding.tvAytoTitulo.setText("Sin ayuntamiento asignado");
            binding.tvAytoNombre.setText("—");
            return;
        }

        binding.tvAytoTitulo.setText("Ayuntamiento actual");

        if (getContext() != null) {
            String cache = Preferencias.obtenerAyuntamientoNombre(getContext());
            binding.tvAytoNombre.setText(cache != null && !cache.trim().isEmpty() ? cache : "—");
        }

        db.collection("ayuntamientos")
                .document(ayuntamientoId)
                .get()
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

    private String extraerNombreAyuntamiento(com.google.firebase.firestore.DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return "(desconocido)";
        Object n1 = doc.get("nombre");
        Object n2 = doc.get("razonSocial");
        String nom = n1 != null ? String.valueOf(n1) : null;
        if (nom == null || nom.trim().isEmpty() || "null".equalsIgnoreCase(nom)) {
            nom = n2 != null ? String.valueOf(n2) : "(desconocido)";
        }
        return nom;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
