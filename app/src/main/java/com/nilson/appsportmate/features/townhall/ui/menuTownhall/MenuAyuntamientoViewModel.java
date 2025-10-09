package com.nilson.appsportmate.features.townhall.ui.menuTownhall;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuAyuntamientoViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String ayuntamientoId;

    private final MutableLiveData<String> ayuntamientoNombre = new MutableLiveData<>("—");
    private final MutableLiveData<List<Map<String, Object>>> eventos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> mensaje = new MutableLiveData<>("");

    public LiveData<String> getAyuntamientoNombre() { return ayuntamientoNombre; }
    public LiveData<List<Map<String, Object>>> getEventos() { return eventos; }
    public LiveData<String> getMensaje() { return mensaje; }

    /* ===== Carga ===== */

    public void cargarDatosAyuntamiento(@NonNull Context ctx) {
        ayuntamientoId = Preferencias.obtenerAyuntamientoId(ctx);
        if (TextUtils.isEmpty(ayuntamientoId)) {
            ayuntamientoNombre.postValue("Sin ayuntamiento asignado");
            return;
        }

        String cache = Preferencias.obtenerAyuntamientoNombre(ctx);
        if (!TextUtils.isEmpty(cache)) ayuntamientoNombre.postValue(cache);

        db.collection("ayuntamientos")
                .document(ayuntamientoId)
                .get(Source.DEFAULT)
                .addOnSuccessListener(doc -> {
                    String nombre = extraerNombre(doc);
                    ayuntamientoNombre.postValue(nombre);
                    Preferencias.guardarAyuntamientoNombre(ctx, nombre);
                })
                .addOnFailureListener(e -> mensaje.postValue("Error cargando ayuntamiento"));
    }

    public void cargarEventos(@NonNull Context ctx) {
        if (TextUtils.isEmpty(ayuntamientoId))
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(ctx);
        if (TextUtils.isEmpty(ayuntamientoId)) {
            mensaje.postValue("No se encontró ayuntamientoId");
            return;
        }

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .get(Source.DEFAULT)
                .addOnSuccessListener(q -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
                        m = new HashMap<>(m);
                        m.put("idDoc", d.getId());
                        list.add(m);
                    }
                    eventos.postValue(list);
                })
                .addOnFailureListener(e ->
                        mensaje.postValue("Error cargando eventos: " + e.getMessage()));
    }

    /* ===== Accesos ===== */

    public CollectionReference getInscritosRef(@NonNull String idDoc) {
        if (TextUtils.isEmpty(ayuntamientoId)) return null;
        return db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(idDoc)
                .collection("inscritos");
    }

    /* ===== Helpers ===== */

    private String extraerNombre(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return "(desconocido)";
        Object n1 = doc.get("nombre");
        Object n2 = doc.get("razonSocial");
        String nom = n1 != null ? String.valueOf(n1) : null;
        if (TextUtils.isEmpty(nom) || "null".equalsIgnoreCase(nom)) {
            nom = n2 != null ? String.valueOf(n2) : "(desconocido)";
        }
        return nom;
    }
}
