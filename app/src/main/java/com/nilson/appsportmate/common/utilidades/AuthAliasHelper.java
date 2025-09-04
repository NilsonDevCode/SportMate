package com.nilson.appsportmate.common.utilidades;

import java.text.Normalizer;

public final class AuthAliasHelper {
    private AuthAliasHelper() {}

    // Dominio con TLD real para el email sintético
    private static final String DOMAIN = "@alias.appsportmate.com";

    /**
     * Valida el alias:
     * - Sin espacios.
     * - Debe empezar por MAYÚSCULA (A-Z).
     * El resto puede ser cualquier cosa.
     */
    public static String getAliasValidationError(String aliasRaw) {
        if (aliasRaw == null) return "Alias requerido";
        String alias = aliasRaw.trim();
        if (alias.isEmpty()) return "Alias requerido";

        if (alias.contains(" "))
            return "El alias no puede tener espacios";

        if (!alias.substring(0, 1).matches("[A-Z]"))
            return "El alias debe empezar por letra mayúscula (A-Z)";

        return null; // ✅ válido
    }

    /**
     * Convierte alias a email sintético válido para Firebase Auth:
     * - Quita tildes/ñ.
     * - Minúsculas.
     * - Sustituye lo no permitido en emails por '.'.
     * - Añade dominio con TLD.
     */
    public static String aliasToEmail(String aliasRaw) {
        if (aliasRaw == null) aliasRaw = "";
        String a = stripDiacritics(aliasRaw).trim().toLowerCase();
        // Solo dejamos letras, números, '.', '_', '-' ; lo demás → '.'
        a = a.replaceAll("[^a-z0-9._-]", ".");
        a = a.replaceAll("\\.+", ".");     // colapsa '...'
        a = a.replaceAll("^\\.|\\.$", ""); // sin '.' al inicio/fin
        if (a.isEmpty()) a = "user";
        return a + DOMAIN;
    }

    /** Elimina diacríticos: á→a, é→e, ñ→n, etc. */
    private static String stripDiacritics(String input) {
        String norm = Normalizer.normalize(input, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}", "")
                .replace("Ñ", "N")
                .replace("ñ", "n");
    }
}
