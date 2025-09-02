package com.nilson.appsportmate.caracteristicas.ayuntamiento.ui.dialogos;

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
import com.nilson.appsportmate.caracteristicas.ayuntamiento.adaptadores.InscritosAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment para mostrar inscritos en TIEMPO REAL.
 * La Activity es el Host y recibe actualizaciones desde el Presenter,
 * llamando a updateData(...) en este fragment.
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
        adapter = new InscritosAdapter(new ArrayList<>(), new ArrayList<>(), (uid) -> {
            Host host = (Host) requireActivity();
            host.onExpulsarClicked(idDoc, uid);
        });
        rv.setAdapter(adapter);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Inscritos — " + titulo)
                .setView(view)
                .setNegativeButton("Cerrar", (d, w) -> {
                    Host host = (Host) requireActivity();
                    host.onDialogDismissRequested();
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Host host = (Host) requireActivity();
        host.onDialogShown(idDoc, titulo);
    }

    public void updateData(@NonNull List<String> aliases, @NonNull List<String> uids) {
        if (adapter != null) {
            adapter.submit(aliases, uids);
        }
    }
}
