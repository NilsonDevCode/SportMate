package com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.VerEventosApuntadoPrivate.VerEventosApuntadoPrivateViewModel.EventoUi;

public class EventosApuntadosUserPrivateAdapter
        extends ListAdapter<EventoUi, EventosApuntadosUserPrivateAdapter.VH> {

    public interface Listener {
        void onItemClick(EventoUi item);
        void onItemLongClick(EventoUi item);
    }

    private final Listener listener;

    public EventosApuntadosUserPrivateAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    // ============================
    // 🔥 DIFFUTIL PROFESIONAL
    // Igual al del adapter que funciona
    // ============================
    private static final DiffUtil.ItemCallback<EventoUi> DIFF =
            new DiffUtil.ItemCallback<EventoUi>() {
                @Override
                public boolean areItemsTheSame(@NonNull EventoUi a, @NonNull EventoUi b) {
                    return a.docId != null && a.docId.equals(b.docId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull EventoUi a, @NonNull EventoUi b) {
                    return str(a.nombre).equals(str(b.nombre)) &&
                            str(a.descripcion).equals(str(b.descripcion)) &&
                            str(a.fecha).equals(str(b.fecha)) &&
                            str(a.hora).equals(str(b.hora)) &&
                            str(a.lugar).equals(str(b.lugar)) &&
                            a.plazas == b.plazas &&
                            a.inscritos == b.inscritos;
                }

                private String str(String s){ return s == null ? "" : s; }
            };


    // ============================
    // VIEW HOLDER
    // ============================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvNombre, tvDescripcion, tvFecha, tvHora, tvLugar, tvPlazas, tvInscritos;
        ImageView ivDummy1, ivDummy2;

        VH(@NonNull View v) {
            super(v);

            tvNombre      = v.findViewById(R.id.tvNombreDeporte);
            tvDescripcion = v.findViewById(R.id.tvDescripcionActividad);
            tvFecha       = v.findViewById(R.id.tvFecha);
            tvHora        = v.findViewById(R.id.tvHora);
            tvLugar       = v.findViewById(R.id.tvLugar);
            tvPlazas      = v.findViewById(R.id.tvPlazas);
            tvInscritos   = v.findViewById(R.id.tvInscritos);


        }
    }

    // ============================
    // ON CREATE
    // ============================
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_deporte_apuntado_private, parent, false);
        return new VH(v);
    }

    // ============================
    // ON BIND (MISMO FORMATO DE INICIO)
    // ============================
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        EventoUi it = getItem(pos);

        h.tvNombre.setText(nz(it.nombre));
        h.tvDescripcion.setText(nz(it.descripcion));
        h.tvFecha.setText(nz(it.fecha));
        h.tvHora.setText(nz(it.hora));
        h.tvLugar.setText(nz(it.lugar));

        h.tvPlazas.setText("PLAZAS > " + it.plazas);
        h.tvInscritos.setText("INSCRITOS > " + it.inscritos);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(it);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(it);
            return true;
        });
    }

    private String nz(String s){ return s == null ? "" : s; }
}
