package com.nilson.appsportmate.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    /* ==== Alias / UID / Tipo ==== */

    public static void guardarAlias(Context context, String alias) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(Constantes.PREFS_ALIAS, alias).apply();
    }

    public static void guardarNombreUsuario(Context context, String nombre) {
        SharedPreferences p = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        p.edit().putString("nombreUsuario", nombre).apply();
    }

    public static String obtenerNombreUsuario(Context context) {
        SharedPreferences p = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return p.getString("nombreUsuario", "");
    }

    public static String obtenerAlias(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getString(Constantes.PREFS_ALIAS, null);
    }

    public static void guardarUid(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(Constantes.PREFS_UID, uid).apply();
    }

    public static String obtenerUid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getString(Constantes.PREFS_UID, null);
    }

    public static void guardarTipoUsuario(Context context, String tipo) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString("tipo_usuario", tipo).apply();
    }

    public static String obtenerTipoUsuario(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getString("tipo_usuario", "normal"); // por defecto
    }

    public static void borrarTodo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        SharedPreferences appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        appPrefs.edit().clear().apply();
    }

    /* ==== Rol / App prefs ==== */

    public static void guardarRol(Context context, String rol) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("rol", rol).apply();
    }

    public static String obtenerRol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("rol", null);
    }

    /* ==== Ayuntamiento (ID + nombre) ==== */

    public static void guardarAyuntamientoId(Context context, String ayuntamientoId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("ayuntamiento_id", ayuntamientoId);
        editor.apply();
    }

    public static String obtenerAyuntamientoId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("ayuntamiento_id", null);
    }

    public static void guardarAyuntamientoNombre(Context context, String ayuntamientoNombre) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("ayuntamiento_nombre", ayuntamientoNombre);
        editor.apply();
    }

    public static String obtenerAyuntamientoNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("ayuntamiento_nombre", null);
    }

    /* ==== Pueblo ==== */

    public static void guardarPuebloId(Context context, String puebloId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("pueblo_id", puebloId);
        editor.apply();
    }

    public static String obtenerPuebloId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("pueblo_id", null);
    }

    public static void guardarPuebloNombre(Context context, String puebloNombre) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("pueblo_nombre", puebloNombre);
        editor.apply();
    }

    public static String obtenerPuebloNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("pueblo_nombre", null);
    }

    /* ==== NUEVO: Foto de perfil del usuario ==== */

    public static void guardarFotoUrlUsuario(Context context, String url) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("foto_url_usuario", url);
        editor.apply();
    }

    public static String obtenerFotoUrlUsuario(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("foto_url_usuario", null);
    }

    public static String obtenerLocalidad(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("localidad", null);
    }

    public static void guardarLocalidad(Context context, String localidad) {
        SharedPreferences prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        prefs.edit().putString("localidad", localidad).apply();
    }

}
