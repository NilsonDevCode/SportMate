package com.nilson.appsportmate.comun.datos.firebase;

import java.util.Map;

public class FirebaseUtils {

    /**
     * Devuelve el nombre por ID de un mapa o "Desconocido" si no existe.
     */
    public static String getNombrePorId(String id, Map<String, String> mapa) {
        if (id == null || mapa == null) return "Desconocido";
        return mapa.containsKey(id) ? mapa.get(id) : "Desconocido";
    }

    /**
     * Valida si un string está vacío o nulo.
     */
    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // Puedes ir añadiendo más funciones útiles aquí más adelante...
}
