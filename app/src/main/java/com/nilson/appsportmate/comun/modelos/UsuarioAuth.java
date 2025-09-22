package com.nilson.appsportmate.comun.modelos;

/** Documento en la colección "usuariosAuth" */
public class UsuarioAuth {
    private String uid;     // documentId en usuariosAuth
    private String alias;   // normalizado a lower-case
    private String rol;     // "usuario" | "ayuntamiento"
    private String email;   // opcional (si lo usas)

    public UsuarioAuth() { }

    public UsuarioAuth(String uid, String alias, String rol, String email) {
        this.uid = uid;
        this.alias = alias;
        this.rol = rol;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "UsuarioAuth{" +
                "uid='" + uid + '\'' +
                ", alias='" + alias + '\'' +
                ", rol='" + rol + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
