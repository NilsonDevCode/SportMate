package com.nilson.appsportmate.common.modelos;

public class Ayuntamiento {
    private String uid;        // = documentId en "ayuntamientos"
    private String nombre;
    private String numero;
    private String comunidad;
    private String provincia;
    private String ciudad;
    private String pueblo;
    private String localidad;

    // Constructor vacío requerido por Firebase
    public Ayuntamiento() { }

    public Ayuntamiento(String uid, String nombre, String numero, String comunidad,
                        String provincia, String ciudad, String pueblo, String localidad) {
        this.uid = uid;
        this.nombre = nombre;
        this.numero = numero;
        this.comunidad = comunidad;
        this.provincia = provincia;
        this.ciudad = ciudad;
        this.pueblo = pueblo;
        this.localidad = localidad;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComunidad() { return comunidad; }
    public void setComunidad(String comunidad) { this.comunidad = comunidad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPueblo() { return pueblo; }
    public void setPueblo(String pueblo) { this.pueblo = pueblo; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    @Override
    public String toString() {
        return "Ayuntamiento{" +
                "uid='" + uid + '\'' +
                ", nombre='" + nombre + '\'' +
                ", numero='" + numero + '\'' +
                ", comunidad='" + comunidad + '\'' +
                ", provincia='" + provincia + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", pueblo='" + pueblo + '\'' +
                ", localidad='" + localidad + '\'' +
                '}';
    }
}
