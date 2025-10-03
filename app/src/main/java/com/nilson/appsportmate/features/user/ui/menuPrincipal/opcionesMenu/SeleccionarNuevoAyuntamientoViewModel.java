package com.nilson.appsportmate.features.user.ui.menuPrincipal.opcionesMenu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarNuevoAyuntamientoViewModel extends ViewModel {

    private final MutableLiveData<SeleccionarNuevoAyuntamientoUiState> _ui =
            new MutableLiveData<>(SeleccionarNuevoAyuntamientoUiState.idle());
    public LiveData<SeleccionarNuevoAyuntamientoUiState> ui = _ui;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ids seleccionados (estado interno)
    private String comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId;

    public void cargarComunidades() {
        SeleccionarNuevoAyuntamientoUiState cur = _ui.getValue();
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(cur == null ? SeleccionarNuevoAyuntamientoUiState.idle() : cur));

        db.collection("comunidades").orderBy("nombre")
                .get()
                .addOnSuccessListener(snap -> {
                    List<SeleccionarNuevoAyuntamientoUiState.Opcion> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap) {
                        list.add(new SeleccionarNuevoAyuntamientoUiState.Opcion(d.getId(), safe(d.getString("nombre"))));
                    }
                    SeleccionarNuevoAyuntamientoUiState next = SeleccionarNuevoAyuntamientoUiState.withLists(
                            _ui.getValue(), list, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    next = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(next,
                            comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
                    _ui.setValue(next);
                })
                .addOnFailureListener(e ->
                        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "Error cargando comunidades"))
                );
    }

    public void onComunidadSelected(int index) {
        SeleccionarNuevoAyuntamientoUiState s = _ui.getValue();
        if (s == null || index < 0 || index >= s.comunidades.size()) return;

        comunidadId = s.comunidades.get(index).id;

        // limpiar hijos
        provinciaId = ciudadId = puebloId = ayuntamientoId = null;

        SeleccionarNuevoAyuntamientoUiState clearing = SeleccionarNuevoAyuntamientoUiState.withLists(
                s, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelection(clearing, "", "");
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(clearing,
                comunidadId, null, null, null, null);
        _ui.setValue(clearing);

        Query q = db.collection("provincias").whereEqualTo("comunidadId", comunidadId).orderBy("nombre");
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(_ui.getValue()));
        q.get().addOnSuccessListener(snap -> {
            List<SeleccionarNuevoAyuntamientoUiState.Opcion> list = new ArrayList<>();
            for (DocumentSnapshot d : snap) {
                list.add(new SeleccionarNuevoAyuntamientoUiState.Opcion(d.getId(), safe(d.getString("nombre"))));
            }
            SeleccionarNuevoAyuntamientoUiState next = SeleccionarNuevoAyuntamientoUiState.withLists(
                    _ui.getValue(), null, list, new ArrayList<>(), new ArrayList<>());
            next = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(next,
                    comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
            _ui.setValue(next);
        }).addOnFailureListener(e ->
                _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "Error cargando provincias"))
        );
    }

    public void onProvinciaSelected(int index) {
        SeleccionarNuevoAyuntamientoUiState s = _ui.getValue();
        if (s == null || index < 0 || index >= s.provincias.size()) return;

        provinciaId = s.provincias.get(index).id;

        ciudadId = puebloId = ayuntamientoId = null;

        SeleccionarNuevoAyuntamientoUiState clearing = SeleccionarNuevoAyuntamientoUiState.withLists(
                s, null, null, new ArrayList<>(), new ArrayList<>());
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelection(clearing, "", "");
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(clearing,
                comunidadId, provinciaId, null, null, null);
        _ui.setValue(clearing);

        Query q = db.collection("ciudades").whereEqualTo("provinciaId", provinciaId).orderBy("nombre");
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(_ui.getValue()));
        q.get().addOnSuccessListener(snap -> {
            List<SeleccionarNuevoAyuntamientoUiState.Opcion> list = new ArrayList<>();
            for (DocumentSnapshot d : snap) {
                list.add(new SeleccionarNuevoAyuntamientoUiState.Opcion(d.getId(), safe(d.getString("nombre"))));
            }
            SeleccionarNuevoAyuntamientoUiState next = SeleccionarNuevoAyuntamientoUiState.withLists(
                    _ui.getValue(), null, null, list, new ArrayList<>());
            next = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(next,
                    comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
            _ui.setValue(next);
        }).addOnFailureListener(e ->
                _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "Error cargando ciudades"))
        );
    }

    public void onCiudadSelected(int index) {
        SeleccionarNuevoAyuntamientoUiState s = _ui.getValue();
        if (s == null || index < 0 || index >= s.ciudades.size()) return;

        ciudadId = s.ciudades.get(index).id;

        puebloId = ayuntamientoId = null;

        SeleccionarNuevoAyuntamientoUiState clearing = SeleccionarNuevoAyuntamientoUiState.withLists(
                s, null, null, null, new ArrayList<>());
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelection(clearing, "", "");
        clearing = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(clearing,
                comunidadId, provinciaId, ciudadId, null, null);
        _ui.setValue(clearing);

        Query q = db.collection("pueblos").whereEqualTo("ciudadId", ciudadId).orderBy("nombre");
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(_ui.getValue()));
        q.get().addOnSuccessListener(snap -> {
            List<SeleccionarNuevoAyuntamientoUiState.PuebloOpcion> list = new ArrayList<>();
            for (DocumentSnapshot d : snap) {
                String nombre = safe(d.getString("nombre"));
                String aytoId = safeN(d.getString("ayuntamientoId"));
                if (aytoId == null) aytoId = safeN(d.getString("createdByUid"));
                String aytoNombre = safeN(d.getString("ayuntamientoNombre"));
                list.add(new SeleccionarNuevoAyuntamientoUiState.PuebloOpcion(d.getId(), nombre, aytoId, aytoNombre));
            }
            SeleccionarNuevoAyuntamientoUiState next = SeleccionarNuevoAyuntamientoUiState.withLists(
                    _ui.getValue(), null, null, null, list);
            next = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(next,
                    comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
            _ui.setValue(next);
        }).addOnFailureListener(e ->
                _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "Error cargando pueblos"))
        );
    }

    public void onPuebloSelected(int index) {
        SeleccionarNuevoAyuntamientoUiState s = _ui.getValue();
        if (s == null || index < 0 || index >= s.pueblos.size()) return;

        SeleccionarNuevoAyuntamientoUiState.PuebloOpcion p = s.pueblos.get(index);
        puebloId = p.id;

        // Mostrar nombre del pueblo inmediatamente
        SeleccionarNuevoAyuntamientoUiState base = SeleccionarNuevoAyuntamientoUiState.withSelection(
                _ui.getValue(), p.nombre, "");
        base = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(base,
                comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
        _ui.setValue(base);

        // Resolver ayuntamiento
        if (p.ayuntamientoNombre != null && !p.ayuntamientoNombre.isEmpty()) {
            ayuntamientoId = p.ayuntamientoId;
            SeleccionarNuevoAyuntamientoUiState done = SeleccionarNuevoAyuntamientoUiState.withSelection(
                    _ui.getValue(), p.nombre, p.ayuntamientoNombre);
            done = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(done,
                    comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
            _ui.setValue(done);
            return;
        }

        if (p.ayuntamientoId == null || p.ayuntamientoId.isEmpty()) {
            ayuntamientoId = null;
            SeleccionarNuevoAyuntamientoUiState none = SeleccionarNuevoAyuntamientoUiState.withSelection(
                    _ui.getValue(), p.nombre, "");
            none = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(none,
                    comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
            _ui.setValue(none);
            return;
        }

        ayuntamientoId = p.ayuntamientoId;
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(_ui.getValue()));
        db.collection("ayuntamientos").document(p.ayuntamientoId).get()
                .addOnSuccessListener(doc -> {
                    String nombre = doc.getString("nombre");            // 1º nombre
                    if (nombre == null || nombre.isEmpty())
                        nombre = doc.getString("razonSocial");          // 2º razonSocial
                    if (nombre == null || nombre.isEmpty())
                        nombre = doc.getString("alias");                // 3º alias (fallback)
                    if (nombre == null) nombre = "";

                    SeleccionarNuevoAyuntamientoUiState done = SeleccionarNuevoAyuntamientoUiState.withSelection(
                            _ui.getValue(), p.nombre, nombre);
                    done = SeleccionarNuevoAyuntamientoUiState.withSelectedIds(done,
                            comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId);
                    _ui.setValue(done);
                })
                .addOnFailureListener(e ->
                        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "Error leyendo ayuntamiento"))
                );
    }

    /** Guardar selección en Firestore (usuarios/{uid}) */
    public void guardarSeleccion() {
        String uid = FirebaseAuth.getInstance().getUid();
        SeleccionarNuevoAyuntamientoUiState s = _ui.getValue();
        if (uid == null || s == null || puebloId == null || ayuntamientoId == null) {
            _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(
                    s != null ? s : SeleccionarNuevoAyuntamientoUiState.idle(), "Selecciona pueblo válido"));
            return;
        }
        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.loading(s));

        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .update("puebloId", puebloId,
                        "puebloNombre", s.puebloNombre,
                        "ayuntamientoId", ayuntamientoId,
                        "ayuntamientoNombre", s.ayuntamientoNombre)
                .addOnSuccessListener(unused -> {
                    SeleccionarNuevoAyuntamientoUiState ok = new SeleccionarNuevoAyuntamientoUiState(
                            false, "Guardado", null,
                            s.comunidades, s.provincias, s.ciudades, s.pueblos,
                            s.puebloNombre, s.ayuntamientoNombre,
                            comunidadId, provinciaId, ciudadId, puebloId, ayuntamientoId
                    );
                    _ui.setValue(ok);
                })
                .addOnFailureListener(e ->
                        _ui.setValue(SeleccionarNuevoAyuntamientoUiState.error(_ui.getValue(), "No se pudo guardar"))
                );
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String safeN(String s) { return (s == null || "null".equalsIgnoreCase(s)) ? null : s; }
}
