package com.nilson.appsportmate.common.modelos;

import java.io.Serializable;
import java.util.Objects;

/** Catálogo: Ciudad (hija de Provincia y Comunidad) */
public class Ciudad implements Serializable {
    private String id;           // p.ej. "utrera"
    private String nombre;       // p.ej. "Utrera"
    private String comunidadId;  // p.ej. "es_andalucia"
    private String provinciaId;  // p.ej. "sevilla"

    // Constructor vacío requerido por Firestore
    public Ciudad() {}

    public Ciudad(String id, String nombre, String comunidadId, String provinciaId) {
        this.id = id;
        this.nombre = nombre;
        this.comunidadId = comunidadId;
        this.provinciaId = provinciaId;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getComunidadId() { return comunidadId; }
    public void setComunidadId(String comunidadId) { this.comunidadId = comunidadId; }

    public String getProvinciaId() { return provinciaId; }
    public void setProvinciaId(String provinciaId) { this.provinciaId = provinciaId; }

    @Override public String toString() {
        return nombre != null && !nombre.isEmpty() ? nombre : "(Sin nombre)";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciudad)) return false;
        Ciudad that = (Ciudad) o;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
