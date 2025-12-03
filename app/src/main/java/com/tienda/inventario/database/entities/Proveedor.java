package com.tienda.inventario.database.entities;

/**
 * Modelo de Proveedor
 */
public class Proveedor {
    private int idProveedor;
    private String nombreProveedor;
    private String telefono;
    private String email;
    private String direccion;
    private String ciudad;
    private String pais;
    private long fechaRegistro;

    // Constructor vacío
    public Proveedor() {
        this.fechaRegistro = System.currentTimeMillis();
    }

    // Constructor con parámetros
    public Proveedor(String nombreProveedor, String telefono, String email,
                     String direccion, String ciudad, String pais) {
        this.nombreProveedor = nombreProveedor;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.pais = pais;
        this.fechaRegistro = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return nombreProveedor; // Para mostrar en Spinners
    }
}