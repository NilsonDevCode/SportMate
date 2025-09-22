package com.nilson.appsportmate.common.modelos;

import java.io.Serializable;
import java.util.Objects;

/** Catálogo: Provincia (hija de Comunidad) */
public class Provincia implements Serializable {
    private String id;           // p.ej. "sevilla"
    private String nombre;       // p.ej. "Sevilla"
    private String comunidadId;  // p.ej. "es_andalucia"

    // Constructor vacío requerido por Firestore
    public Provincia() {}

    public Provincia(String id, String nombre, String comunidadId) {
        this.id = id;
        this.nombre = nombre;
        this.comunidadId = comunidadId;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getComunidadId() { return comunidadId; }
    public void setComunidadId(String comunidadId) { this.comunidadId = comunidadId; }

    @Override public String toString() {
        return nombre != null && !nombre.isEmpty() ? nombre : "(Sin nombre)";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Provincia)) return false;
        Provincia that = (Provincia) o;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
