package com.nilson.appsportmate.features.user.ui.eventosPrivados.gestionEventosPrivate;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.nilson.appsportmate.common.datos.firebase.FirestoreTransaccionesPrivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GestionEventosUserPrivateViewModel extends ViewModel {

    private String uidUsuario;
    private FirestoreTransaccionesPrivate fx;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> empty   = new MutableLiveData<>(true);
    private final MutableLiveData<String>  mensaje = new MutableLiveData<>("");

    private final MutableLiveData<List<Map<String, Object>>> eventos =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration regEventos;
    private ListenerRegistration regInscritos;

    private final MutableLiveData<Pair<String, String>> openInscritosEvent = new MutableLiveData<>();
    private final MutableLiveData<Pair<List<String>, List<String>>> inscritosData = new MutableLiveData<>();

    /* ==========================================================
     * SET UID Y CREAR EL SERVICIO PRIVADO
     * ========================================================== */
    public void setUidUsuario(String uid) {
        this.uidUsuario = uid;

        if (fx == null) {
            fx = new FirestoreTransaccionesPrivate(
                    FirebaseFirestore.getInstance(),
                    uidUsuario   // ownerId del evento privado
            );
        }
    }

    /* ==========================================================
     * GETTERS
     * ========================================================== */
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getEmpty()   { return empty; }
    public LiveData<String>  getMensaje() { return mensaje; }
    public LiveData<List<Map<String, Object>>> getEventos() { return eventos; }
    public LiveData<Pair<String,String>> getOpenInscritosEvent() { return openInscritosEvent; }
    public LiveData<Pair<List<String>, List<String>>> getInscritosData() { return inscritosData; }

    /* ==========================================================
     * TIEMPO REAL → EVENTOS PRIVADOS
     * ========================================================== */
    public void suscribirTiempoRealEventos() {
        desuscribirTiempoRealEventos();
        loading.postValue(true);

        regEventos = fx.escucharEventos(new FirestoreTransaccionesPrivate.EventsListener() {
            @Override public void onChange(@NonNull List<Map<String, Object>> list) {
                loading.postValue(false);
                eventos.postValue(list);
                empty.postValue(list.isEmpty());
            }

            @Override public void onError(@NonNull Exception e) {
                mensaje.postValue("Error tiempo real: " + e.getMessage());
                loading.postValue(false);
            }
        });
    }

    public void desuscribirTiempoRealEventos() {
        if (regEventos != null) {
            regEventos.remove();
            regEventos = null;
        }
    }

    /* ==========================================================
     * CRUD → Plazas Privadas
     * ========================================================== */
    public void incrementarPlazas(String idDoc) {
        fx.incrementarPlazas(idDoc, simpleToastReload("Plaza añadida"));
    }

    public void decrementarPlazas(String idDoc) {
        fx.decrementarPlazas(idDoc, simpleToastReload("Plaza restada"));
    }

    /* ==========================================================
     * BORRAR EVENTO PRIVADO
     * ========================================================== */
    public void borrarEvento(String idDoc) {
        fx.borrarEvento(idDoc, simpleToastReload("Evento borrado"));
    }

    public void editarEvento(Map<String,Object> evento) {
        mensaje.postValue("Editar evento privado (pendiente)");
    }

    /* ==========================================================
     * INSCRITOS PRIVADOS → RealTime
     * ========================================================== */
    public void abrirInscritosTiempoReal(String idDoc, String titulo) {
        openInscritosEvent.postValue(new Pair<>(idDoc, titulo));
        dejarDeEscucharInscritos();

        regInscritos = fx.escucharInscritos(idDoc,
                new FirestoreTransaccionesPrivate.InscritosListener() {
                    @Override
                    public void onChange(@NonNull List<String> aliases,
                                         @NonNull List<String> uids) {
                        inscritosData.postValue(new Pair<>(aliases, uids));
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        mensaje.postValue("Error inscritos: " + e.getMessage());
                    }
                }
        );
    }

    public void cerrarInscritosTiempoReal() {
        dejarDeEscucharInscritos();
    }

    public void dejarDeEscucharInscritos() {
        if (regInscritos != null) {
            regInscritos.remove();
            regInscritos = null;
        }
    }

    /* ==========================================================
     * EXPULSAR USUARIO PRIVADO
     * ========================================================== */
    public void expulsarInscrito(String idDoc, String uid) {
        fx.expulsarInscrito(idDoc, uid, simpleToastReload("Usuario expulsado"));
    }

    public CollectionReference getInscritosRef(String idDoc) {
        return fx.inscritosRef(idDoc);
    }

    /* ==========================================================
     * Helper: Mensaje de OK/Error
     * ========================================================== */
    private FirestoreTransaccionesPrivate.SimpleResult simpleToastReload(String okMsg) {
        return new FirestoreTransaccionesPrivate.SimpleResult() {
            @Override public void onSuccess() {
                mensaje.postValue(okMsg);
            }

            @Override public void onError(@NonNull Exception e) {
                mensaje.postValue("Error: " + e.getMessage());
            }
        };
    }

    /* ==========================================================
     * LIMPIEZA
     * ========================================================== */
    @Override
    protected void onCleared() {
        desuscribirTiempoRealEventos();
        dejarDeEscucharInscritos();
        super.onCleared();
    }
}
