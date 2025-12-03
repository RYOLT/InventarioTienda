package com.tienda.inventario.database.entities;

/**
 * Modelo de Producto - Solo POJO (sin Room)
 * Compatible con Firestore y la app de escritorio
 */
public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String descripcion;
    private double precioUnitario;
    private int stockActual;
    private int stockMinimo;
    private int idCategoria;
    private int idProveedor;
    private String codigoBarras;
    private long fechaRegistro;
    private long ultimaActualizacion;
    private boolean activo;

    // NUEVO: URL de la imagen del producto
    private String imagenUrl;

    // ID del documento de Firestore
    private String docId;

    // Constructor vacío (requerido por Firestore)
    public Producto() {
        this.activo = true;
        this.fechaRegistro = System.currentTimeMillis();
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    // Constructor con parámetros
    public Producto(String nombreProducto, String descripcion, double precioUnitario,
                    int stockActual, int stockMinimo, int idCategoria, int idProveedor,
                    String codigoBarras) {
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.idCategoria = idCategoria;
        this.idProveedor = idProveedor;
        this.codigoBarras = codigoBarras;
        this.activo = true;
        this.fechaRegistro = System.currentTimeMillis();
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public long getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(long ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    // NUEVO: Getter y Setter para imagenUrl
    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    // Método útil para verificar stock bajo
    public boolean isBajoStock() {
        return stockActual <= stockMinimo;
    }
}