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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.AdaptadoresPrivate.EventosUserPrivateAdapter;
import com.nilson.appsportmate.features.townhall.ui.dialogos.InscritosDialogFragment;
import com.nilson.appsportmate.features.user.ui.eventosPrivados.menuBase.BaseUserPrivateFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionEventosUserPrivateFragment extends BaseUserPrivateFragment
        implements EventosUserPrivateAdapter.EventoActions,
        InscritosDialogFragment.Host {

    // ==============================
    // UI
    // ==============================
    private MaterialToolbar toolbar;
    private RecyclerView rvEventos;
    private TextView tvEmpty;
    private ProgressBar progress;
    private MaterialButton btnVolver;

    // ==============================
    // DATA
    // ==============================
    private String uidUsuario;
    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosUserPrivateAdapter adapter;

    private GestionEventosUserPrivateViewModel vm;
    private InscritosDialogFragment inscritosDialog;

    // ==============================
    // LAYOUT
    // ==============================
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_gestion_eventos_user_private,
                container,
                false
        );
    }

    // ==============================
    // ON VIEW CREATED
    // ==============================
    @Override
    public void onViewCreated(
            @NonNull View v,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(v, savedInstanceState);

        bindViews(v);
        configurarMenuPrivado(toolbar);
        setupRecycler();

        uidUsuario = Preferencias.obtenerUid(requireContext());
        if (TextUtils.isEmpty(uidUsuario)) {
            Toast.makeText(requireContext(),
                    "Error: uid no encontrado",
                    Toast.LENGTH_LONG
            ).show();
            Navigation.findNavController(v)
                    .navigate(R.id.loginFragment);
            return;
        }

        vm = new ViewModelProvider(this)
                .get(GestionEventosUserPrivateViewModel.class);
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

    // ==============================
    // BINDS
    // ==============================
    private void bindViews(View v) {
        toolbar  = v.findViewById(R.id.toolbar);
        rvEventos = v.findViewById(R.id.rvEventos);
        tvEmpty   = v.findViewById(R.id.tvEmpty);
        progress  = v.findViewById(R.id.progress);
        btnVolver = v.findViewById(R.id.btnVolver);
    }

    // ==============================
    // RECYCLER
    // ==============================
    private void setupRecycler() {
        rvEventos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventosUserPrivateAdapter(listaEventos, this);
        rvEventos.setAdapter(adapter);
    }

    // ==============================
    // OBSERVERS
    // ==============================
    private void observeVm() {

        vm.getLoading().observe(getViewLifecycleOwner(), show ->
                progress.setVisibility(show != null && show ? View.VISIBLE : View.GONE)
        );

        vm.getEmpty().observe(getViewLifecycleOwner(), show ->
                tvEmpty.setVisibility(show != null && show ? View.VISIBLE : View.GONE)
        );

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

    // ==============================
    // CLICKS
    // ==============================
    private void setupClicks() {
        btnVolver.setOnClickListener(view -> {
            NavController nav = Navigation.findNavController(view);
            nav.popBackStack();
        });
    }

    // ==============================
    // ADAPTER ACTIONS
    // ==============================
    @Override public void onIncrementar(String idDoc) { vm.incrementarPlazas(idDoc); }
    @Override public void onDecrementar(String idDoc) { vm.decrementarPlazas(idDoc); }
    @Override public void onBorrar(Map<String, Object> evento) { pedirConfirmacionBorrar(evento); }
    @Override public void onEditar(Map<String, Object> evento) { mostrarDialogoEditar(evento); }

    @Override
    public void onVerInscritos(String idDoc, String tituloMostrado) {
        vm.abrirInscritosTiempoReal(idDoc, tituloMostrado);
    }

    @Override
    public CollectionReference getInscritosRef(String idDoc) {
        return vm.getInscritosRef(idDoc);
    }

    // ==============================
    // BORRADO
    // ==============================
    private void pedirConfirmacionBorrar(Map<String, Object> evento) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento privado?")
                .setPositiveButton("Borrar", (d, w) ->
                        vm.borrarEvento(String.valueOf(evento.get("idDoc")))
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==============================
    // EDITAR (AQUÍ ESTABA EL PROBLEMA)
    // ==============================
    private void mostrarDialogoEditar(Map<String, Object> evento) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_editar_deporte, null);

        TextInputEditText etNombre   = view.findViewById(R.id.etNombreDeporte);
        TextInputEditText etPlazas   = view.findViewById(R.id.etCantidadJugadores);
        TextInputEditText etFecha    = view.findViewById(R.id.etFecha);
        TextInputEditText etHora     = view.findViewById(R.id.etHora);
        TextInputEditText etDesc     = view.findViewById(R.id.etDescripcionEvento);
        TextInputEditText etReglas   = view.findViewById(R.id.etReglasEvento);
        TextInputEditText etMaterial = view.findViewById(R.id.etMateriales);

        etNombre.setText(String.valueOf(evento.get("nombre")));
        etPlazas.setText(String.valueOf(evento.get("plazasDisponibles")));
        etFecha.setText(String.valueOf(evento.get("fecha")));
        etHora.setText(String.valueOf(evento.get("hora")));
        etDesc.setText(str(evento.get("descripcion")));
        etReglas.setText(str(evento.get("reglas")));
        etMaterial.setText(str(evento.get("materiales")));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar evento privado")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dlg ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(btn -> {

                            String nombre = etNombre.getText().toString().trim();
                            String plazasTx = etPlazas.getText().toString().trim();

                            if (nombre.isEmpty()) {
                                etNombre.setError("Obligatorio");
                                return;
                            }

                            int plazas;
                            try {
                                plazas = Integer.parseInt(plazasTx);
                            } catch (Exception e) {
                                etPlazas.setError("Número inválido");
                                return;
                            }

                            Map<String, Object> nuevos = new HashMap<>();
                            nuevos.put("nombre", nombre);
                            nuevos.put("plazasDisponibles", plazas);
                            nuevos.put("fecha", etFecha.getText().toString().trim());
                            nuevos.put("hora", etHora.getText().toString().trim());
                            nuevos.put("descripcion", etDesc.getText().toString().trim());
                            nuevos.put("reglas", etReglas.getText().toString().trim());
                            nuevos.put("materiales", etMaterial.getText().toString().trim());
                            nuevos.put("uidOwner", uidUsuario);

                            String oldId = String.valueOf(evento.get("idDoc"));
                            String newId = GestionEventosUserPrivateViewModel
                                    .generarDocIdPrivado(
                                            nombre,
                                            String.valueOf(evento.get("fecha")),
                                            String.valueOf(evento.get("hora"))
                                    );

                            vm.editarEvento(oldId, newId, nuevos);
                            dialog.dismiss();
                        })
        );

        dialog.show();
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    // ==============================
    // INSCRITOS
    // ==============================
    private void abrirInscritosDialog(String idDoc, String titulo) {
        inscritosDialog = InscritosDialogFragment.newInstance(idDoc, titulo);
        inscritosDialog.show(getChildFragmentManager(), "inscritos_dialog");
    }

    @Override
    public void onDialogShown(String idDoc, String titulo) {}

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
