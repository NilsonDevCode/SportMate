package com.nilson.appsportmate.features.townhall.ui.menuTownhall;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuAyuntamientoViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private String ayuntamientoId;

    private final MutableLiveData<String> ayuntamientoNombre = new MutableLiveData<>("—");
    private final MutableLiveData<String> logoUrl = new MutableLiveData<>(null);
    private final MutableLiveData<List<Map<String, Object>>> eventos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> mensaje = new MutableLiveData<>("");

    public LiveData<String> getAyuntamientoNombre() { return ayuntamientoNombre; }
    public LiveData<String> getLogoUrl() { return logoUrl; }
    public LiveData<List<Map<String, Object>>> getEventos() { return eventos; }
    public LiveData<String> getMensaje() { return mensaje; }

    /* ===== CARGA ===== */

    public void cargarDatosAyuntamiento(@NonNull Context ctx) {
        ayuntamientoId = Preferencias.obtenerAyuntamientoId(ctx);
        Log.d("AYTO_DEBUG", "cargarDatosAyuntamiento() → ID: " + ayuntamientoId);

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
                    Log.d("AYTO_DEBUG", "Documento Firestore obtenido correctamente.");
                    String nombre = extraerNombre(doc);
                    ayuntamientoNombre.postValue(nombre);
                    Preferencias.guardarAyuntamientoNombre(ctx, nombre);

                    if (doc.contains("logoUrl")) {
                        String url = doc.getString("logoUrl");
                        Log.d("AYTO_DEBUG", "LogoUrl encontrado: " + url);
                        logoUrl.postValue(url);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AYTO_DEBUG", "Error cargando ayuntamiento: " + e.getMessage());
                    mensaje.postValue("Error cargando ayuntamiento");
                });
    }

    public void cargarEventos(@NonNull Context ctx) {
        if (TextUtils.isEmpty(ayuntamientoId))
            ayuntamientoId = Preferencias.obtenerAyuntamientoId(ctx);
        if (TextUtils.isEmpty(ayuntamientoId)) {
            mensaje.postValue("No se encontró ayuntamientoId");
            return;
        }

        Log.d("AYTO_DEBUG", "Cargando eventos para ayuntamientoId=" + ayuntamientoId);

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
                    Log.d("AYTO_DEBUG", "Eventos cargados: " + list.size());
                    eventos.postValue(list);
                })
                .addOnFailureListener(e ->
                        mensaje.postValue("Error cargando eventos: " + e.getMessage()));
    }

    /* ===== GUARDAR LOGO ===== */

    public void subirLogoAyuntamiento(@NonNull Uri uri, @NonNull Context ctx) {
        ayuntamientoId = Preferencias.obtenerAyuntamientoId(ctx);
        Log.d("AYTO_DEBUG", "Intentando subir logo para ID=" + ayuntamientoId + " URI=" + uri);

        if (TextUtils.isEmpty(ayuntamientoId)) {
            mensaje.postValue("Error: ayuntamientoId no encontrado.");
            Log.e("AYTO_DEBUG", "Error: ayuntamientoId nulo o vacío");
            return;
        }

        StorageReference ref = storage.getReference()
                .child("logos_ayuntamientos/" + ayuntamientoId + ".jpg");

        Log.d("AYTO_DEBUG", "Subiendo archivo a ruta Storage: logos_ayuntamientos/" + ayuntamientoId + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("AYTO_DEBUG", "Subida exitosa, obteniendo URL...");
                    ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String url = downloadUri.toString();
                                Log.d("AYTO_DEBUG", "URL obtenida: " + url);

                                db.collection("ayuntamientos")
                                        .document(ayuntamientoId)
                                        .update("logoUrl", url)
                                        .addOnSuccessListener(v -> {
                                            logoUrl.postValue(url);
                                            mensaje.postValue("Logo actualizado correctamente");
                                            Log.d("AYTO_DEBUG", "URL guardada en Firestore correctamente.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("AYTO_DEBUG", "Error guardando URL en Firestore: " + e.getMessage());
                                            mensaje.postValue("Error guardando URL en Firestore");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AYTO_DEBUG", "Error obteniendo URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AYTO_DEBUG", "Error subiendo imagen: " + e.getMessage());
                    mensaje.postValue("Error subiendo imagen: " + e.getMessage());
                });
    }

    /* ===== ACCESOS ===== */

    public CollectionReference getInscritosRef(@NonNull String idDoc) {
        if (TextUtils.isEmpty(ayuntamientoId)) return null;
        return db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(idDoc)
                .collection("inscritos");
    }

    /* ===== HELPERS ===== */

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
