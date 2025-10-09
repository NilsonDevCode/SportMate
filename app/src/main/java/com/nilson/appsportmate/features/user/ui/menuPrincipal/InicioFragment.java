package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import android.app.AlertDialog;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentInicioBinding;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores.DeporteApuntadoAdapter;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private DeporteApuntadoAdapter adapter;

    // 🔹 Añadido para mostrar el nombre del ayuntamiento
    private FirebaseFirestore db;
    private String ayuntamientoId;
    private String lastAyuntamientoId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        // 🔹 init Firestore
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
                new AlertDialog.Builder(requireContext())
                        .setTitle("Desapuntarse")
                        .setMessage("¿Quieres desapuntarte de \"" + item.nombreDeporte + "\"?")
                        .setPositiveButton("Sí", (d, w) -> viewModel.desapuntarse(item.docId, item.aytoId))
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        binding.rvDeportesApuntados.setAdapter(adapter);

        binding.rvDeportesApuntados.setAdapter(adapter);

        // Ir a deportes disponibles
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

        // 🔹 NUEVO: leer ayuntamientoId de Preferencias y mostrar nombre
        if (getContext() != null) {
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(getContext());
            lastAyuntamientoId = ayuntamientoId;
        }
        cargarNombreAyuntamiento(); // pinta título+nombre

        // Cargar datos
        viewModel.cargarDeportesApuntados();
    }

    // 🔹 NUEVO: refrescar si cambia el ayuntamiento (por ejemplo, al volver de otro fragment)
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

    // 🔹 NUEVO: carga el nombre del ayuntamiento y lo pinta en tvAytoTitulo/tvAytoNombre
    private void cargarNombreAyuntamiento() {
        if (!isAdded()) return;

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            binding.tvAytoTitulo.setText("Sin ayuntamiento asignado");
            binding.tvAytoNombre.setText("—");
            return;
        }

        // título fijo cuando hay ayto configurado
        binding.tvAytoTitulo.setText("Ayuntamiento actual");

        // 1) prefill desde cache si lo tienes guardado
        if (getContext() != null) {
            String cache = Preferencias.obtenerAyuntamientoNombre(getContext());
            if (cache != null && !cache.trim().isEmpty()) {
                binding.tvAytoNombre.setText(cache);
            } else {
                binding.tvAytoNombre.setText("—");
            }
        }

        // 2) refresco online (o cache Firestore) y guardado en preferencias
        db.collection("ayuntamientos")
                .document(ayuntamientoId)
                .get(Source.DEFAULT)
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = extraerNombreAyuntamiento(doc);
                    binding.tvAytoNombre.setText(nombre);

                    // guardamos en prefs para futuras aperturas rápidas
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
