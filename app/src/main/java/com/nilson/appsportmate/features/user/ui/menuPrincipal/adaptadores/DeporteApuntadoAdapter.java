package com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.user.ui.menuPrincipal.InicioUiState;

import java.util.ArrayList;
import java.util.List;

public class DeporteApuntadoAdapter extends RecyclerView.Adapter<DeporteApuntadoAdapter.VH> {

    public interface Listener {
        void onItemClick(InicioUiState.DeporteUi item);
        void onItemLongClick(InicioUiState.DeporteUi item); // ⬅️ nuevo
    }

    private final List<InicioUiState.DeporteUi> data = new ArrayList<>();
    private final Listener listener;

    public DeporteApuntadoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<InicioUiState.DeporteUi> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deporte_apuntado, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        InicioUiState.DeporteUi it = data.get(position);
        h.tvNombre.setText(it.nombreDeporte);
        h.tvFecha.setText("Fecha: " + it.fecha);
        h.tvHora.setText("Hora: " + it.hora);
        h.tvAyto.setText("Ayuntamiento: " + it.ayuntamiento);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(it);
        });

        // ⬇️ Long press para desapuntarse
        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(it);
            return true; // importante: consumir el evento
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvFecha, tvHora, tvAyto;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreDeporte);
            tvFecha  = itemView.findViewById(R.id.tvFecha);
            tvHora   = itemView.findViewById(R.id.tvHora);
            tvAyto   = itemView.findViewById(R.id.tvAyuntamiento);
        }
    }
}
