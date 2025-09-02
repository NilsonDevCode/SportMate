package com.nilson.appsportmate.caracteristicas.ayuntamiento.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.comun.modelos.Ayuntamiento;

import java.util.List;

public class AyuntamientoAdapter extends RecyclerView.Adapter<AyuntamientoAdapter.AyuntamientoViewHolder> {

    private Context context;
    private List<Ayuntamiento> listaAyuntamientos;
    private OnItemClickListener listener;

    // Interfaz para manejar clics en editar/eliminar
    public interface OnItemClickListener {
        void onEdit(Ayuntamiento ayuntamiento);
        void onDelete(Ayuntamiento ayuntamiento);
    }

    public AyuntamientoAdapter(Context context, List<Ayuntamiento> listaAyuntamientos, OnItemClickListener listener) {
        this.context = context;
        this.listaAyuntamientos = listaAyuntamientos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AyuntamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ayuntamiento, parent, false);
        return new AyuntamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyuntamientoViewHolder holder, int position) {
        Ayuntamiento ayuntamiento = listaAyuntamientos.get(position);

        holder.tvNombre.setText("Nombre: " + ayuntamiento.getNombre());
        holder.tvNumero.setText("Número: " + ayuntamiento.getNumero());
        holder.tvComunidad.setText("Comunidad: " + ayuntamiento.getComunidad());
        holder.tvProvincia.setText("Provincia: " + ayuntamiento.getProvincia());
        holder.tvCiudad.setText("Ciudad: " + ayuntamiento.getCiudad());
        holder.tvPueblo.setText("Pueblo: " + ayuntamiento.getPueblo());
        holder.tvLocalidad.setText("Localidad: " + ayuntamiento.getLocalidad());
        holder.tvUid.setText("UID: " + ayuntamiento.getUid());

        // Botones editar/eliminar
        holder.btnEditar.setOnClickListener(v -> listener.onEdit(ayuntamiento));
        holder.btnEliminar.setOnClickListener(v -> listener.onDelete(ayuntamiento));
    }

    @Override
    public int getItemCount() {
        return listaAyuntamientos.size();
    }

    public static class AyuntamientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvNumero, tvComunidad, tvProvincia, tvCiudad, tvPueblo, tvLocalidad, tvUid;
        ImageButton btnEditar, btnEliminar;

        public AyuntamientoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvComunidad = itemView.findViewById(R.id.tvComunidad);
            tvProvincia = itemView.findViewById(R.id.tvProvincia);
            tvCiudad = itemView.findViewById(R.id.tvCiudad);
            tvPueblo = itemView.findViewById(R.id.tvPueblo);
            tvLocalidad = itemView.findViewById(R.id.tvLocalidad);
            tvUid = itemView.findViewById(R.id.tvUid);

            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
