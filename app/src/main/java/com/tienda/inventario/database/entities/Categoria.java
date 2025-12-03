package com.tienda.inventario.database.entities;

public class Categoria {

    private int idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private long fechaCreacion;
    private String docId; // ID del documento en Firestore

    // Constructor vacío (requerido por Firestore)
    public Categoria() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Constructor con parámetros
    public Categoria(String nombreCategoria, String descripcion) {
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        return nombreCategoria; // Para mostrar en Spinners
    }
}