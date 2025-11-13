package com.nilson.appsportmate.common.utils;

import android.util.Log;

import java.text.Normalizer;

/**
 * Helper class for handling alias to email conversion and validation.
 * <p>
 * An alias is a user-friendly identifier that is converted to an email address
 * by appending a fixed domain. This allows users to sign up and log in using
 * a simple alias instead of a full email address.
 *
 * @author Nilson
 * @version 1.0
 */
public final class AuthAliasHelper {

    /// Domain with real TLD for the email
    private static final String DOMAIN = "@alias.appsportmate.com";

    /**
     * Validate alias according to rules:
     * - Required.
     * - No spaces.
     * - Starts with uppercase letter (A-Z).
     */
    public static String getAliasValidationError(String aliasRaw) {
        if (aliasRaw == null || aliasRaw.isEmpty()) return "Alias requerido";

        String alias = aliasRaw.trim();
        if (alias.contains(" "))
            return "El alias no puede tener espacios";

        if (!alias.substring(0, 1).matches("[A-Z]"))
            return "El alias debe empezar por letra mayúscula (A-Z)";

        return null;
    }

    /**
     * Convert an alias to a valid email address.
     * Rules:
     * - Lowercase.
     * - Remove diacritics (á→a, é→e, ñ→n, etc.).
     * - Only letters, numbers, '.', '_', '-' ; others → '.'.
     * - Collapse consecutive '.' into single '.'.
     * - No '.' at start or end.
     * - If empty after processing, use 'user' as alias.
     */
    public static String aliasToEmail(String aliasRaw) {
        if (aliasRaw == null) aliasRaw = "";
        String a = stripDiacritics(aliasRaw).trim().toLowerCase();

        // Only allowed characters are a-z, 0-9, '.', '_', '-'
        a = a.replaceAll("[^a-z0-9._-]", ".");
        a = a.replaceAll("\\.+", ".");
        a = a.replaceAll("^\\.|\\.$", "");
        if (a.isEmpty()) a = "user";

        String finalString = a + DOMAIN;
        Log.d("AuthAliasHelper", "aliasToEmail: " + finalString);
        return finalString;
    }

    /// Remove diacritics from a string
    private static String stripDiacritics(String input) {
        String norm = Normalizer.normalize(input, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}", "")
                .replace("Ñ", "N")
                .replace("ñ", "n");
    }
}
