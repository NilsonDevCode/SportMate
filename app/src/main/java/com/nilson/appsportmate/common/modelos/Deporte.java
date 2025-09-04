package com.nilson.appsportmate.common.modelos;

public class Deporte {
    private String id;
    private String ayuntamiento_id;
    private String nombre;
    private String fecha;
    private String hora;
    private int max_personas;
    private int apuntados; // contador de inscritos
    private String ayuntamientoId;
    public Deporte() {}

    public Deporte(String id, String ayuntamiento_id, String nombre, String fecha, String hora, int max_personas, int apuntados) {
        this.id = id;
        this.ayuntamiento_id = ayuntamiento_id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.hora = hora;
        this.max_personas = max_personas;
        this.apuntados = apuntados;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAyuntamiento_id() { return ayuntamiento_id; }
    public void setAyuntamiento_id(String ayuntamiento_id) { this.ayuntamiento_id = ayuntamiento_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public int getMax_personas() { return max_personas; }
    public void setMax_personas(int max_personas) { this.max_personas = max_personas; }

    public int getApuntados() { return apuntados; }
    public void setApuntados(int apuntados) { this.apuntados = apuntados; }

    public String getAyuntamientoId() {
        return ayuntamientoId;
    }

    public void setAyuntamientoId(String ayuntamientoId) {
        this.ayuntamientoId = ayuntamientoId;
    }
}
