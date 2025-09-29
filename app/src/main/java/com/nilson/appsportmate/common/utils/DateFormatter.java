package com.nilson.appsportmate.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static final SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final SimpleDateFormat formatoSoloFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static String formatearFechaHora(Date fecha) {
        return formatoFechaHora.format(fecha);
    }

    public static String formatearSoloFecha(Date fecha) {
        return formatoSoloFecha.format(fecha);
    }
}
