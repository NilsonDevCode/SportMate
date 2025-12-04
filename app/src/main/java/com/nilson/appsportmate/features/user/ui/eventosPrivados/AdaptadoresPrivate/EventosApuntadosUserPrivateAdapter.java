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

import java.util.Objects;

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

    private static final DiffUtil.ItemCallback<EventoUi> DIFF =
            new DiffUtil.ItemCallback<EventoUi>() {

                @Override
                public boolean areItemsTheSame(@NonNull EventoUi a, @NonNull EventoUi b) {
                    return Objects.equals(a.docId, b.docId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull EventoUi a, @NonNull EventoUi b) {
                    return eq(a.nombre, b.nombre) &&
                            eq(a.descripcion, b.descripcion) &&
                            eq(a.fecha, b.fecha) &&
                            eq(a.hora, b.hora) &&
                            eq(a.lugar, b.lugar) &&
                            a.plazas == b.plazas &&
                            a.inscritos == b.inscritos;
                }

                private boolean eq(String a, String b) {
                    if (a == null) a = "";
                    if (b == null) b = "";
                    return a.equals(b);
                }
            };

    public static class VH extends RecyclerView.ViewHolder {

        TextView tvNombre, tvDescripcion, tvFecha, tvHora, tvLugar, tvPlazas, tvInscritos;
        ImageView ivChevrons;

        public VH(@NonNull View v) {
            super(v);

            tvNombre      = v.findViewById(R.id.tvNombreDeporte);
            tvDescripcion = v.findViewById(R.id.tvDescripcionActividad);
            tvFecha       = v.findViewById(R.id.tvFecha);
            tvHora        = v.findViewById(R.id.tvHora);
            tvLugar       = v.findViewById(R.id.tvLugar);
            tvPlazas      = v.findViewById(R.id.tvPlazas);
            tvInscritos   = v.findViewById(R.id.tvInscritos);
            ivChevrons    = v.findViewById(R.id.ivChevrons);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_deporte_apuntado_private, parent, false);
        return new VH(v);
    }

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

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
