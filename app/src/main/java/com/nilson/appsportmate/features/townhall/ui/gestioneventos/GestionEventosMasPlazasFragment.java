package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;
import com.nilson.appsportmate.common.utils.ValidacionesEvento;
import com.nilson.appsportmate.features.townhall.adaptadores.EventosAdapter;
import com.nilson.appsportmate.features.townhall.ui.dialogos.InscritosDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionEventosMasPlazasFragment extends Fragment
        implements EventosAdapter.EventoActions, InscritosDialogFragment.Host {

    private RecyclerView rvEventos;
    private TextView tvEmpty;
    private ProgressBar progress;
    private MaterialButton btnVolver;

    private String ayuntamientoId;
    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosAdapter adapter;

    private GestionEventosMasPlazasViewModel vm;
    private InscritosDialogFragment inscritosDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gestion_eventos_mas_plazas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        bindViews(v);
        setupRecycler();

        Bundle args = getArguments();
        String extra = args != null ? args.getString("ayuntamientoId", "") : "";
        ayuntamientoId = !TextUtils.isEmpty(extra)
                ? extra
                : Preferencias.obtenerAyuntamientoId(requireContext());

        if (TextUtils.isEmpty(ayuntamientoId)) {
            Toast.makeText(requireContext(), "ayuntamientoId no encontrado", Toast.LENGTH_LONG).show();
            Navigation.findNavController(v).popBackStack();
            return;
        }

        vm = new ViewModelProvider(this).get(GestionEventosMasPlazasViewModel.class);
        vm.setAyuntamientoId(ayuntamientoId);

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
        adapter = new EventosAdapter(listaEventos, this);
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

    private void pedirConfirmacionBorrar(Map<String, Object> evento) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento?")
                .setPositiveButton("Borrar", (d, w) ->
                        vm.borrarEvento(String.valueOf(evento.get("idDoc"))))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==============================
    // EDITAR (ÚNICA MEJORA AQUÍ)
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
        TextInputEditText etUrl      = view.findViewById(R.id.etUrlPueblo);

        String fechaOriginal = String.valueOf(evento.get("fecha"));
        String horaOriginal  = String.valueOf(evento.get("hora"));

        etNombre.setText(String.valueOf(evento.get("nombre")));
        etPlazas.setText(String.valueOf(evento.get("plazasDisponibles")));
        etFecha.setText(fechaOriginal);
        etHora.setText(horaOriginal);
        etDesc.setText(String.valueOf(evento.get("descripcion")));
        etReglas.setText(String.valueOf(evento.get("reglas")));
        etMaterial.setText(String.valueOf(evento.get("materiales")));
        etUrl.setText(String.valueOf(evento.get("urlPueblo")));

        etFecha.setOnClickListener(v -> mostrarDatePickerPara(etFecha));
        etHora.setOnClickListener(v -> mostrarTimePickerPara(etHora));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar evento")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dlg ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String nombre = etNombre.getText().toString().trim();
                            String plazasTx = etPlazas.getText().toString().trim();
                            String fecha = etFecha.getText().toString().trim();
                            String hora  = etHora.getText().toString().trim();

                            if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }

                            int plazas;
                            try { plazas = Integer.parseInt(plazasTx); }
                            catch (Exception e) { etPlazas.setError("Número inválido"); return; }

                            boolean fechaCambiada =
                                    !fecha.equals(fechaOriginal) || !hora.equals(horaOriginal);

                            if (fechaCambiada) {
                                String err = ValidacionesEvento.validarFechaHoraFuturas(fecha, hora);
                                if (err != null) {
                                    etFecha.setError(err);
                                    return;
                                }
                            }

                            Map<String, Object> nuevos = new HashMap<>();
                            nuevos.put("nombre", nombre);
                            nuevos.put("plazasDisponibles", plazas);
                            nuevos.put("fecha", fecha);
                            nuevos.put("hora", hora);
                            nuevos.put("descripcion", etDesc.getText().toString().trim());
                            nuevos.put("reglas", etReglas.getText().toString().trim());
                            nuevos.put("materiales", etMaterial.getText().toString().trim());
                            nuevos.put("urlPueblo", etUrl.getText().toString().trim());
                            nuevos.put("ayuntamientoId", ayuntamientoId);

                            String oldId = String.valueOf(evento.get("idDoc"));
                            String newId = GestionEventosMasPlazasViewModel
                                    .generarDocId(nombre, fecha, hora);

                            vm.guardarEdicion(oldId, newId, nuevos);
                            dialog.dismiss();
                        })
        );

        dialog.show();
    }

    private void mostrarDatePickerPara(TextInputEditText target) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (v, y, m, d) ->
                        target.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void mostrarTimePickerPara(TextInputEditText target) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (v, h, m) ->
                        target.setText(String.format("%02d:%02d", h, m)),
                c.get(java.util.Calendar.HOUR_OF_DAY),
                c.get(java.util.Calendar.MINUTE),
                true
        ).show();
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
