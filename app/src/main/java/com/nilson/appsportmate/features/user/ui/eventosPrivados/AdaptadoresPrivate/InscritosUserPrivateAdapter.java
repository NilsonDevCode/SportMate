package com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nilson.appsportmate.R;

import java.util.ArrayList;
import java.util.List;

public class InscritosUserPrivateAdapter extends RecyclerView.Adapter<InscritosUserPrivateAdapter.VH> {

    // Listener para expulsar participantes
    public interface OnExpulsar {
        void onClick(String uid);
    }

    private final List<String> aliases = new ArrayList<>();
    private final List<String> uids = new ArrayList<>();
    private final OnExpulsar onExpulsar;

    public InscritosUserPrivateAdapter(List<String> aliases, List<String> uids, OnExpulsar onExpulsar) {
        if (aliases != null) this.aliases.addAll(aliases);
        if (uids != null) this.uids.addAll(uids);
        this.onExpulsar = onExpulsar;
    }

    /** Para refrescar la lista cuando cambien los inscritos */
    public void submit(List<String> newAliases, List<String> newUids) {
        aliases.clear();
        uids.clear();
        if (newAliases != null) aliases.addAll(newAliases);
        if (newUids != null) uids.addAll(newUids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_inscritos_user_private, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String alias = aliases.get(position);
        String uid   = uids.get(position);

        h.tvAlias.setText(alias);

        h.btnExpulsar.setOnClickListener(v ->
                onExpulsar.onClick(uid)
        );
    }

    @Override
    public int getItemCount() {
        return aliases.size();
    }

    /* ===== ViewHolder ===== */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvAlias;
        ImageButton btnExpulsar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAlias = itemView.findViewById(R.id.tvAlias);
            btnExpulsar = itemView.findViewById(R.id.btnExpulsar);
        }
    }
}
