package com.nilson.appsportmate.caracteristicas.ayuntamiento.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.nilson.appsportmate.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

/**
 * Adapter de gestión de eventos del ayuntamiento.
 * Pinta los datos y delega acciones a la Activity a través de EventoActions.
 */
public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.VH> {

    public interface EventoActions {
        void onIncrementar(String idDoc);
        void onDecrementar(String idDoc);
        void onEditar(Map<String, Object> evento);
        void onBorrar(Map<String, Object> evento);
        void onVerInscritos(String idDoc, String tituloMostrado); // NUEVO: pulsar "Inscritos"
        CollectionReference getInscritosRef(String idDoc);
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvUbicacion, tvPlazas, tvInscritosCount;
        MaterialButton btnMas, btnMenos, btnEditar, btnBorrar, btnInscritos;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitulo        = itemView.findViewById(R.id.tvTitulo);
            tvUbicacion     = itemView.findViewById(R.id.tvUbicacion);
            tvPlazas        = itemView.findViewById(R.id.tvPlazas);
            tvInscritosCount= itemView.findViewById(R.id.tvInscritosCount);
            btnMas          = itemView.findViewById(R.id.btnMas);
            btnMenos        = itemView.findViewById(R.id.btnMenos);
            btnEditar       = itemView.findViewById(R.id.btnEditar);
            btnBorrar       = itemView.findViewById(R.id.btnBorrar);
            btnInscritos    = itemView.findViewById(R.id.btnInscritos);
        }
    }
    private final List<Map<String, Object>> data;
    private final EventoActions actions;

    public EventosAdapter(@NonNull List<Map<String, Object>> data,
                          @NonNull EventoActions actions) {
        this.data = data;
        this.actions = actions;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento_gestion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> ev = data.get(position);

        String idDoc     = String.valueOf(ev.get("idDoc"));
        String nombre    = safeStr(ev.get("nombre"));
        String fecha     = safeStr(ev.get("fecha"));
        String hora      = safeStr(ev.get("hora"));
        String comunidad = safeStr(ev.get("comunidad"));
        String provincia = safeStr(ev.get("provincia"));
        String ciudad    = safeStr(ev.get("ciudad"));
        String pueblo    = safeStr(ev.get("pueblo"));

        long plazas = toLong(ev.get("plazasDisponibles"));

        // Título (solo el nombre) y subtítulo con fecha/hora/ubicación
        h.tvTitulo.setText(nombre);
        String ubicacion = buildUbicacion(ciudad, provincia, pueblo);
        String sub = buildSub(fecha, hora, ubicacion);
        h.tvUbicacion.setText(ubicacion);
        h.tvPlazas.setText("Plazas disponibles: " + plazas);
        h.tvInscritosCount.setText("Inscritos: —");

        // Contador de inscritos (lectura única)
        CollectionReference insRef = actions.getInscritosRef(idDoc);
        if (insRef != null) {
            insRef.get()
                    .addOnSuccessListener(snap ->
                            h.tvInscritosCount.setText("Inscritos: " + snap.size()))
                    .addOnFailureListener(e ->
                            h.tvInscritosCount.setText("Inscritos: —"));
        }

        // Clicks
        h.btnMas.setOnClickListener(v -> actions.onIncrementar(idDoc));
        h.btnMenos.setOnClickListener(v -> actions.onDecrementar(idDoc));
        h.btnEditar.setOnClickListener(v -> actions.onEditar(ev));
        h.btnBorrar.setOnClickListener(v -> actions.onBorrar(ev));
        h.btnInscritos.setOnClickListener(v -> actions.onVerInscritos(idDoc, nombre + " - " + sub));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /* ==================== helpers ==================== */

    private static String safeStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static long toLong(Object o) {
        if (o instanceof Long) return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof String) {
            try { return Long.parseLong((String) o); } catch (Exception ignored) {}
        }
        return 0L;
    }

    private static String buildUbicacion(String ciudad, String provincia, String pueblo) {
        // "Ciudad, Provincia · Pueblo" (ignora vacíos)
        StringBuilder sb = new StringBuilder();
        if (!ciudad.isEmpty()) sb.append(ciudad);
        if (!provincia.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(provincia);
        }
        if (!pueblo.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(pueblo);
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String buildSub(String fecha, String hora, String ubicacion) {
        StringBuilder sb = new StringBuilder();
        if (!fecha.isEmpty()) sb.append(fecha);
        if (!hora.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(hora);
        }
        if (!ubicacion.isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(ubicacion);
        }
        return sb.toString();
    }



}
