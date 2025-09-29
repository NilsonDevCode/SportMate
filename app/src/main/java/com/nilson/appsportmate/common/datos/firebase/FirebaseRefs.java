package com.nilson.appsportmate.common.datos.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseRefs {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    // ✅ UID actual del usuario autenticado
    public static String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // ✅ Colecciones directas
    public static CollectionReference usuarios() {
        return db.collection("usuarios");
    }

    public static CollectionReference ayuntamientos() {
        return db.collection("ayuntamientos");
    }

    public static CollectionReference deportes() {
        return db.collection("deportes");
    }

    public static CollectionReference pistas() {
        return db.collection("pistas");
    }

    public static CollectionReference partidos() {
        return db.collection("partidos");
    }

    public static CollectionReference equipos() {
        return db.collection("equipos");
    }

    // ✅ Accesos explícitos (para legibilidad y modularidad)
    public static CollectionReference getUsuariosRef() {
        return usuarios();
    }

    public static CollectionReference getAyuntamientosRef() {
        return ayuntamientos();
    }

    public static CollectionReference getDeportesRef() {
        return deportes();
    }

    public static CollectionReference getPistasRef() {
        return pistas();
    }

    public static CollectionReference getPartidosRef() {
        return partidos();
    }

    public static CollectionReference getEquiposRef() {
        return equipos();
    }

    // ✅ Referencias a instancias
    // ✅ Instancia de FirebaseAuth
    public static FirebaseAuth getAuth() {
        return auth;
    }

    // ✅ Instancia de FirebaseFirestore
    public static FirebaseFirestore getDb() {
        return db;
    }

    // ✅ Referencia a la colección usuariosAuth
    public static CollectionReference usuariosAuth() {
        return db.collection("usuariosAuth");
    }

    public static CollectionReference deportesAyuntamiento() {
        return db.collection("deportes_ayuntamiento");
    }
}
