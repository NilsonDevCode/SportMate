package com.nilson.appsportmate.features.townhall.adaptadores;

import android.content.Context;
import android.content.Intent;
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
import com.nilson.appsportmate.features.townhall.ui.GestionDeportesAyuntamientoActivity;

import java.util.List;

public class DeporteAdapter extends RecyclerView.Adapter<DeporteAdapter.DeporteViewHolder> {

    private Context context;
    private List<Deporte> listaDeportes;
    private FirebaseFirestore db;

    public DeporteAdapter(Context context, List<Deporte> listaDeportes) {
        this.context = context;
        this.listaDeportes = listaDeportes;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public DeporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deporte, parent, false);
        return new DeporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeporteViewHolder holder, int position) {
        Deporte d = listaDeportes.get(position);

        holder.tvNombre.setText(d.getNombre());
        holder.tvFecha.setText("Fecha: " + d.getFecha());
        holder.tvHora.setText("Hora: " + d.getHora());
        holder.tvCupos.setText("Cupos: " + d.getApuntados() + "/" + d.getMax_personas());

        // Al hacer click -> editar
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, GestionDeportesAyuntamientoActivity.class);
            i.putExtra("id", d.getId());
            i.putExtra("nombre", d.getNombre());
            i.putExtra("fecha", d.getFecha());
            i.putExtra("hora", d.getHora());
            i.putExtra("max_personas", d.getMax_personas());
            i.putExtra("apuntados", d.getApuntados());
            context.startActivity(i);
        });

        // Long click -> eliminar
        holder.itemView.setOnLongClickListener(v -> {
            db.collection("deportes").document(d.getId()).delete()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(context, "Deporte eliminado", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
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
