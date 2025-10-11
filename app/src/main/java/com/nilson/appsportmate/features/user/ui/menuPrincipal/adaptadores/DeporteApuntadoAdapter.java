package com.nilson.appsportmate.features.user.ui.menuPrincipal.adaptadores;

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
import com.nilson.appsportmate.features.user.ui.menuPrincipal.InicioUiState;

public class DeporteApuntadoAdapter extends ListAdapter<InicioUiState.DeporteUi, DeporteApuntadoAdapter.VH> {

    public interface Listener {
        void onItemClick(InicioUiState.DeporteUi item);
        void onItemLongClick(InicioUiState.DeporteUi item);
    }

    private final Listener listener;

    public DeporteApuntadoAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<InicioUiState.DeporteUi> DIFF =
            new DiffUtil.ItemCallback<InicioUiState.DeporteUi>() {
                @Override
                public boolean areItemsTheSame(@NonNull InicioUiState.DeporteUi a, @NonNull InicioUiState.DeporteUi b) {
                    return a.docId != null && a.docId.equals(b.docId);
                }
                @Override
                public boolean areContentsTheSame(@NonNull InicioUiState.DeporteUi a, @NonNull InicioUiState.DeporteUi b) {
                    return str(a.nombreDeporte).equals(str(b.nombreDeporte)) &&
                            str(a.descripcion).equals(str(b.descripcion)) &&
                            str(a.fecha).equals(str(b.fecha)) &&
                            str(a.hora).equals(str(b.hora)) &&
                            str(a.lugar).equals(str(b.lugar)) &&
                            a.plazasMax == b.plazasMax &&
                            a.inscritos == b.inscritos &&
                            str(a.ayuntamiento).equals(str(b.ayuntamiento));
                }
                private String str(String s){ return s==null? "": s; }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deporte_apuntado, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        InicioUiState.DeporteUi it = getItem(pos);

        h.tvNombreDeporte.setText(nz(it.nombreDeporte));
        h.tvDescripcion.setText(nz(it.descripcion));
        h.tvFecha.setText(nz(it.fecha));
        h.tvHora.setText(nz(it.hora));
        h.tvLugar.setText(nz(it.lugar));

        h.tvPlazas.setText("PLAZAS > " + it.plazasMax);
        h.tvInscritos.setText("INSCRITOS > " + it.inscritos);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(it); });
        h.itemView.setOnLongClickListener(v -> { if (listener != null) listener.onItemLongClick(it); return true; });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombreDeporte, tvDescripcion, tvFecha, tvHora, tvLugar, tvPlazas, tvInscritos;
        ImageView ivDummy1, ivDummy2; // (por si quieres setear iconos dinámicos en el futuro)

        VH(@NonNull View v) {
            super(v);
            tvNombreDeporte = v.findViewById(R.id.tvNombreDeporte);
            tvDescripcion   = v.findViewById(R.id.tvDescripcionActividad);
            tvFecha         = v.findViewById(R.id.tvFecha);
            tvHora          = v.findViewById(R.id.tvHora);
            tvLugar         = v.findViewById(R.id.tvLugar);
            tvPlazas        = v.findViewById(R.id.tvPlazas);
            tvInscritos     = v.findViewById(R.id.tvInscritos);
        }
    }

    public void submit(java.util.List<InicioUiState.DeporteUi> items){ submitList(items); }

    private String nz(String s){ return s==null? "": s; }
}
