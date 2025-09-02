package com.nilson.appsportmate.caracteristicas.ayuntamiento.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.adaptadores.EventosAdapter;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.presentacion.GestionEventosMasPlazasPresenter;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.presentacion.GestionEventosMasPlazasView;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.ui.dialogos.InscritosDialogFragment;
import com.nilson.appsportmate.comun.datos.firebase.FirestoreTransacciones;
import com.nilson.appsportmate.comun.utilidades.Preferencias;
import com.nilson.appsportmate.comun.utilidades.ValidacionesEvento;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionEventosMasPlazasActivity extends AppCompatActivity
        implements EventosAdapter.EventoActions, GestionEventosMasPlazasView,
        InscritosDialogFragment.Host {

    private RecyclerView rvEventos;
    private TextView tvEmpty;
    private ProgressBar progress;
    private MaterialButton btnVolver;

    private String ayuntamientoId;

    private final List<Map<String, Object>> listaEventos = new ArrayList<>();
    private EventosAdapter adapter;

    private GestionEventosMasPlazasPresenter presenter;

    // Diálogo de inscritos en vivo
    private InscritosDialogFragment inscritosDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_eventos_mas_plazas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestión de Eventos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvEventos = findViewById(R.id.rvEventos);
        tvEmpty   = findViewById(R.id.tvEmpty);
        progress  = findViewById(R.id.progress);
        btnVolver = findViewById(R.id.btnVolver);

        rvEventos.setLayoutManager(new LinearLayoutManager(this));

        String extra = getIntent().getStringExtra("ayuntamientoId");
        ayuntamientoId = (extra != null && !extra.isEmpty())
                ? extra
                : Preferencias.obtenerAyuntamientoId(this);

        if (TextUtils.isEmpty(ayuntamientoId)) {
            Toast.makeText(this, "ayuntamientoId no encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Presenter
        FirestoreTransacciones fx = new FirestoreTransacciones(
                com.google.firebase.firestore.FirebaseFirestore.getInstance(), ayuntamientoId);
        presenter = new GestionEventosMasPlazasPresenter(this, fx, ayuntamientoId);

        adapter = new EventosAdapter(listaEventos, this);
        rvEventos.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override protected void onStart() {
        super.onStart();
        presenter.suscribirTiempoRealEventos();
    }

    @Override protected void onStop() {
        presenter.desuscribirTiempoRealEventos();
        // por si el diálogo quedó abierto al pausar
        presenter.dejarDeEscucharInscritos();
        super.onStop();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    /* ====== EventosAdapter.EventoActions ====== */

    @Override public void onIncrementar(String idDoc) { presenter.incrementarPlazas(idDoc); }
    @Override public void onDecrementar(String idDoc) { presenter.decrementarPlazas(idDoc); }
    @Override public void onEditar(Map<String, Object> evento) { presenter.solicitarEdicion(evento); }
    @Override public void onBorrar(Map<String, Object> evento) { presenter.solicitarBorrado(evento); }

    @Override
    public void onVerInscritos(String idDoc, String tituloMostrado) {
        // Abrir diálogo en vivo
        presenter.abrirInscritosTiempoReal(idDoc, tituloMostrado);
    }

    @Override public CollectionReference getInscritosRef(String idDoc) {
        return presenter.getInscritosRef(idDoc);
    }

    /* ====== GestionEventosMasPlazasView ====== */

    @Override public void mostrarLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void mostrarEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void mostrarMensaje(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void mostrarEventos(List<Map<String, Object>> eventos) {
        listaEventos.clear();
        if (eventos != null) listaEventos.addAll(eventos);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void pedirConfirmacionBorrar(Map<String, Object> evento) {
        new AlertDialog.Builder(this)
                .setTitle("Borrar evento")
                .setMessage("¿Seguro que quieres borrar este evento? Se perderán inscripciones.")
                .setPositiveButton("Borrar", (d, w) -> {
                    String idDoc = String.valueOf(evento.get("idDoc"));
                    presenter.borrarEvento(idDoc);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void mostrarDialogoEditar(Map<String, Object> evento) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_deporte, null);

        TextInputEditText etNombre    = view.findViewById(R.id.etNombreDeporte);
        TextInputEditText etPlazas    = view.findViewById(R.id.etCantidadJugadores);
        TextInputEditText etFecha     = view.findViewById(R.id.etFecha);
        TextInputEditText etHora      = view.findViewById(R.id.etHora);
        TextInputEditText etComunidad = view.findViewById(R.id.etDescripcionEvento);
        TextInputEditText etProvincia = view.findViewById(R.id.etReglasEvento);
        TextInputEditText etCiudad    = view.findViewById(R.id.etMateriales);
        TextInputEditText etPueblo    = view.findViewById(R.id.etUrlPueblo);

        // Precarga
        etNombre.setText(String.valueOf(evento.get("nombre")));
        etPlazas.setText(String.valueOf(evento.get("plazasDisponibles")));
        etFecha.setText(String.valueOf(evento.get("fecha")));
        etHora.setText(String.valueOf(evento.get("hora")));
        etComunidad.setText(String.valueOf(evento.get("comunidad")));
        etProvincia.setText(String.valueOf(evento.get("provincia")));
        etCiudad.setText(String.valueOf(evento.get("ciudad")));
        etPueblo.setText(String.valueOf(evento.get("pueblo")));

        AlertDialog dialog = new AlertDialog.Builder(this)
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
            String comunidad = String.valueOf(etComunidad.getText()).trim();
            String provincia = String.valueOf(etProvincia.getText()).trim();
            String ciudad    = String.valueOf(etCiudad.getText()).trim();
            String pueblo    = String.valueOf(etPueblo.getText()).trim();

            if (nombre.isEmpty())    { etNombre.setError("Obligatorio"); return; }
            if (comunidad.isEmpty()) { etComunidad.setError("Obligatorio"); return; }
            if (provincia.isEmpty()) { etProvincia.setError("Obligatorio"); return; }
            if (ciudad.isEmpty())    { etCiudad.setError("Obligatorio"); return; }
            if (pueblo.isEmpty())    { etPueblo.setError("Obligatorio"); return; }

            String errPlazas = ValidacionesEvento.validarPlazas(plazasTx, 1, 200);
            if (errPlazas != null) { etPlazas.setError(errPlazas); return; }

            String errFechaHora = ValidacionesEvento.validarFechaHoraFuturas(fecha, hora);
            if (errFechaHora != null) { etFecha.setError(errFechaHora); return; }

            int plazas;
            try { plazas = Integer.parseInt(plazasTx); }
            catch (Exception e) { etPlazas.setError("Número inválido"); return; }

            Map<String, Object> nuevos = new HashMap<>();
            nuevos.put("nombre", nombre);
            nuevos.put("plazasDisponibles", plazas);
            nuevos.put("fecha", fecha);
            nuevos.put("hora", hora);
            nuevos.put("comunidad", comunidad);
            nuevos.put("provincia", provincia);
            nuevos.put("ciudad", ciudad);
            nuevos.put("pueblo", pueblo);
            nuevos.put("ayuntamientoId", ayuntamientoId);

            String oldId = String.valueOf(evento.get("idDoc"));
            String newId = GestionEventosMasPlazasPresenter.generarDocId(nombre, fecha, hora);

            presenter.guardarEdicion(oldId, newId, nuevos);
            dialog.dismiss();
        }));

        dialog.show();
    }

    /* ====== Inscritos tiempo real (via DialogFragment) ====== */

    @Override
    public void abrirInscritosTiempoReal(String idDoc, String titulo) {
        FragmentManager fm = getSupportFragmentManager();
        inscritosDialog = InscritosDialogFragment.newInstance(idDoc, titulo);
        inscritosDialog.show(fm, "inscritos_dialog");
    }

    @Override
    public void actualizarInscritosTiempoReal(List<String> aliases, List<String> uids) {
        if (inscritosDialog != null) {
            inscritosDialog.updateData(aliases, uids);
        }
    }

    @Override
    public void cerrarInscritosTiempoReal() {
        if (inscritosDialog != null) {
            inscritosDialog.dismissAllowingStateLoss();
            inscritosDialog = null;
        }
    }

    /* ====== Host del Dialog para llamar al presenter ====== */
    @Override
    public void onDialogShown(String idDoc, String titulo) {
        // cuando el dialog se muestra, ya hay un listener activo desde presenter. No hacer nada extra.
    }

    @Override
    public void onDialogDismissRequested() {
        presenter.cerrarInscritosTiempoReal();
    }

    @Override
    public void onExpulsarClicked(String idDoc, String uid) {
        presenter.expulsarInscrito(idDoc, uid);
    }
}
