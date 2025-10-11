package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.nilson.appsportmate.common.datos.firebase.FirestoreTransacciones;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lógica y persistencia para Gestión de eventos (MVVM).
 * Mismas operaciones que el Presenter original.
 */
public class GestionEventosMasPlazasViewModel extends ViewModel {

    // Estado base
    private String ayuntamientoId;
    private FirestoreTransacciones fx;

    // Estado UI
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> empty   = new MutableLiveData<>(true);
    private final MutableLiveData<String>  mensaje = new MutableLiveData<>("");

    private final MutableLiveData<List<Map<String, Object>>> eventos =
            new MutableLiveData<>(new ArrayList<>());

    // Tiempo real
    private ListenerRegistration regEventos;

    // Inscritos (tiempo real)
    private ListenerRegistration regInscritos;
    private final MutableLiveData<Pair<String, String>> openInscritosEvent = new MutableLiveData<>();


    private final MutableLiveData<Pair<List<String>, List<String>>> inscritosData = new MutableLiveData<>();

    // ---------------------------
    // Init
    // ---------------------------

    public void setAyuntamientoId(String id) {
        this.ayuntamientoId = id;
        if (this.fx == null) {
            this.fx = new FirestoreTransacciones(FirebaseFirestore.getInstance(), ayuntamientoId);
        }
    }

    // ---------------------------
    // Exposición estado
    // ---------------------------

    public LiveData<Pair<List<String>, List<String>>> getInscritosData() { return inscritosData; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getEmpty()   { return empty; }
    public LiveData<String>  getMensaje() { return mensaje; }
    public LiveData<List<Map<String, Object>>> getEventos() { return eventos; }

    public LiveData<Pair<String,String>> getOpenInscritosEvent() { return openInscritosEvent; }


    // ---------------------------
    // Carga one-shot (opcional)
    // ---------------------------

    public void cargarEventos() {
        loading.postValue(true);
        fx.listarEventos(new FirestoreTransacciones.EventsResult() {
            @Override public void onSuccess(@NonNull List<Map<String, Object>> list) {
                loading.postValue(false);
                eventos.postValue(list);
                empty.postValue(list == null || list.isEmpty());
            }
            @Override public void onError(@NonNull Exception e) {
                loading.postValue(false);
                mensaje.postValue("Error cargando eventos: " + e.getMessage());
            }
        });
    }

    // ---------------------------
    // Tiempo real: eventos
    // ---------------------------

    public void suscribirTiempoRealEventos() {
        desuscribirTiempoRealEventos();
        loading.postValue(true);
        regEventos = fx.escucharEventos(new FirestoreTransacciones.EventsListener() {
            @Override public void onChange(@NonNull List<Map<String, Object>> list) {
                loading.postValue(false);
                eventos.postValue(list);
                empty.postValue(list == null || list.isEmpty());
            }
            @Override public void onError(@NonNull Exception e) {
                loading.postValue(false);
                mensaje.postValue("Error tiempo real: " + e.getMessage());
            }
        });
    }

    public void desuscribirTiempoRealEventos() {
        if (regEventos != null) {
            regEventos.remove();
            regEventos = null;
        }
    }

    // ---------------------------
    // Acciones rápidas
    // ---------------------------

    public void incrementarPlazas(String idDoc) {
        fx.incrementarPlazas(idDoc, simpleToastReload("Plaza añadida"));
    }

    public void decrementarPlazas(String idDoc) {
        fx.decrementarPlazas(idDoc, simpleToastReload("Plaza restada"));
    }

    public void borrarEvento(String idDoc) {
        fx.borrarEvento(idDoc, simpleToastReload("Evento borrado"));
    }

    public void guardarEdicion(String oldId, String newId, Map<String, Object> nuevos) {
        if (oldId.equals(newId)) {
            fx.actualizarEventoCampos(oldId, nuevos, new FirestoreTransacciones.SimpleResult() {
                @Override public void onSuccess() {
                    mensaje.postValue("Actualizado");
                    if (regEventos == null) cargarEventos();
                }
                @Override public void onError(@NonNull Exception e) {
                    mensaje.postValue("Error: " + e.getMessage());
                }
            });
        } else {
            fx.crearEventoConMigracion(oldId, newId, nuevos, new FirestoreTransacciones.SimpleResult() {
                @Override public void onSuccess() {
                    mensaje.postValue("Actualizado y migrado");
                    if (regEventos == null) cargarEventos();
                }
                @Override public void onError(@NonNull Exception e) {
                    mensaje.postValue("Error migrando: " + e.getMessage());
                }
            });
        }
    }

    // ---------------------------
    // Inscritos en tiempo real
    // ---------------------------

    public void abrirInscritosTiempoReal(String idDoc, String titulo) {
        openInscritosEvent.postValue(new Pair<>(idDoc, titulo));
        dejarDeEscucharInscritos();
        regInscritos = fx.escucharInscritos(idDoc, new FirestoreTransacciones.InscritosListener() {
            @Override public void onChange(@NonNull List<String> aliases, @NonNull List<String> uids) {
                inscritosData.postValue(new Pair<>(aliases, uids));
            }
            @Override public void onError(@NonNull Exception e) {
                mensaje.postValue("Error tiempo real (inscritos): " + e.getMessage());
            }
        });
    }

    public void cerrarInscritosTiempoReal() {
        dejarDeEscucharInscritos();
        // El cierre visual lo realiza el Fragment
    }

    public void dejarDeEscucharInscritos() {
        if (regInscritos != null) {
            regInscritos.remove();
            regInscritos = null;
        }
    }

    public void expulsarInscrito(String idDoc, String uidSeleccionado) {
        fx.expulsarInscrito(idDoc, uidSeleccionado, simpleToastReload("Usuario expulsado"));
    }

    public CollectionReference getInscritosRef(String idDoc) {
        return fx.inscritosRef(idDoc);
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    private FirestoreTransacciones.SimpleResult simpleToastReload(String okMsg) {
        return new FirestoreTransacciones.SimpleResult() {
            @Override public void onSuccess() {
                mensaje.postValue(okMsg);
                if (regEventos == null) cargarEventos();
            }
            @Override public void onError(@NonNull Exception e) {
                mensaje.postValue("Error: " + e.getMessage());
            }
        };
    }

    /** Igual que el Presenter original */
    public static String generarDocId(String nombre, String fecha, String hora) {
        if (nombre == null) nombre = "";
        if (fecha  == null) fecha  = "";
        if (hora   == null) hora   = "";
        return nombre + "_" + fecha.replace("/", "_") + "_" + hora.replace(":", "_");
    }

    @Override
    protected void onCleared() {
        desuscribirTiempoRealEventos();
        dejarDeEscucharInscritos();
        super.onCleared();
    }
}
