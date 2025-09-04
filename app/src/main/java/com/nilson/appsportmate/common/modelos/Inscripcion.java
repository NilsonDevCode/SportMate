package com.nilson.appsportmate.common.modelos;

/**
 * En la subcolección:
 * /deportes_ayuntamiento/{aytoId}/lista/{deporteId}/inscritos/{uid}
 */
public class Inscripcion {
    private String uid;     // del usuario inscrito
    private String alias;   // alias del usuario
    private long ts;        // timestamp inscripción

    public Inscripcion() { }

    public Inscripcion(String uid, String alias, long ts) {
        this.uid = uid;
        this.alias = alias;
        this.ts = ts;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }

    @Override
    public String toString() {
        return "Inscripcion{" +
                "uid='" + uid + '\'' +
                ", alias='" + alias + '\'' +
                ", ts=" + ts +
                '}';
    }
}
