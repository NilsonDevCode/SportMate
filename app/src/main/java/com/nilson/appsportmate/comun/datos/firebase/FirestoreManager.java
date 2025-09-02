package com.nilson.appsportmate.comun.datos.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * FirestoreManager unificado:
 * - CRUD utilidades
 * - Gestión de deportes_ayuntamiento
 * - Preferencias locales
 * - Resolución de rol por UID (usuariosAuth/{uid}) + autorreparación
 * - Callbacks SIEMPRE en MAIN thread
 */
public class FirestoreManager {

    // ===== MAIN thread helper (evita "Can't toast on a thread...") =====
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static void postMain(Runnable r) { MAIN.post(r); }

    // ===========================
    // ====== UTILIDADES CRUD ====
    // ===========================

    public static void crearDocumento(CollectionReference ref, String id, Map<String, Object> data, String logTag) {
        ref.document(id).set(data)
                .addOnSuccessListener(unused -> Log.d(logTag, "Documento creado correctamente"))
                .addOnFailureListener(e -> Log.e(logTag, "Error al crear documento", e));
    }

    public static void actualizarDocumento(CollectionReference ref, String id, Map<String, Object> data, String logTag) {
        ref.document(id).update(data)
                .addOnSuccessListener(unused -> Log.d(logTag, "Documento actualizado correctamente"))
                .addOnFailureListener(e -> Log.e(logTag, "Error al actualizar documento", e));
    }

    public static void eliminarDocumento(CollectionReference ref, String id, String logTag) {
        ref.document(id).delete()
                .addOnSuccessListener(unused -> Log.d(logTag, "Documento eliminado correctamente"))
                .addOnFailureListener(e -> Log.e(logTag, "Error al eliminar documento", e));
    }

    public static void obtenerDocumentosFiltrados(CollectionReference ref, String campo, String valor, EventListener<QuerySnapshot> listener) {
        ref.whereEqualTo(campo, valor).addSnapshotListener(listener);
    }

    // ===========================
    // == deportes_ayuntamiento ==
    // ===========================

    public static void obtenerDeportesPorAyuntamiento(String ayuntamientoId,
                                                      OnSuccessListener<List<String>> onSuccess,
                                                      OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> lista = (List<String>) documentSnapshot.get("deportes");
                        onSuccess.onSuccess(lista != null ? lista : new ArrayList<>());
                    } else {
                        onSuccess.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public static void guardarDeportesAyuntamiento(String ayuntamientoId, List<String> listaDeportes,
                                                   OnSuccessListener<Void> onSuccess,
                                                   OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> datos = new HashMap<>();
        datos.put("deportes", listaDeportes);

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .set(datos)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // ===========================
    // ========= Preferencias ====
    // ===========================

    public static void guardarAyuntamientoId(Context context, String ayuntamientoId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("ayuntamiento_id", ayuntamientoId);
        editor.apply();
    }

    // ===========================================
    // === RESOLUCIÓN DE ROL Y AUTORREPARACIÓN ===
    // ===========================================

    public interface RoleCallback {
        /** rol: "usuario", "ayuntamiento" o null si no hay perfil */
        void onResolved(@Nullable String rol);
        void onError(Exception e);
    }

    /**
     * Lee usuariosAuth/{uid}. Si falta o no tiene 'rol', repara buscando:
     * - ayuntamientos/{uid}  -> rol = ayuntamiento
     * - usuarios/{uid}       -> rol = usuario
     * - (compat) deportistas/{uid} -> rol = usuario
     */
    public static void resolveRolOrRepair(String uid, String alias, RoleCallback cb) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuariosAuth").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String rol = safeLower(doc.getString("rol"));
                        if (rol == null || rol.isEmpty()) {
                            autoRepairUsuariosAuth(db, uid, alias, cb);
                        } else {
                            postMain(() -> cb.onResolved(rol));
                        }
                    } else {
                        autoRepairUsuariosAuth(db, uid, alias, cb);
                    }
                })
                .addOnFailureListener(e -> postMain(() -> cb.onError(e)));
    }

    // Reconstruye usuariosAuth/{uid} si falta
    private static void autoRepairUsuariosAuth(FirebaseFirestore db, String uid, String alias, RoleCallback cb) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1) ¿Existe en ayuntamientos/{uid}?
                DocumentSnapshot ay = Tasks.await(db.collection("ayuntamientos").document(uid).get());
                if (ay.exists()) { createUsuariosAuth(db, uid, alias, "ayuntamiento", cb); return; }

                // 2) ¿Existe en usuarios/{uid}?
                DocumentSnapshot us = Tasks.await(db.collection("usuarios").document(uid).get());
                if (us.exists()) { createUsuariosAuth(db, uid, alias, "usuario", cb); return; }

                // 3) Compat: ¿existe en deportistas/{uid}? -> tratar como usuario
                DocumentSnapshot dep = Tasks.await(db.collection("deportistas").document(uid).get());
                if (dep.exists()) { createUsuariosAuth(db, uid, alias, "usuario", cb); return; }

                // 4) No hay perfil en ninguna
                postMain(() -> cb.onResolved(null));
            } catch (Exception e) {
                postMain(() -> cb.onError(e));
            }
        });
    }

    private static void createUsuariosAuth(FirebaseFirestore db, String uid, String alias, String rol, RoleCallback cb) {
        Map<String, Object> authDoc = new HashMap<>();
        authDoc.put("uid", uid);          // SIEMPRE UID, no alias
        authDoc.put("alias", alias);      // informativo
        authDoc.put("rol", rol);          // "usuario" | "ayuntamiento"

        db.collection("usuariosAuth").document(uid)
                .set(authDoc, SetOptions.merge())
                .addOnSuccessListener(v -> postMain(() -> cb.onResolved(rol)))
                .addOnFailureListener(e -> postMain(() -> cb.onError(e)));
    }

    private static String safeLower(@Nullable String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
}
