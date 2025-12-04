package com.nilson.appsportmate.features.user.ui.eventosPrivados.VerEventosApuntadoPrivate;

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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentVerEventosApuntadoPrivateBinding;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate.EventosApuntadosUserPrivateAdapter;

public class VerEventosApuntadoPrivateFragment extends Fragment {

    private FragmentVerEventosApuntadoPrivateBinding binding;
    private VerEventosApuntadoPrivateViewModel viewModel;
    private EventosApuntadosUserPrivateAdapter adapter;

    private FirebaseFirestore db;
    private String ubicacionActualId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentVerEventosApuntadoPrivateBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(VerEventosApuntadoPrivateViewModel.class);

        cargarFotoPerfil();
        cargarUbicacionActual();

        // ---------------------------
        // CONFIGURAR ADAPTER
        // ---------------------------
        binding.rvDeportesApuntados.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventosApuntadosUserPrivateAdapter(
                new EventosApuntadosUserPrivateAdapter.Listener() {

                    @Override
                    public void onItemClick(VerEventosApuntadoPrivateViewModel.EventoUi item) {
                        Toast.makeText(requireContext(),
                                item.nombre + " - " + item.lugar,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onItemLongClick(VerEventosApuntadoPrivateViewModel.EventoUi item) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Desapuntarse")
                                .setMessage("¿Quieres desinscribirte de \"" + item.nombre + "\"?")
                                .setPositiveButton("Sí", (d, w) -> viewModel.desapuntarse(item))
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                }
        );

        binding.rvDeportesApuntados.setAdapter(adapter);

        // Botón Ver eventos disponibles
        binding.btnVerDeportes.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_global_eventosDisponiblesUserPrivateFragment)
        );

        // OBSERVER
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {

            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.error != null)
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();

            if (state.message != null)
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();

            adapter.submitList(state.eventos);

            binding.tvEmpty.setVisibility(
                    state.eventos == null || state.eventos.isEmpty()
                            ? View.VISIBLE : View.GONE
            );
        });

        viewModel.loadEventosApuntados();
    }

    // =====================================================
    // FOTO PERFIL — SOLO MOSTRAR
    // =====================================================
    private void cargarFotoPerfil() {
        if (!isAdded()) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String cached = Preferencias.obtenerFotoUrlUsuario(requireContext());

        if (cached != null && !cached.isEmpty()) {
            Glide.with(this).load(cached).into(binding.imgFotoPerfil);
            return;
        }

        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String url = doc.getString("fotoUrl");

                    if (url != null && !url.isEmpty()) {
                        Preferencias.guardarFotoUrlUsuario(requireContext(), url);
                        Glide.with(this).load(url).into(binding.imgFotoPerfil);
                    }
                });
    }

    // =====================================================
    // UBICACIÓN ACTUAL
    // =====================================================
    private void cargarUbicacionActual() {
        if (!isAdded()) return;

        ubicacionActualId = Preferencias.obtenerPuebloId(requireContext());
        binding.tvUbicacionTitulo.setText("Ubicación Actual");

        if (ubicacionActualId == null) {
            binding.tvAytoNombre.setText("—");
            return;
        }

        db.collection("pueblos")
                .document(ubicacionActualId)
                .get()
                .addOnSuccessListener(doc -> {
                    String nombre = doc.getString("nombre");
                    binding.tvAytoNombre.setText(nombre != null ? nombre : "—");
                })
                .addOnFailureListener(e -> binding.tvAytoNombre.setText("(desconocido)"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
