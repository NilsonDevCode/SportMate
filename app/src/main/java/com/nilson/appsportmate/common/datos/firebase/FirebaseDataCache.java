package com.nilson.appsportmate.common.datos.firebase;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDataCache {

    public static Map<String, String> mapaProfesionIdNombre = new HashMap<>();
    public static Map<String, String> mapaAyuntamientoIdNombre = new HashMap<>();
    public static Map<String, String> mapaDeporteIdNombre = new HashMap<>();
    public static Map<String, String> mapaPistaIdNombre = new HashMap<>();
    public static Map<String, String> mapaEquipoIdNombre = new HashMap<>();
    public static Map<String, String> mapaUsuarioIdNombre = new HashMap<>();
    public static Map<String, String> mapaRolIdNombre = new HashMap<>();

    public static void cargarTodosLosMapas(String uid) {
        if (uid == null) {
            Log.e("FirebaseDataCache", "UID es null. No se puede cargar la caché.");
            return;
        }

        cargarMapa("profesiones", "nombre", mapaProfesionIdNombre, uid);
        cargarMapa("ayuntamientos", "nombre", mapaAyuntamientoIdNombre, uid);
        cargarMapa("deportes", "nombre", mapaDeporteIdNombre, uid);
        cargarMapa("pistas", "nombre", mapaPistaIdNombre, uid);
        cargarMapa("equipos", "nombre", mapaEquipoIdNombre, uid);
        cargarMapa("usuarios", "nombre", mapaUsuarioIdNombre, uid);
        cargarMapa("roles", "nombre", mapaRolIdNombre, uid);
    }

    private static void cargarMapa(String coleccion, String campo, Map<String, String> mapa, String uid) {
        FirebaseFirestore.getInstance()
                .collection(coleccion)
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mapa.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        mapa.put(doc.getId(), doc.getString(campo));
                    }
                    Log.d("FirebaseDataCache", "✅ " + coleccion + ": " + mapa.size() + " elementos cargados.");
                })
                .addOnFailureListener(e -> Log.e("FirebaseDataCache", "❌ Error al cargar " + coleccion, e));
    }

    public static void limpiarTodosLosMapas() {
        mapaProfesionIdNombre.clear();
        mapaAyuntamientoIdNombre.clear();
        mapaDeporteIdNombre.clear();
        mapaPistaIdNombre.clear();
        mapaEquipoIdNombre.clear();
        mapaUsuarioIdNombre.clear();
        mapaRolIdNombre.clear();

        Log.d("FirebaseDataCache", "🧹 Todos los mapas han sido limpiados.");
    }
}
