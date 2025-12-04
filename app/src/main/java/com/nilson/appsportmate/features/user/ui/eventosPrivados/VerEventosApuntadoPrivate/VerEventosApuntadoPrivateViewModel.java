package com.nilson.appsportmate.features.user.ui.eventosPrivados.VerEventosApuntadoPrivate;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VerEventosApuntadoPrivateViewModel extends ViewModel {

    private static final String TAG = "EventosApuntadosVM";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<VerEventosApuntadoPrivateUiState> _uiState =
            new MutableLiveData<>(VerEventosApuntadoPrivateUiState.loading());
    public LiveData<VerEventosApuntadoPrivateUiState> uiState = _uiState;

    private final String uid;

    public VerEventosApuntadoPrivateViewModel() {
        uid = FirebaseAuth.getInstance().getUid();
    }

    // ============================================================
    // CLASE PARA EL ADAPTER
    // ============================================================
    public static class EventoUi {
        public String docId;
        public String ownerId;
        public String nombre;
        public String descripcion;
        public String fecha;
        public String hora;
        public String lugar;
        public int plazas;
        public int inscritos;

        public EventoUi() {}

        public EventoUi(String docId, String ownerId,
                        String nombre, String descripcion,
                        String fecha, String hora, String lugar,
                        int plazas, int inscritos) {

            this.docId = docId;
            this.ownerId = ownerId;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.hora = hora;
            this.lugar = lugar;
            this.plazas = plazas;
            this.inscritos = inscritos;
        }
    }

    public void subirFotoPerfilUsuario(@NonNull Uri uri, @NonNull Runnable onSuccess, @NonNull java.util.function.Consumer<String> onError) {
        if (uid == null) {
            onError.accept("Usuario no autenticado.");
            return;
        }

        FirebaseStorage.getInstance().getReference("logos_usuarios/" + uid + ".jpg")
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    db.collection("usuarios").document(uid)
                                            .update("fotoUrl", downloadUri.toString())
                                            .addOnSuccessListener(aVoid -> onSuccess.run())
                                            .addOnFailureListener(e -> onError.accept("Error guardando URL: " + e.getMessage()));
                                })
                                .addOnFailureListener(e -> onError.accept("Error obteniendo URL: " + e.getMessage()))
                )
                .addOnFailureListener(e -> onError.accept("Error subiendo imagen: " + e.getMessage()));
    }

    // ============================================================
    // CARGAR EVENTOS DONDE EL USUARIO ESTÁ APUNTADO
    // ============================================================
    public void loadEventosApuntados() {

        if (uid == null) {
            _uiState.setValue(VerEventosApuntadoPrivateUiState.error("Debes iniciar sesión"));
            return;
        }

        _uiState.setValue(VerEventosApuntadoPrivateUiState.loading());

        new Thread(() -> {
            try {

                List<DocumentSnapshot> snaps = Tasks.await(
                        db.collection("usuarios")
                                .document(uid)
                                .collection("inscripciones_privadas")
                                .get(Source.SERVER)
                ).getDocuments();

                List<EventoUi> lista = new ArrayList<>();
                WriteBatch limpiar = db.batch();

                for (DocumentSnapshot d : snaps) {

                    HashMap<String, Object> ins = (HashMap<String, Object>) d.getData();
                    if (ins == null) continue;

                    String eventId = d.getId();
                    String ownerId = val(ins.get("ownerId"));

                    DocumentSnapshot snapEvt = Tasks.await(
                            db.collection("eventos_user_private")
                                    .document(ownerId)
                                    .collection("lista")
                                    .document(eventId)
                                    .get(Source.SERVER)
                    );

                    if (!snapEvt.exists()) {
                        limpiar.delete(d.getReference());
                        continue;
                    }

                    lista.add(new EventoUi(
                            eventId, ownerId,
                            val(snapEvt.get("nombre")),
                            val(snapEvt.get("descripcion")),
                            val(snapEvt.get("fecha")),
                            val(snapEvt.get("hora")),
                            val(snapEvt.get("lugar")),
                            getInt(snapEvt.get("plazasDisponibles")),
                            getInt(snapEvt.get("inscritos"))
                    ));
                }

                Tasks.await(limpiar.commit());
                _uiState.postValue(VerEventosApuntadoPrivateUiState.success(lista));

            } catch (Exception e) {
                Log.e(TAG, "Error cargando eventos apuntados", e);
                _uiState.postValue(
                        VerEventosApuntadoPrivateUiState.error("Error cargando tus eventos")
                );
            }
        }).start();
    }

    // ============================================================
    // DESAPUNTARSE
    // ============================================================
    public void desapuntarse(EventoUi evento) {
        if (uid == null) return;

        String eventId = evento.docId;
        String ownerId = evento.ownerId;

        var refEvt = db.collection("eventos_user_private")
                .document(ownerId).collection("lista").document(eventId);

        var refInscrito = refEvt.collection("inscritos_privados").document(uid);
        var refUser = db.collection("usuarios").document(uid)
                .collection("inscripciones_privadas").document(eventId);

        db.runTransaction(tx -> {

                    DocumentSnapshot snapEvt = tx.get(refEvt);

                    int plazas = getInt(snapEvt.get("plazasDisponibles"));

                    tx.update(refEvt, "plazasDisponibles", plazas + 1);
                    tx.delete(refInscrito);
                    tx.delete(refUser);

                    return null;

                }).addOnSuccessListener(v -> loadEventosApuntados())
                .addOnFailureListener(e ->
                        _uiState.setValue(
                                VerEventosApuntadoPrivateUiState.error("No se pudo desapuntar"))
                );
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private String val(Object o) {
        return o == null ? "" : o.toString();
    }

    private int getInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Integer) return (Integer) o;
        return 0;
    }
}
