package com.nilson.appsportmate.features.townhall.presentacion;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.nilson.appsportmate.common.datos.firebase.FirestoreTransacciones;

import java.util.List;
import java.util.Map;

public class GestionEventosMasPlazasPresenter {

    private final GestionEventosMasPlazasView view;
    private final FirestoreTransacciones fx;
    private final String ayuntamientoId;

    // Tiempo real
    private ListenerRegistration regEventos;
    private ListenerRegistration regInscritos;

    public GestionEventosMasPlazasPresenter(GestionEventosMasPlazasView view,
                                            FirestoreTransacciones fx,
                                            String ayuntamientoId) {
        this.view = view;
        this.fx = fx;
        this.ayuntamientoId = ayuntamientoId;
    }

    /* ====== Lista eventos (one-shot opcional) ====== */
    public void cargarEventos() {
        view.mostrarLoading(true);
        fx.listarEventos(new FirestoreTransacciones.EventsResult() {
            @Override public void onSuccess(@NonNull List<Map<String, Object>> eventos) {
                view.mostrarLoading(false);
                view.mostrarEventos(eventos);
                view.mostrarEmpty(eventos == null || eventos.isEmpty());
            }
            @Override public void onError(@NonNull Exception e) {
                view.mostrarLoading(false);
                view.mostrarMensaje("Error cargando eventos: " + e.getMessage());
            }
        });
    }

    /* ====== Tiempo real: eventos ====== */
    public void suscribirTiempoRealEventos() {
        desuscribirTiempoRealEventos();
        view.mostrarLoading(true);
        regEventos = fx.escucharEventos(new FirestoreTransacciones.EventsListener() {
            @Override public void onChange(@NonNull List<Map<String, Object>> eventos) {
                view.mostrarLoading(false);
                view.mostrarEventos(eventos);
                view.mostrarEmpty(eventos.isEmpty());
            }
            @Override public void onError(@NonNull Exception e) {
                view.mostrarLoading(false);
                view.mostrarMensaje("Error tiempo real: " + e.getMessage());
            }
        });
    }

    public void desuscribirTiempoRealEventos() {
        if (regEventos != null) {
            regEventos.remove();
            regEventos = null;
        }
    }

    /* ====== Acciones rápidas ====== */
    public void incrementarPlazas(String idDoc) {
        fx.incrementarPlazas(idDoc, simpleToastReload("Plaza añadida"));
    }

    public void decrementarPlazas(String idDoc) {
        fx.decrementarPlazas(idDoc, simpleToastReload("Plaza restada"));
    }

    public void solicitarBorrado(Map<String, Object> evento) {
        view.pedirConfirmacionBorrar(evento);
    }

    public void borrarEvento(String idDoc) {
        fx.borrarEvento(idDoc, simpleToastReload("Evento borrado"));
    }

    public void solicitarEdicion(Map<String, Object> evento) {
        view.mostrarDialogoEditar(evento);
    }

    public void guardarEdicion(String oldId, String newId, Map<String, Object> nuevos) {
        if (oldId.equals(newId)) {
            fx.actualizarEventoCampos(oldId, nuevos, new FirestoreTransacciones.SimpleResult() {
                @Override public void onSuccess() {
                    view.mostrarMensaje("Actualizado");
                    if (regEventos == null) cargarEventos();
                }
                @Override public void onError(@NonNull Exception e) {
                    view.mostrarMensaje("Error: " + e.getMessage());
                }
            });
        } else {
            fx.crearEventoConMigracion(oldId, newId, nuevos, new FirestoreTransacciones.SimpleResult() {
                @Override public void onSuccess() {
                    view.mostrarMensaje("Actualizado y migrado");
                    if (regEventos == null) cargarEventos();
                }
                @Override public void onError(@NonNull Exception e) {
                    view.mostrarMensaje("Error migrando: " + e.getMessage());
                }
            });
        }
    }

    /* ====== Inscritos tiempo real ====== */

    /** Abre el diálogo y empieza a escuchar en vivo. */
    public void abrirInscritosTiempoReal(String idDoc, String titulo) {
        view.abrirInscritosTiempoReal(idDoc, titulo);
        dejarDeEscucharInscritos();
        regInscritos = fx.escucharInscritos(idDoc, new FirestoreTransacciones.InscritosListener() {
            @Override public void onChange(@NonNull List<String> aliases, @NonNull List<String> uids) {
                view.actualizarInscritosTiempoReal(aliases, uids);
            }
            @Override public void onError(@NonNull Exception e) {
                view.mostrarMensaje("Error tiempo real (inscritos): " + e.getMessage());
            }
        });
    }

    /** Cierra el diálogo y deja de escuchar. */
    public void cerrarInscritosTiempoReal() {
        dejarDeEscucharInscritos();
        view.cerrarInscritosTiempoReal();
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

    /* ====== Helpers ====== */

    private FirestoreTransacciones.SimpleResult simpleToastReload(String okMsg) {
        return new FirestoreTransacciones.SimpleResult() {
            @Override public void onSuccess() {
                view.mostrarMensaje(okMsg);
                if (regEventos == null) cargarEventos();
            }
            @Override public void onError(@NonNull Exception e) {
                view.mostrarMensaje("Error: " + e.getMessage());
            }
        };
    }

    /** Igual que en GestionDeportesAyuntamientoActivity */
    public static String generarDocId(String nombre, String fecha, String hora) {
        if (nombre == null) nombre = "";
        if (fecha  == null) fecha  = "";
        if (hora   == null) hora   = "";
        return nombre + "_" + fecha.replace("/", "_") + "_" + hora.replace(":", "_");
    }
}


