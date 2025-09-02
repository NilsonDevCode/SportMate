package com.nilson.appsportmate.comun.utilidades;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import androidx.annotation.Nullable;

public class ValidacionesEvento {

    private static final SimpleDateFormat F_FECHA = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat F_HORA  = new SimpleDateFormat("HH:mm");
    static {
        F_FECHA.setLenient(false);
        F_HORA.setLenient(false);
    }

    /** Devuelve null si OK; mensaje de error si no. */
    @Nullable
    public static String validarFechaHoraFuturas(String fecha, String hora) {
        if (fecha == null || fecha.trim().isEmpty()) return "Fecha requerida";
        if (hora  == null || hora.trim().isEmpty())  return "Hora requerida";
        try {
            Date d = F_FECHA.parse(fecha.trim());
            Date t = F_HORA.parse(hora.trim());

            // Combina fecha y hora en un Calendar
            Calendar calFecha = Calendar.getInstance();
            calFecha.setTime(d);

            Calendar calHora = Calendar.getInstance();
            calHora.setTime(t);

            Calendar combinado = Calendar.getInstance();
            combinado.set(calFecha.get(Calendar.YEAR),
                    calFecha.get(Calendar.MONTH),
                    calFecha.get(Calendar.DAY_OF_MONTH),
                    calHora.get(Calendar.HOUR_OF_DAY),
                    calHora.get(Calendar.MINUTE),
                    0);
            combinado.set(Calendar.MILLISECOND, 0);

            if (combinado.getTimeInMillis() <= System.currentTimeMillis()) {
                return "La fecha y hora deben ser futuras";
            }
            return null;
        } catch (ParseException e) {
            return "Formato de fecha u hora inválido (usa dd/MM/yyyy y HH:mm)";
        }
    }

    /** Devuelve null si OK; mensaje de error si no. */
    @Nullable
    public static String validarPlazas(String plazasStr, int min, int max) {
        if (plazasStr == null || plazasStr.trim().isEmpty())
            return "Plazas requeridas";
        try {
            int n = Integer.parseInt(plazasStr.trim());
            if (n < min) return "Mínimo " + min + " plazas";
            if (n > max) return "Máximo " + max + " plazas";
            return null;
        } catch (NumberFormatException e) {
            return "Número de plazas inválido";
        }
    }
}
