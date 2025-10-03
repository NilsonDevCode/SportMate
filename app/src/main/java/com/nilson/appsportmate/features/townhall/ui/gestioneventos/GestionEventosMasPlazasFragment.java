package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla con lista y CRUD (+/−, editar, borrar, ver inscritos).
 * Usa tu layout:
 *   - List: R.layout.gestion_eventos_mas_plazas
 *   - Item: R.layout.item_evento_gestion
 */
public class GestionEventosMasPlazasFragment extends Fragment {

    private GestionEventosMasPlazasViewModel viewModel;

    private TextView tvEmpty;
    private ProgressBar progress;
    private RecyclerView rvEventos;
    private Button btnVolver;

    private EventosAdapter adapter;
    private String ayuntamientoId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gestion_eventos_mas_plazas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GestionEventosMasPlazasViewModel.class);

        tvEmpty   = root.findViewById(R.id.tvEmpty);
        progress  = root.findViewById(R.id.progress);
        rvEventos = root.findViewById(R.id.rvEventos);
        btnVolver = root.findViewById(R.id.btnVolver);

        ayuntamientoId = Preferencias.obtenerAyuntamientoId(requireContext());

        rvEventos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosAdapter(new EventosAdapter.Listener() {
            @Override public void onMas(String docId) { viewModel.updatePlazas(ayuntamientoId, docId, +1); }
            @Override public void onMenos(String docId) { viewModel.updatePlazas(ayuntamientoId, docId, -1); }
            @Override public void onBorrar(String docId) { confirmarBorrado(docId); }
            @Override public void onInscritos(String docId) { mostrarInscritos(docId); }
            @Override public void onEditar(String docId, Map<String, Object> actual) { mostrarDialogoEditar(docId, actual); }
        });
        rvEventos.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> requireActivity().onBackPressed());

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            progress.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            boolean empty = state.eventos == null || state.eventos.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvEventos.setVisibility(empty ? View.GONE : View.VISIBLE);

            adapter.submit(state.eventos);
            if (state.error != null && !state.error.isEmpty()) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.fetchEventos(ayuntamientoId);
    }

    private void confirmarBorrado(String docId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento?")
                .setPositiveButton("Borrar", (d, w) -> viewModel.deleteEvento(ayuntamientoId, docId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Diálogo simple para editar nombre, fecha, hora y plazas. */
    private void mostrarDialogoEditar(String docId, Map<String, Object> actual) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etNombre = new EditText(requireContext());
        etNombre.setHint("Nombre del deporte");
        etNombre.setText(s(actual.get("nombre")));
        layout.addView(etNombre);

        EditText etFecha = new EditText(requireContext());
        etFecha.setHint("Fecha");
        etFecha.setText(s(actual.get("fecha")));
        layout.addView(etFecha);

        EditText etHora = new EditText(requireContext());
        etHora.setHint("Hora");
        etHora.setText(s(actual.get("hora")));
        layout.addView(etHora);

        EditText etPlazas = new EditText(requireContext());
        etPlazas.setHint("Plazas disponibles");
        etPlazas.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPlazas.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        Long p = asLong(actual.get("plazasDisponibles"));
        etPlazas.setText(p == null ? "" : String.valueOf(p));
        layout.addView(etPlazas);

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar evento")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("nombre", etNombre.getText().toString().trim());
                    fields.put("fecha", etFecha.getText().toString().trim());
                    fields.put("hora", etHora.getText().toString().trim());
                    String plazasTxt = etPlazas.getText().toString().trim();
                    if (!plazasTxt.isEmpty()) {
                        try { fields.put("plazasDisponibles", Long.parseLong(plazasTxt)); }
                        catch (Exception ignored) {}
                    }
                    viewModel.updateEvento(ayuntamientoId, docId, fields);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Lista simple de inscritos (alias) en un AlertDialog. */
    private void mostrarInscritos(String docId) {
        viewModel.fetchInscritos(ayuntamientoId, docId, (aliases, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (aliases == null) aliases = new ArrayList<>();
            if (aliases.isEmpty()) aliases.add("(Sin inscritos)");

            CharSequence[] items = aliases.toArray(new CharSequence[0]);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Inscritos")
                    .setItems(items, null)
                    .setPositiveButton("Cerrar", null)
                    .show();
        });
    }

    private static String s(Object o) {
        if (o == null) return "";
        String x = String.valueOf(o);
        return "null".equalsIgnoreCase(x) ? "" : x;
    }
    private static Long asLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(s(o)); } catch (Exception e) { return null; }
    }

    // =========================
    //   ADAPTER interno
    // =========================
    private static class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.VH> {
        interface Listener {
            void onMas(String docId);
            void onMenos(String docId);
            void onBorrar(String docId);
            void onInscritos(String docId);
            void onEditar(String docId, Map<String, Object> actual);
        }

        private final List<Map<String, Object>> data = new ArrayList<>();
        private final Listener listener;

        EventosAdapter(Listener l) { this.listener = l; }

        void submit(@Nullable List<Map<String, Object>> nuevos) {
            data.clear();
            if (nuevos != null) data.addAll(nuevos);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_evento_gestion, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Map<String, Object> ev = data.get(position);

            String docId   = s(ev.get("idDoc"));
            String nombre  = s(ev.get("nombre"));
            String fecha   = s(ev.get("fecha"));
            String hora    = s(ev.get("hora"));
            String comunidad = s(ev.get("comunidadNombre"));
            String provincia = s(ev.get("provinciaNombre"));
            String ciudad    = s(ev.get("ciudadNombre"));
            String pueblo    = s(ev.get("puebloNombre"));
            Long plazas      = asLong(ev.get("plazasDisponibles"));
            if (plazas == null) plazas = 0L;
            Long inscritos    = asLong(ev.get("inscritosCount"));
            if (inscritos == null) inscritos = 0L;

            StringBuilder titulo = new StringBuilder();
            if (!nombre.isEmpty()) titulo.append(nombre);
            if (!fecha.isEmpty() || !hora.isEmpty()) {
                if (titulo.length() > 0) titulo.append(" - ");
                titulo.append(fecha);
                if (!hora.isEmpty()) titulo.append(" ").append(hora);
            }
            h.tvTitulo.setText(titulo.length() == 0 ? "(Evento)" : titulo.toString());

            StringBuilder ubic = new StringBuilder();
            if (!ciudad.isEmpty())    ubic.append(ciudad);
            if (!provincia.isEmpty()) { if (ubic.length() > 0) ubic.append(", "); ubic.append(provincia); }
            if (!pueblo.isEmpty())    { if (ubic.length() > 0) ubic.append(" · "); ubic.append(pueblo); }
            if (ubic.length() == 0)   ubic.append(comunidad);
            h.tvUbicacion.setText(ubic.length() == 0 ? "(Ubicación)" : ubic.toString());

            h.tvPlazas.setText("Plazas disponibles: " + plazas);
            h.tvInscritosCount.setText("Inscritos: " + inscritos);

            h.btnMas.setOnClickListener(v -> { if (listener != null) listener.onMas(docId); });
            h.btnMenos.setOnClickListener(v -> { if (listener != null) listener.onMenos(docId); });
            h.btnBorrar.setOnClickListener(v -> { if (listener != null) listener.onBorrar(docId); });
            h.btnInscritos.setOnClickListener(v -> { if (listener != null) listener.onInscritos(docId); });
            h.btnEditar.setOnClickListener(v -> { if (listener != null) listener.onEditar(docId, ev); });
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvTitulo, tvUbicacion, tvPlazas, tvInscritosCount;
            final View btnMenos, btnMas, btnInscritos, btnEditar, btnBorrar;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitulo        = itemView.findViewById(R.id.tvTitulo);
                tvUbicacion     = itemView.findViewById(R.id.tvUbicacion);
                tvPlazas        = itemView.findViewById(R.id.tvPlazas);
                tvInscritosCount= itemView.findViewById(R.id.tvInscritosCount);
                btnMenos        = itemView.findViewById(R.id.btnMenos);
                btnMas          = itemView.findViewById(R.id.btnMas);
                btnInscritos    = itemView.findViewById(R.id.btnInscritos);
                btnEditar       = itemView.findViewById(R.id.btnEditar);
                btnBorrar       = itemView.findViewById(R.id.btnBorrar);
            }
        }

        private static String s(Object o) {
            if (o == null) return "";
            String x = String.valueOf(o);
            return "null".equalsIgnoreCase(x) ? "" : x;
        }
        private static Long asLong(Object o) {
            if (o instanceof Number) return ((Number) o).longValue();
            try { return Long.parseLong(s(o)); } catch (Exception e) { return null; }
        }
    }
}
