package com.nilson.appsportmate.features.townhall.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.modelos.Deporte;

import java.util.List;

public class DeporteAdapter extends RecyclerView.Adapter<DeporteAdapter.DeporteViewHolder> {

    private List<Deporte> listaDeportes;
    private FirebaseFirestore db;
    private final Listener listener;

    // 🔹 Interfaz para comunicar eventos al Fragment
    public interface Listener {
        void onEditar(Deporte deporte);
        void onEliminar(String deporteId);
    }

    public DeporteAdapter(List<Deporte> listaDeportes, Listener listener) {
        this.listaDeportes = listaDeportes;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public DeporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deporte, parent, false);
        return new DeporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeporteViewHolder holder, int position) {
        Deporte d = listaDeportes.get(position);

        holder.tvNombre.setText(d.getNombre());
        holder.tvFecha.setText("Fecha: " + d.getFecha());
        holder.tvHora.setText("Hora: " + d.getHora());
        holder.tvCupos.setText("Cupos: " + d.getApuntados() + "/" + d.getMax_personas());

        // Click -> editar (avisamos al Fragment)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(d);
            }
        });

        // Long click -> eliminar (avisamos al Fragment)
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onEliminar(d.getId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaDeportes.size();
    }

    public static class DeporteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvHora, tvCupos;

        public DeporteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvCupos = itemView.findViewById(R.id.tvCupos);
        }
    }
}
