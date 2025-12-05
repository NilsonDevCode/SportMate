package com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
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
        String urlPueblo   = str(d.get("urlPueblo"));
        String material    = firstNonEmpty(str(d.get("materiales")), str(d.get("material")));

        String puebloId    = str(d.get("puebloId"));
        String urlEvento   = str(d.get("url"));

        // ---------- DATOS VISUALES ----------
        h.tvNombre.setText(nonEmpty(nombre, "(Sin nombre)"));
        h.tvFecha.setText("Fecha: " + nonEmpty(fecha, "—"));
        h.tvHora.setText("Hora: " + nonEmpty(hora, "—"));
        h.tvPlazas.setText("Plazas: " + nonEmpty(plazas, "0"));
        h.tvDescripcion.setText("Descripción: " + nonEmpty(descripcion, "—"));
        h.tvReglas.setText("Reglas: " + nonEmpty(reglas, "—"));
        h.tvMaterial.setText("Material: " + nonEmpty(material, "—"));

        // -------------- URL EVENTO + URL PUEBLO -----------------

        String textoFinal = "URL del evento : " + nonEmpty(urlPueblo, "—");

        SpannableString spannable = new SpannableString(textoFinal);

        if (!TextUtils.isEmpty(urlPueblo)) {
            int start = textoFinal.indexOf(urlPueblo);
            int end = start + urlPueblo.length();

            if (start >= 0) {
                spannable.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlPueblo));
                        widget.getContext().startActivity(intent);
                    }
                }, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        h.tvUrl.setText(spannable);
        h.tvUrl.setMovementMethod(LinkMovementMethod.getInstance()); // Solo el pueblo es clicable

        // -------- BOTÓN APUNTARSE --------
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
