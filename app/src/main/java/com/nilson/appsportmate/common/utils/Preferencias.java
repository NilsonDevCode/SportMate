package com.nilson.appsportmate.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    public static void guardarAlias(Context context, String alias) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(Constantes.PREFS_ALIAS, alias).apply();
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
    }

    public static void guardarRol(Context context, String rol) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("rol", rol).apply();
    }

    public static String obtenerRol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("rol", null);
    }



    public static void guardarAyuntamientoId(Context context, String ayuntamientoId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("ayuntamiento_id", ayuntamientoId);
        editor.apply();
    }

    public static String obtenerAyuntamientoId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("ayuntamiento_id", null);
    }



}

