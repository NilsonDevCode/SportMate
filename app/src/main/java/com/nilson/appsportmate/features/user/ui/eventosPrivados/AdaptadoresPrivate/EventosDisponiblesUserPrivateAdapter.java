package com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventosDisponiblesUserPrivateAdapter
        extends RecyclerView.Adapter<EventosDisponiblesUserPrivateAdapter.VH> {

    public interface Listener {
        void onApuntarse(Map<String, Object> evento);
        void onDesapuntarse(Map<String, Object> evento);
    }

    private List<Map<String, Object>> data = new ArrayList<>();
    private final Listener listener;

    private final Set<String> apuntados = new HashSet<>();

    // 🔥 Cache local de puebloId → nombre
    private final Map<String, String> cachePueblos = new HashMap<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventosDisponiblesUserPrivateAdapter(List<Map<String, Object>> data, Listener listener) {
        if (data != null) this.data = new ArrayList<>(data);
        this.listener = listener;
    }

    public void update(List<Map<String, Object>> nuevos) {
        if (nuevos == null) return;
        this.data = new ArrayList<>(nuevos);
        notifyDataSetChanged();
    }

    public void markApuntado(String idDoc) {
        if (idDoc == null) return;
        apuntados.add(idDoc);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_deportes_disponibles_user_private, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Map<String, Object> d = data.get(position);

        String idDoc       = str(d.get("idDoc"));
        String nombre      = str(d.get("nombre"));
        String fecha       = str(d.get("fecha"));
        String hora        = str(d.get("hora"));
        String plazas      = str(d.get("plazasDisponibles"));
        String descripcion = str(d.get("descripcion"));
        String reglas      = str(d.get("reglas"));
        String material    = firstNonEmpty(str(d.get("materiales")), str(d.get("material")));

        // ------- AQUÍ ES DONDE MEJORAMOS -------
        String puebloId = str(d.get("puebloId"));
        String url      = str(d.get("url"));

        // Texto final a mostrar (nombre pueblo + url si existe)
        String infoPueblo = "—";

        if (!TextUtils.isEmpty(puebloId)) {

            if (cachePueblos.containsKey(puebloId)) {
                // ✔ Ya lo tenemos en cache → instantáneo
                String nombrePueblo = cachePueblos.get(puebloId);
                infoPueblo = "Pueblo: " + nombrePueblo +
                        (!TextUtils.isEmpty(url) ? "\nUbi: " + url : "");
            } else {
                // ✔ Lo cargamos de Firestore solo una vez
                db.collection("pueblos")
                        .document(puebloId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String nombreReal = doc.getString("nombre");
                            if (!TextUtils.isEmpty(nombreReal)) {

                                cachePueblos.put(puebloId, nombreReal);

                                // refrescar item concreto
                                notifyItemChanged(h.getAdapterPosition());
                            }
                        });
                // Mientras carga mostramos provisionalmente
                infoPueblo = !TextUtils.isEmpty(url) ? url : "Cargando pueblo…";
            }

        } else {
            // Si no hay puebloId, mostramos solo la URL
            infoPueblo = TextUtils.isEmpty(url) ? "—" : url;
        }

        // ------- FIN MEJORA -------

        h.tvNombre.setText(nonEmpty(nombre, "(Sin nombre)"));
        h.tvFecha.setText("Fecha: " + nonEmpty(fecha, "—"));
        h.tvHora.setText("Hora: " + nonEmpty(hora, "—"));
        h.tvPlazas.setText("Plazas: " + nonEmpty(plazas, "0"));
        h.tvDescripcion.setText("Descripción: " + nonEmpty(descripcion, "—"));
        h.tvReglas.setText("Reglas: " + nonEmpty(reglas, "—"));
        h.tvMaterial.setText("Material: " + nonEmpty(material, "—"));

        // ✔ Ahora sí mostramos el NOMBRE del pueblo
        h.tvUrl.setText(infoPueblo);

        boolean yaApuntado = apuntados.contains(idDoc);

        h.btnApuntarse.setText(yaApuntado ? "Apuntado" : "Apuntarse");
        h.btnApuntarse.setEnabled(!yaApuntado);

        h.btnApuntarse.setOnClickListener(v -> {
            if (!yaApuntado) listener.onApuntarse(d);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static String str(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private static String nonEmpty(String s, String def) {
        return TextUtils.isEmpty(s) ? def : s;
    }

    private static String firstNonEmpty(String a, String b) {
        return !TextUtils.isEmpty(a) ? a : (!TextUtils.isEmpty(b) ? b : "");
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvHora, tvPlazas, tvDescripcion, tvReglas, tvMaterial, tvUrl;
        Button btnApuntarse;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre      = itemView.findViewById(R.id.tvNombre);
            tvFecha       = itemView.findViewById(R.id.tvFecha);
            tvHora        = itemView.findViewById(R.id.tvHora);
            tvPlazas      = itemView.findViewById(R.id.tvPlazas);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvReglas      = itemView.findViewById(R.id.tvReglas);
            tvMaterial    = itemView.findViewById(R.id.tvMaterial);
            tvUrl         = itemView.findViewById(R.id.tvUrl);
            btnApuntarse  = itemView.findViewById(R.id.btnApuntarse);
        }
    }
}
