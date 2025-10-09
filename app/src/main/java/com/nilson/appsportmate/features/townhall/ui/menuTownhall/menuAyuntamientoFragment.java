package com.nilson.appsportmate.features.townhall.ui.menuTownhall;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.databinding.FragmentMenuAyuntamientoBinding;

import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menú principal del ayuntamiento:
 * - Título "Inicio"
 * - Logo (imagen)
 * - Nombre del ayuntamiento (cabecera)
 * - Lista de eventos creados (resumen)
 * - Botón para ir a Gestión de Deportes
 * - Menú superior: cambiar foto, gestionar eventos de más plazas, salir
 */
public class menuAyuntamientoFragment extends Fragment {

    private FragmentMenuAyuntamientoBinding binding;
    private MenuAyuntamientoViewModel vm;

    // Lista de eventos para el adapter
    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosResumenAdapter adapter;

    // Picker de imagen para “Cambiar foto de perfil”
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuAyuntamientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(MenuAyuntamientoViewModel.class);

        // ===== Toolbar (sin "cambiar ayuntamiento") =====
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_cambiar_foto_perfil) {
                pickImage.launch("image/*");
                return true;

            } else if (id == R.id.action_gestion_eventos_mas_plazas) {
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_global_gestionEventosMasPlazasFragment);
                return true;

            } else if (id == R.id.action_cerrarSesion) {
                confirmarLogout();
                return true;
            }
            return false;
        });

        // ===== RecyclerView (adapter resumen) =====
        binding.rvEventos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosResumenAdapter();
        binding.rvEventos.setAdapter(adapter);

        // ===== Botón: ir a Gestión de Deportes =====
        binding.btnGestionDeportes.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_global_gestionDeportesAyuntamientoFragment)
        );

        // ===== Observers =====
        vm.getAyuntamientoNombre().observe(getViewLifecycleOwner(),
                nombre -> binding.tvAytoNombre.setText(
                        TextUtils.isEmpty(nombre) ? "Sin ayuntamiento asignado" : nombre));

        vm.getEventos().observe(getViewLifecycleOwner(), evs -> {
            listaEventos.clear();
            if (evs != null) listaEventos.addAll(evs);
            adapter.notifyDataSetChanged();

            binding.tvEmpty.setVisibility(listaEventos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        vm.getMensaje().observe(getViewLifecycleOwner(),
                msg -> { if (!TextUtils.isEmpty(msg)) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); });

        // ===== Cargar datos =====
        vm.cargarDatosAyuntamiento(requireContext());
        vm.cargarEventos(requireContext());
    }

    /* ============================ Menú: acciones ============================ */

    private void onImagePicked(@Nullable Uri uri) {
        if (!isAdded()) return;
        if (uri == null) {
            Toast.makeText(requireContext(), "No se seleccionó imagen.", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.imgLogo.setImageURI(uri);
        Toast.makeText(requireContext(), "Imagen seleccionada.", Toast.LENGTH_SHORT).show();
        // Aquí podrías subirla a Storage si quieres.
    }

    private void confirmarLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí, salir", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    Preferencias.borrarTodo(requireContext());
                    NavOptions opts = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build();
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.authFragment, null, opts);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /* ======================== Adapter resumen interno ======================== */
    private class EventosResumenAdapter extends RecyclerView.Adapter<EventosResumenAdapter.VH> {

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvSub, tvPlazas, tvInscritos;
            // Reutilizamos ids del layout item_evento_gestion
            MaterialButton btnMas, btnMenos, btnEditar, btnBorrar, btnInscritos;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitulo    = itemView.findViewById(R.id.tvTitulo);
                tvSub       = itemView.findViewById(R.id.tvUbicacion);      // aquí pondremos la fecha
                tvPlazas    = itemView.findViewById(R.id.tvPlazas);
                tvInscritos = itemView.findViewById(R.id.tvInscritosCount);

                btnMas      = itemView.findViewById(R.id.btnMas);
                btnMenos    = itemView.findViewById(R.id.btnMenos);
                btnEditar   = itemView.findViewById(R.id.btnEditar);
                btnBorrar   = itemView.findViewById(R.id.btnBorrar);
                btnInscritos= itemView.findViewById(R.id.btnInscritos);

                // Ocultamos todos los botones en esta pantalla
                if (btnMas != null)       btnMas.setVisibility(View.GONE);
                if (btnMenos != null)     btnMenos.setVisibility(View.GONE);
                if (btnEditar != null)    btnEditar.setVisibility(View.GONE);
                if (btnBorrar != null)    btnBorrar.setVisibility(View.GONE);
                if (btnInscritos != null) btnInscritos.setVisibility(View.GONE);
            }
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_evento_gestion, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Map<String, Object> ev = listaEventos.get(position);

            String idDoc   = String.valueOf(ev.get("idDoc"));
            String nombre  = safe(ev.get("nombre"));
            String fecha   = safe(ev.get("fecha"));
            String hora    = safe(ev.get("hora"));
            long plazas    = toLong(ev.get("plazasDisponibles"));

            h.tvTitulo.setText(nombre.isEmpty() ? "-" : nombre);

            // Solo fecha (y hora si existe)
            String sub = fecha;
            if (!hora.isEmpty()) sub = sub.isEmpty() ? hora : (sub + " " + hora);
            h.tvSub.setText(sub.isEmpty() ? "-" : sub);

            h.tvPlazas.setText("Plazas disponibles: " + plazas);
            h.tvInscritos.setText("Inscritos: —");

            // Contador de inscritos
            CollectionReference ref = vm.getInscritosRef(idDoc);
            if (ref != null) {
                ref.get()
                        .addOnSuccessListener(snap -> h.tvInscritos.setText("Inscritos: " + snap.size()))
                        .addOnFailureListener(e -> h.tvInscritos.setText("Inscritos: —"));
            }
        }

        @Override
        public int getItemCount() { return listaEventos.size(); }

        private String safe(Object o) { return o == null ? "" : String.valueOf(o).trim(); }
        private long toLong(Object o) {
            if (o instanceof Long) return (Long) o;
            if (o instanceof Integer) return ((Integer) o).longValue();
            if (o instanceof String) {
                try { return Long.parseLong((String) o); } catch (Exception ignored) {}
            }
            return 0L;
        }
    }

    /* ======================================================================= */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
