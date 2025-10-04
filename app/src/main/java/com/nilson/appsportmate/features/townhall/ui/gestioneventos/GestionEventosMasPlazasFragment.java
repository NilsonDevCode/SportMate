package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import com.google.android.material.textfield.TextInputEditText;


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

/**
 * Lista/gestión de eventos (MVVM).
 * Mantiene IDs/keys y layouts existentes.
 */
public class GestionEventosMasPlazasFragment extends Fragment
        implements EventosAdapter.EventoActions, InscritosDialogFragment.Host {

    // UI
    private RecyclerView rvEventos;
    private TextView tvEmpty;
    private ProgressBar progress;
    private MaterialButton btnVolver;

    // Estado
    private String ayuntamientoId;
    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosAdapter adapter;

    // VM
    private GestionEventosMasPlazasViewModel vm;

    // Diálogo inscritos
    private InscritosDialogFragment inscritosDialog;

    public GestionEventosMasPlazasFragment() { }

    // ---------------------------
    // Lifecycle
    // ---------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Se usa tu layout existente tal cual
        return inflater.inflate(R.layout.activity_gestion_eventos_mas_plazas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        bindViews(v);
        setupRecycler();

        // ayuntamientoId: arg o Preferencias
        Bundle args = getArguments();
        String extra = args != null ? args.getString("ayuntamientoId", "") : "";
        ayuntamientoId = !TextUtils.isEmpty(extra) ? extra : Preferencias.obtenerAyuntamientoId(requireContext());

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

    // ---------------------------
    // Bind / Observers / Clicks
    // ---------------------------

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

        // Señal para abrir diálogo de inscritos
        vm.getOpenInscritosEvent().observe(getViewLifecycleOwner(), pair -> {
            if (pair == null) return;
            abrirInscritosDialog(pair.first, pair.second);
        });

        // Datos en tiempo real de inscritos (aliases + uids a la vez)
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

    // ---------------------------
    // Adapter actions
    // ---------------------------

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

    // ---------------------------
    // Diálogos y helpers UI
    // ---------------------------

    private void pedirConfirmacionBorrar(Map<String, Object> evento) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento? Se perderán inscripciones.")
                .setPositiveButton("Borrar", (d, w) -> {
                    String idDoc = String.valueOf(evento.get("idDoc"));
                    vm.borrarEvento(idDoc);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(Map<String, Object> evento) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_deporte, null);

        TextInputEditText etNombre    = view.findViewById(R.id.etNombreDeporte);
        TextInputEditText etPlazas    = view.findViewById(R.id.etCantidadJugadores);
        TextInputEditText etFecha     = view.findViewById(R.id.etFecha);
        TextInputEditText etHora      = view.findViewById(R.id.etHora);
        TextInputEditText etDesc      = view.findViewById(R.id.etDescripcionEvento);
        TextInputEditText etReglas    = view.findViewById(R.id.etReglasEvento);
        TextInputEditText etMaterial  = view.findViewById(R.id.etMateriales);
        TextInputEditText etUrl       = view.findViewById(R.id.etUrlPueblo);

        // Precarga segura (evita "null" en UI)
        etNombre.setText(String.valueOf(evento.get("nombre")));
        etPlazas.setText(String.valueOf(evento.get("plazasDisponibles")));
        etFecha.setText(String.valueOf(evento.get("fecha")));
        etHora.setText(String.valueOf(evento.get("hora")));
        // Pickers al tocar los campos
        etFecha.setOnClickListener(v -> mostrarDatePickerPara(etFecha));
        etHora.setOnClickListener(v -> mostrarTimePickerPara(etHora));
        etDesc.setText(evento.get("descripcion") == null ? "" : String.valueOf(evento.get("descripcion")));
        etReglas.setText(evento.get("reglas") == null ? "" : String.valueOf(evento.get("reglas")));
        etMaterial.setText(evento.get("materiales") == null ? "" : String.valueOf(evento.get("materiales")));
        etUrl.setText(evento.get("urlPueblo") == null ? "" : String.valueOf(evento.get("urlPueblo")));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar evento")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre    = String.valueOf(etNombre.getText()).trim();
            String plazasTx  = String.valueOf(etPlazas.getText()).trim();
            String fecha     = String.valueOf(etFecha.getText()).trim();
            String hora      = String.valueOf(etHora.getText()).trim();
            String desc      = String.valueOf(etDesc.getText()).trim();
            String reglas    = String.valueOf(etReglas.getText()).trim();
            String material  = String.valueOf(etMaterial.getText()).trim();
            String url       = String.valueOf(etUrl.getText()).trim();

            if (nombre.isEmpty())  { etNombre.setError("Obligatorio"); return; }
            if (desc.isEmpty())    { etDesc.setError("Obligatorio"); return; }
            if (reglas.isEmpty())  { etReglas.setError("Obligatorio"); return; }
            if (material.isEmpty()){ etMaterial.setError("Obligatorio"); return; }
            if (url.isEmpty())     { etUrl.setError("Obligatorio"); return; }

            String errPlazas = com.nilson.appsportmate.common.utils.ValidacionesEvento.validarPlazas(plazasTx, 1, 200);
            if (errPlazas != null) { etPlazas.setError(errPlazas); return; }

            String errFechaHora = com.nilson.appsportmate.common.utils.ValidacionesEvento.validarFechaHoraFuturas(fecha, hora);
            if (errFechaHora != null) { etFecha.setError(errFechaHora); return; }

            int plazas;
            try { plazas = Integer.parseInt(plazasTx); }
            catch (Exception e) { etPlazas.setError("Número inválido"); return; }

            Map<String, Object> nuevos = new HashMap<>();
            nuevos.put("nombre", nombre);
            nuevos.put("plazasDisponibles", plazas);
            nuevos.put("fecha", fecha);
            nuevos.put("hora", hora);
            nuevos.put("descripcion", desc);
            nuevos.put("reglas", reglas);
            nuevos.put("materiales", material);
            nuevos.put("urlPueblo", url);
            nuevos.put("ayuntamientoId", ayuntamientoId);

            String oldId = String.valueOf(evento.get("idDoc"));
            String newId = GestionEventosMasPlazasViewModel.generarDocId(nombre, fecha, hora);

            vm.guardarEdicion(oldId, newId, nuevos);
            dialog.dismiss();
        }));

        dialog.show();
    }
    private void mostrarDatePickerPara(TextInputEditText target) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        // Si ya hay fecha, intenta precargarla (formato dd/MM/yyyy)
        String f = target.getText() != null ? target.getText().toString().trim() : "";
        if (!f.isEmpty()) {
            try {
                String[] p = f.split("/");
                int day = Integer.parseInt(p[0]);
                int mon = Integer.parseInt(p[1]) - 1;
                int yr  = Integer.parseInt(p[2]);
                c.set(yr, mon, day);
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) ->
                        target.setText(String.format(java.util.Locale.getDefault(),
                                "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void mostrarTimePickerPara(TextInputEditText target) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        // Si ya hay hora, intenta precargarla (formato HH:mm)
        String h = target.getText() != null ? target.getText().toString().trim() : "";
        if (!h.isEmpty()) {
            try {
                String[] p = h.split(":");
                int hr = Integer.parseInt(p[0]);
                int mi = Integer.parseInt(p[1]);
                c.set(java.util.Calendar.HOUR_OF_DAY, hr);
                c.set(java.util.Calendar.MINUTE, mi);
            } catch (Exception ignored) {}
        }
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) ->
                        target.setText(String.format(java.util.Locale.getDefault(),
                                "%02d:%02d", hourOfDay, minute)),
                c.get(java.util.Calendar.HOUR_OF_DAY),
                c.get(java.util.Calendar.MINUTE),
                true
        ).show();
    }



    // ---------------------------
    // Helpers diálogo inscritos (sin @Override)
    // ---------------------------

    private void abrirInscritosDialog(String idDoc, String titulo) {
        inscritosDialog = InscritosDialogFragment.newInstance(idDoc, titulo);
        inscritosDialog.show(getParentFragmentManager(), "inscritos_dialog");
    }

    private void cerrarInscritosDialog() {
        if (inscritosDialog != null) {
            inscritosDialog.dismissAllowingStateLoss();
            inscritosDialog = null;
        }
    }

    // ---------------------------
    // Host callbacks del diálogo
    // ---------------------------

    @Override public void onDialogShown(String idDoc, String titulo) { /* VM ya escuchando */ }

    @Override
    public void onDialogDismissRequested() {
        vm.cerrarInscritosTiempoReal(); // detiene listener
        cerrarInscritosDialog();        // cierra UI por si acaso
    }

    @Override
    public void onExpulsarClicked(String idDoc, String uid) {
        vm.expulsarInscrito(idDoc, uid);
    }
}
