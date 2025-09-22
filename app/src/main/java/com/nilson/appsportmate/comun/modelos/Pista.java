package com.nilson.appsportmate.comun.modelos;

/**
 * Por si gestionas pistas/ubicaciones del ayuntamiento.
 * Colección sugerida: /ayuntamientos/{uid}/pistas/{pistaId}
 */
public class Pista {
    private String id;
    private String ayuntamientoId;
    private String nombre;
    private String direccion;
    private String descripcion;
    private Double lat; // opcional
    private Double lng; // opcional

    public Pista() { }

    public Pista(String id, String ayuntamientoId, String nombre,
                 String direccion, String descripcion, Double lat, Double lng) {
        this.id = id;
        this.ayuntamientoId = ayuntamientoId;
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAyuntamientoId() { return ayuntamientoId; }
    public void setAyuntamientoId(String ayuntamientoId) { this.ayuntamientoId = ayuntamientoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    @Override
    public String toString() {
        return "Pista{" +
                "id='" + id + '\'' +
                ", ayuntamientoId='" + ayuntamientoId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
