package com.nilson.appsportmate.features.user.ui.eventosPrivados.gestionEventosPrivate;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.eventosAdapterPrivate.EventosUserPrivateAdapter;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.eventosAdapterPrivate.InscritosUserPrivateAdapter;
import com.nilson.appsportmate.features.townhall.ui.dialogos.InscritosDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GestionEventosUserPrivateFragment extends Fragment
        implements EventosUserPrivateAdapter.EventoActions, InscritosDialogFragment.Host {

    private RecyclerView rvEventos;
    private TextView tvEmpty;
    private ProgressBar progress;
    private MaterialButton btnVolver;

    private String uidUsuario;
    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosUserPrivateAdapter adapter;

    private GestionEventosUserPrivateViewModel vm;

    private InscritosDialogFragment inscritosDialog;

    public GestionEventosUserPrivateFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gestion_eventos_user_private, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        bindViews(v);
        setupRecycler();

        uidUsuario = Preferencias.obtenerUid(requireContext());

        if (TextUtils.isEmpty(uidUsuario)) {
            Toast.makeText(requireContext(), "Error: uid no encontrado", Toast.LENGTH_LONG).show();
            Navigation.findNavController(v).navigate(R.id.loginFragment);
            return;
        }

        vm = new ViewModelProvider(this).get(GestionEventosUserPrivateViewModel.class);
        vm.setUidUsuario(uidUsuario);

        observeVm();
        setupClicks();
    }

    @Override
    public void onStart() {
        super.onStart();
        vm.suscribirTiempoRealEventos();
    }

    @Override
    public void onStop() {
        vm.desuscribirTiempoRealEventos();
        vm.dejarDeEscucharInscritos();
        super.onStop();
    }

    private void bindViews(View v) {
        rvEventos = v.findViewById(R.id.rvEventos);
        tvEmpty   = v.findViewById(R.id.tvEmpty);
        progress  = v.findViewById(R.id.progress);
        btnVolver = v.findViewById(R.id.btnVolver);
    }

    private void setupRecycler() {
        rvEventos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosUserPrivateAdapter(listaEventos, this);
        rvEventos.setAdapter(adapter);
    }

    private void observeVm() {
        vm.getLoading().observe(getViewLifecycleOwner(), show ->
                progress.setVisibility(show != null && show ? View.VISIBLE : View.GONE));

        vm.getEmpty().observe(getViewLifecycleOwner(), show ->
                tvEmpty.setVisibility(show != null && show ? View.VISIBLE : View.GONE));

        vm.getMensaje().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        vm.getEventos().observe(getViewLifecycleOwner(), eventos -> {
            listaEventos.clear();
            if (eventos != null) listaEventos.addAll(eventos);
            adapter.notifyDataSetChanged();
        });

        vm.getOpenInscritosEvent().observe(getViewLifecycleOwner(), pair -> {
            if (pair != null) abrirInscritosDialog(pair.first, pair.second);
        });

        vm.getInscritosData().observe(getViewLifecycleOwner(), pair -> {
            if (inscritosDialog != null && pair != null) {
                inscritosDialog.updateData(pair.first, pair.second);
            }
        });
    }

    private void setupClicks() {
        btnVolver.setOnClickListener(view -> {
            NavController nav = Navigation.findNavController(view);
            nav.popBackStack();
        });
    }

    @Override public void onIncrementar(String idDoc) { vm.incrementarPlazas(idDoc); }
    @Override public void onDecrementar(String idDoc) { vm.decrementarPlazas(idDoc); }
    @Override public void onEditar(Map<String, Object> evento) { vm.editarEvento(evento); }
    @Override public void onBorrar(Map<String, Object> evento) { pedirConfirmacionBorrar(evento); }

    @Override
    public void onVerInscritos(String idDoc, String tituloMostrado) {
        vm.abrirInscritosTiempoReal(idDoc, tituloMostrado);
    }

    @Override
    public CollectionReference getInscritosRef(String idDoc) {
        return vm.getInscritosRef(idDoc);
    }

    private void pedirConfirmacionBorrar(Map<String, Object> evento) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento privado?")
                .setPositiveButton("Borrar", (d, w) -> {
                    String idDoc = String.valueOf(evento.get("idDoc"));
                    vm.borrarEvento(idDoc);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirInscritosDialog(String idDoc, String titulo) {
        inscritosDialog = InscritosDialogFragment.newInstance(idDoc, titulo);
        inscritosDialog.show(getChildFragmentManager(), "inscritos_dialog");
    }

    @Override public void onDialogShown(String idDoc, String titulo) {}

    @Override
    public void onDialogDismissRequested() {
        vm.cerrarInscritosTiempoReal();
        if (inscritosDialog != null) {
            inscritosDialog.dismissAllowingStateLoss();
            inscritosDialog = null;
        }
    }

    @Override
    public void onExpulsarClicked(String idDoc, String uid) {
        vm.expulsarInscrito(idDoc, uid);
    }
}
