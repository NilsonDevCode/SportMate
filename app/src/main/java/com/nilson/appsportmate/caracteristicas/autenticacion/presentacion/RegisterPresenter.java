package com.nilson.appsportmate.caracteristicas.autenticacion.presentacion;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.comun.utilidades.AuthAliasHelper;
import com.nilson.appsportmate.comun.utilidades.Preferencias;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterPresenter {
    private final RegisterView view;
    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public RegisterPresenter(RegisterView view, Context context) {
        this.view = view;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void onRegisterClicked() {
        String aliasInput = view.getAliasInput();
        String pass1 = view.getPassword1();
        String pass2 = view.getPassword2();
        String nombre = view.getNombre();
        String apellidos = view.getApellidos();
        String comunidadNombre = view.getComunidad();
        String provinciaNombre = view.getProvincia();
        String ciudadNombre = view.getCiudad();
        String puebloNombre = view.getPueblo();
        String razonSocial = view.getRazonSocial();
        String rol = view.getRol();

        String aliasErr = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (aliasErr != null) { view.mostrarErrorAlias(aliasErr); return; }
        if (TextUtils.isEmpty(pass1)) { view.mostrarErrorPassword("Contraseña requerida"); return; }
        if (!pass1.equals(pass2)) { view.mostrarErrorPassword("Las contraseñas no coinciden"); return; }
        if (pass1.length() < 6) { view.mostrarErrorPassword("Mínimo 6 caracteres"); return; }
        if (TextUtils.isEmpty(nombre)) { view.mostrarErrorNombre("Nombre requerido"); return; }

        if ("usuario".equals(rol)) {
            if (TextUtils.isEmpty(apellidos)) { view.mostrarErrorApellidos("Apellidos requeridos"); return; }
            if (TextUtils.isEmpty(puebloNombre)) { view.mostrarMensaje("Selecciona un pueblo"); return; }
            if (TextUtils.isEmpty(view.getAyuntamientoSeleccionadoId())) {
                view.mostrarMensaje("No se pudo resolver el ayuntamiento del pueblo seleccionado");
                return;
            }
        } else { // ayuntamiento
            if (TextUtils.isEmpty(razonSocial)) { view.mostrarErrorRazonSocial("Razón social requerida"); return; }
        }

        String emailSintetico = AuthAliasHelper.aliasToEmail(aliasInput);

        auth.fetchSignInMethodsForEmail(emailSintetico)
                .addOnSuccessListener((SignInMethodQueryResult r) -> {
                    boolean existe = r.getSignInMethods() != null && !r.getSignInMethods().isEmpty();
                    if (existe) {
                        view.mostrarErrorAlias("Alias en uso. Inicia sesión o elige otro.");
                        return;
                    }
                    crearUsuario(emailSintetico, pass1, aliasInput, rol,
                            nombre, apellidos, comunidadNombre, provinciaNombre, ciudadNombre, puebloNombre, razonSocial);
                })
                .addOnFailureListener(e -> view.mostrarMensaje("Error comprobando alias: " + e.getMessage()));
    }

    private void crearUsuario(String email, String pass1, String aliasInput, String rol,
                              String nombre, String apellidos, String comunidadNombre, String provinciaNombre,
                              String ciudadNombre, String puebloNombre, String razonSocial) {

        auth.createUserWithEmailAndPassword(email, pass1)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String id  = UUID.randomUUID().toString();

                    // --------- Datos comunes (para perfiles) ----------
                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("id", id);
                    perfil.put("uid", uid);
                    perfil.put("alias", aliasInput);
                    perfil.put("rol", rol);
                    perfil.put("nombre", nombre);
                    perfil.put("comunidad", comunidadNombre);
                    perfil.put("provincia", provinciaNombre);
                    perfil.put("ciudad", ciudadNombre);
                    perfil.put("pueblo", puebloNombre);

                    // Índice único para reglas/lookups
                    Map<String, Object> authDoc = new HashMap<>();
                    authDoc.put("alias", aliasInput);
                    authDoc.put("uid", uid);
                    authDoc.put("rol", rol);

                    // --------- PRIMER COMMIT (batch) ----------
                    WriteBatch batch1 = db.batch();

                    if ("ayuntamiento".equals(rol)) {
                        // ayuntamientos/{uid}
                        Map<String, Object> perfilAyto = new HashMap<>(perfil);
                        perfilAyto.put("razonSocial", razonSocial);
                        perfilAyto.put("ayuntamientoId", uid);
                        batch1.set(db.collection("ayuntamientos").document(uid), perfilAyto, SetOptions.merge());
                    } else {
                        // usuarios/{uid}
                        Map<String, Object> perfilUsuario = new HashMap<>(perfil);
                        perfilUsuario.put("apellidos", apellidos);
                        perfilUsuario.put("ayuntamientoId", view.getAyuntamientoSeleccionadoId());
                        batch1.set(db.collection("usuarios").document(uid), perfilUsuario, SetOptions.merge());
                    }

                    // ✅ Solo usuariosAuth
                    batch1.set(db.collection("usuariosAuth").document(uid), authDoc, SetOptions.merge());

                    batch1.commit()
                            .addOnSuccessListener(unused -> {
                                // --------- SEGUNDA ESCRITURA (crear pueblo si es ayto) ----------
                                if ("ayuntamiento".equals(rol)) {
                                    String nuevoPueblo = view.getPueblo();
                                    String comunidadId = view.getComunidadIdSel();
                                    String provinciaId = view.getProvinciaIdSel();
                                    String ciudadId    = view.getCiudadIdSel();

                                    if (!TextUtils.isEmpty(nuevoPueblo)
                                            && !TextUtils.isEmpty(comunidadId)
                                            && !TextUtils.isEmpty(provinciaId)
                                            && !TextUtils.isEmpty(ciudadId)) {

                                        Map<String, Object> puebloDoc = new HashMap<>();
                                        puebloDoc.put("nombre", nuevoPueblo.trim());
                                        puebloDoc.put("comunidadId", comunidadId);
                                        puebloDoc.put("provinciaId", provinciaId);
                                        puebloDoc.put("ciudadId", ciudadId);
                                        puebloDoc.put("createdByUid", uid);
                                        puebloDoc.put("createdAt", FieldValue.serverTimestamp());
                                        // Denormalizado opcional
                                        puebloDoc.put("comunidadNombre", comunidadNombre);
                                        puebloDoc.put("provinciaNombre", provinciaNombre);
                                        puebloDoc.put("ciudadNombre", ciudadNombre);

                                        db.collection("pueblos").add(puebloDoc)
                                                .addOnFailureListener(e ->
                                                        view.mostrarMensaje("Perfil OK pero falló crear pueblo: " + e.getMessage()));
                                    }
                                }

                                // Preferencias y navegación
                                Preferencias.guardarUid(context, uid);
                                Preferencias.guardarAlias(context, aliasInput);
                                Preferencias.guardarRol(context, rol);
                                if ("ayuntamiento".equals(rol)) {
                                    Preferencias.guardarAyuntamientoId(context, uid);
                                    view.navegarAyuntamiento();
                                } else {
                                    Preferencias.guardarAyuntamientoId(context, view.getAyuntamientoSeleccionadoId());
                                    view.navegarUsuario();
                                }
                                view.mostrarMensaje("Registro exitoso.");
                            })
                            .addOnFailureListener(e ->
                                    view.mostrarMensaje("Error guardando perfil: " + e.getMessage()));
                })
                .addOnFailureListener(e -> view.mostrarMensaje("Registro fallido: " + e.getMessage()));
    }

    // ============================
    // INSCRIPCIONES (APUNTARSE/BAJA)
    // ============================

    // APUNTARSE
    public void apuntarse(String aytoId, String docId, String alias) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1) Crear inscripción en el perfil del usuario
        Map<String, Object> insc = new HashMap<>();
        insc.put("ayuntamientoId", aytoId);
        insc.put("idDoc", docId);
        insc.put("alias", alias);
        insc.put("createdAt", FieldValue.serverTimestamp());

        db.collection("usuarios").document(uid)
                .collection("inscripciones").document(docId)
                .set(insc)
                .addOnSuccessListener(v -> {
                    // 2) Crear marca en inscritos del evento
                    Map<String, Object> marca = new HashMap<>();
                    marca.put("alias", alias);
                    marca.put("ayuntamientoId", aytoId);

                    db.collection("deportes_ayuntamiento").document(aytoId)
                            .collection("lista").document(docId)
                            .collection("inscritos").document(uid)
                            .set(marca)
                            .addOnSuccessListener(v2 -> {
                                // 3) Decrementar plazas
                                db.collection("deportes_ayuntamiento").document(aytoId)
                                        .collection("lista").document(docId)
                                        .update("plazasDisponibles", FieldValue.increment(-1))
                                        .addOnSuccessListener(v3 -> view.mostrarMensaje("Inscripción realizada"))
                                        .addOnFailureListener(e3 -> view.mostrarMensaje("Fallo PASO3 plazas: " + e3.getMessage()));
                            })
                            .addOnFailureListener(e2 -> view.mostrarMensaje("Fallo PASO2 inscritos: " + e2.getMessage()));
                })
                .addOnFailureListener(e -> view.mostrarMensaje("Fallo PASO1 inscripción: " + e.getMessage()));
    }

    // DESAPUNTARSE
    public void desapuntarse(String aytoId, String docId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1) Quitar marca en inscritos del evento
        db.collection("deportes_ayuntamiento").document(aytoId)
                .collection("lista").document(docId)
                .collection("inscritos").document(uid)
                .delete()
                .addOnSuccessListener(v -> {
                    // 2) Incrementar plazas
                    db.collection("deportes_ayuntamiento").document(aytoId)
                            .collection("lista").document(docId)
                            .update("plazasDisponibles", FieldValue.increment(+1))
                            .addOnSuccessListener(v2 -> {
                                // 3) Borrar inscripción en perfil del usuario
                                db.collection("usuarios").document(uid)
                                        .collection("inscripciones").document(docId)
                                        .delete()
                                        .addOnSuccessListener(v3 -> view.mostrarMensaje("Baja realizada"))
                                        .addOnFailureListener(e3 -> view.mostrarMensaje("Borré plaza pero no tu inscripción: " + e3.getMessage()));
                            })
                            .addOnFailureListener(e2 -> view.mostrarMensaje("No pude sumar plaza: " + e2.getMessage()));
                })
                .addOnFailureListener(e -> view.mostrarMensaje("No pude eliminar de inscritos: " + e.getMessage()));
    }
}
