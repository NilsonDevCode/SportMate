package com.nilson.appsportmate.caracteristicas.ayuntamiento.presentacion;

import com.google.firebase.firestore.CollectionReference;

import java.util.List;
import java.util.Map;

public interface GestionEventosMasPlazasView {
    // Lista de eventos (pantalla)
    void mostrarLoading(boolean show);
    void mostrarEmpty(boolean show);
    void mostrarMensaje(String msg);
    void mostrarEventos(List<Map<String, Object>> eventos);

    // Confirmaciones/diálogos
    void pedirConfirmacionBorrar(Map<String, Object> evento);
    void mostrarDialogoEditar(Map<String, Object> evento);

    // Inscritos en TIEMPO REAL
    void abrirInscritosTiempoReal(String idDoc, String titulo);
    void actualizarInscritosTiempoReal(List<String> aliases, List<String> uids);
    void cerrarInscritosTiempoReal();

    // Ref para adapter (si la necesitas)
    CollectionReference getInscritosRef(String idDoc);
}
