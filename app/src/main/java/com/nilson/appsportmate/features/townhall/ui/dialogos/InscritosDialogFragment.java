package com.nilson.appsportmate.features.townhall.ui.dialogos;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.townhall.adaptadores.InscritosAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment para mostrar inscritos en TIEMPO REAL.
 * El Host es el **parent fragment** (GestionEventosMasPlazasFragment).
 */
public class InscritosDialogFragment extends DialogFragment {

    public interface Host {
        void onDialogShown(String idDoc, String titulo);

        void onDialogDismissRequested();

        void onExpulsarClicked(String idDoc, String uid);
    }

    private static final String ARG_ID = "idDoc";
    private static final String ARG_TIT = "titulo";

    private String idDoc;
    private String titulo;

    private RecyclerView rv;
    private InscritosAdapter adapter;

    public static InscritosDialogFragment newInstance(String idDoc, String titulo) {
        InscritosDialogFragment f = new InscritosDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ID, idDoc);
        b.putString(ARG_TIT, titulo);
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            idDoc = getArguments().getString(ARG_ID, "");
            titulo = getArguments().getString(ARG_TIT, "");
        }

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_inscritos, null);
        rv = view.findViewById(R.id.rvInscritos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ⬇️ Cambio: confirmación antes de expulsar
        adapter = new InscritosAdapter(new ArrayList<>(), new ArrayList<>(), (uid) -> {
            Host host = getHostFromParent();
            if (host == null)
                return;

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirmar expulsión")
                    .setMessage("¿Seguro que quieres eliminar a este usuario del evento?")
                    .setPositiveButton("Sí, eliminar", (d, w) -> host.onExpulsarClicked(idDoc, uid))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        rv.setAdapter(adapter);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("inscritos — " + titulo)
                .setView(view)
                .setNegativeButton("Cerrar", (d, w) -> {
                    Host host = getHostFromParent();
                    if (host != null) {
                        host.onDialogDismissRequested();
                    }
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Host host = getHostFromParent();
        if (host != null) {
            host.onDialogShown(idDoc, titulo);
        }
    }

    public void updateData(@NonNull List<String> aliases, @NonNull List<String> uids) {
        if (adapter != null) {
            adapter.submit(aliases, uids);
        }
    }

    /** Obtiene el Host del **parent fragment** de forma segura. */
    @Nullable
    private Host getHostFromParent() {
        if (getParentFragment() instanceof Host) {
            return (Host) getParentFragment();
        }
        return null;
    }
}
