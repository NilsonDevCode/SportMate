package com.nilson.appsportmate.common.modelos;

import java.io.Serializable;
import java.util.Objects;

/** Catálogo: Comunidad autónoma */
public class Comunidad implements Serializable {
    private String id;       // p.ej. "es_andalucia"
    private String nombre;   // p.ej. "Andalucía"

    // Constructor vacío requerido por Firestore
    public Comunidad() {}

    public Comunidad(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Para mostrar bonito en Spinners/Adapters
    @Override public String toString() {
        return nombre != null && !nombre.isEmpty() ? nombre : "(Sin nombre)";
    }

    // Igualdad por id
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comunidad)) return false;
        Comunidad that = (Comunidad) o;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
