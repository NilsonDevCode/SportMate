package com.nilson.appsportmate.features.townhall.adaptadores;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;

import java.util.List;
import java.util.Map;

public class DeportesDisponiblesAdapter extends RecyclerView.Adapter<DeportesDisponiblesAdapter.VH> {

    public interface Listener {
        void onApuntarse(Map<String, Object> deporte);
        void onDesapuntarse(Map<String, Object> deporte);
    }

    private final List<Map<String, Object>> data;
    private final Listener listener;
    private final boolean esListaDeMisDeportes; // true => mostrar "Desapuntarse"; false => "Apuntarse"

    public DeportesDisponiblesAdapter(List<Map<String, Object>> data, boolean esListaDeMisDeportes, Listener listener) {
        this.data = data;
        this.esListaDeMisDeportes = esListaDeMisDeportes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deporte_disponible, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> d = data.get(position);

        String nombre      = str(d.get("nombre"));
        String fecha       = str(d.get("fecha"));
        String hora        = str(d.get("hora"));
        String plazas      = str(d.get("plazasDisponibles"));
        String descripcion = str(d.get("descripcion"));
        String reglas      = str(d.get("reglas"));

        // Compatibilidad de claves
        String material    = firstNonEmpty(str(d.get("materiales")), str(d.get("material")));
        String url         = firstNonEmpty(str(d.get("urlPueblo")), str(d.get("url")));

        h.tvNombre.setText(nonEmpty(nombre, "(Sin nombre)"));
        h.tvFecha.setText("Fecha: " + nonEmpty(fecha, "—"));
        h.tvHora.setText("Hora: " + nonEmpty(hora, "—"));
        h.tvPlazas.setText("Plazas: " + nonEmpty(plazas, "0"));
        h.tvDescripcion.setText("Descripción: " + nonEmpty(descripcion, "—"));
        h.tvReglas.setText("Reglas: " + nonEmpty(reglas, "—"));
        h.tvMaterial.setText("Material: " + nonEmpty(material, "—"));

        // Para autoLink, mejor solo la URL si existe
        h.tvUrl.setText(TextUtils.isEmpty(url) ? "—" : url);

        if (esListaDeMisDeportes) {
            h.btnApuntarse.setVisibility(View.GONE);
            h.btnDesapuntarse.setVisibility(View.VISIBLE);
        } else {
            h.btnApuntarse.setVisibility(View.VISIBLE);
            h.btnDesapuntarse.setVisibility(View.GONE);
        }

        h.btnApuntarse.setOnClickListener(v -> {
            if (listener != null) listener.onApuntarse(d);
        });

        h.btnDesapuntarse.setOnClickListener(v -> {
            if (listener != null) listener.onDesapuntarse(d);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /* ===== Helpers ===== */
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

    /* ===== ViewHolder ===== */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvPlazas, tvDescripcion, tvReglas, tvMaterial, tvUrl, tvHora;
        Button btnApuntarse, btnDesapuntarse;
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
            btnApuntarse    = itemView.findViewById(R.id.btnApuntarse);
            btnDesapuntarse = itemView.findViewById(R.id.btnDesapuntarse);
        }
    }
}
