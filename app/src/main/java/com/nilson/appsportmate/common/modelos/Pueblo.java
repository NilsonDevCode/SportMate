package com.nilson.appsportmate.common.modelos;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Pueblo {
    private String id;
    private String nombre;
    private String ciudadId;
    private String provinciaId;
    private String comunidadId;
    private String createdByUid;
    @ServerTimestamp
    private Date createdAt;

    public Pueblo() { }

    public Pueblo(String id, String nombre, String ciudadId, String provinciaId, String comunidadId) {
        this.id = id;
        this.nombre = nombre;
        this.ciudadId = ciudadId;
        this.provinciaId = provinciaId;
        this.comunidadId = comunidadId;
    }

    // Getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudadId() { return ciudadId; }
    public void setCiudadId(String ciudadId) { this.ciudadId = ciudadId; }

    public String getProvinciaId() { return provinciaId; }
    public void setProvinciaId(String provinciaId) { this.provinciaId = provinciaId; }

    public String getComunidadId() { return comunidadId; }
    public void setComunidadId(String comunidadId) { this.comunidadId = comunidadId; }

    public String getCreatedByUid() { return createdByUid; }
    public void setCreatedByUid(String createdByUid) { this.createdByUid = createdByUid; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
