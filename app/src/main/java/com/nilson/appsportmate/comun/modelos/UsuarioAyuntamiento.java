package com.nilson.appsportmate.comun.modelos;

public class UsuarioAyuntamiento {

    private String id;              // Generado (UUID)
    private String nombre;
    private String apellidos;
    private String alias;
    private String comunidad;
    private String provincia;
    private String ciudad;
    private String pueblo;
    private String uid;             // UID de Firebase Auth
    private String ayuntamiento_id; // Vinculado desde el Spinner

    // Constructor vacío requerido por Firestore
    public UsuarioAyuntamiento() { }

    public UsuarioAyuntamiento(String id, String nombre, String apellidos, String alias,
                               String comunidad, String provincia, String ciudad, String pueblo,
                               String uid, String ayuntamiento_id) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.alias = alias;
        this.comunidad = comunidad;
        this.provincia = provincia;
        this.ciudad = ciudad;
        this.pueblo = pueblo;
        this.uid = uid;
        this.ayuntamiento_id = ayuntamiento_id;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getComunidad() { return comunidad; }
    public void setComunidad(String comunidad) { this.comunidad = comunidad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPueblo() { return pueblo; }
    public void setPueblo(String pueblo) { this.pueblo = pueblo; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getAyuntamiento_id() { return ayuntamiento_id; }
    public void setAyuntamiento_id(String ayuntamiento_id) { this.ayuntamiento_id = ayuntamiento_id; }

    @Override
    public String toString() {
        return "UsuarioAyuntamiento{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", alias='" + alias + '\'' +
                ", comunidad='" + comunidad + '\'' +
                ", provincia='" + provincia + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", pueblo='" + pueblo + '\'' +
                ", uid='" + uid + '\'' +
                ", ayuntamiento_id='" + ayuntamiento_id + '\'' +
                '}';
    }
}
