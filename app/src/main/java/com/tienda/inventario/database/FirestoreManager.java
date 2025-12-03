package com.tienda.inventario.database;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager para Firestore - Compatible con la app de escritorio (Java Swing)
 * Ambas apps comparten la misma estructura de datos en Firestore
 */
public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    // Nombres de colecciones (deben coincidir con desktop)
    private static final String COLLECTION_PRODUCTOS = "productos";
    private static final String COLLECTION_CATEGORIAS = "categorias";
    private static final String COLLECTION_PROVEEDORES = "proveedores";

    private FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // ==================== PRODUCTOS ====================

    public interface OnProductosListener {
        void onSuccess(List<Producto> productos);
        void onError(String error);
    }

    /**
     * Obtener todos los productos activos
     * Compatible con estructura de desktop
     */
    public void getProductos(OnProductosListener listener) {
        db.collection(COLLECTION_PRODUCTOS)
                .whereEqualTo("activo", true)
                .orderBy("nombre_producto")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Producto> productos = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Producto producto = documentToProducto(doc);
                            if (producto != null) {
                                productos.add(producto);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar producto: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "✓ Productos cargados: " + productos.size());
                    listener.onSuccess(productos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al cargar productos: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Agregar nuevo producto
     * Usa la misma estructura que desktop
     */
    public void agregarProducto(Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();

        // Usar los mismos nombres de campos que desktop
        data.put("nombre_producto", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
        data.put("precio_unitario", producto.getPrecioUnitario());
        data.put("stock_actual", producto.getStockActual());
        data.put("stock_minimo", producto.getStockMinimo());
        data.put("codigo_barras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "");
        data.put("id_categoria", producto.getIdCategoria());
        data.put("id_proveedor", producto.getIdProveedor());
        data.put("activo", true);
        data.put("fecha_registro", com.google.firebase.Timestamp.now());
        data.put("ultima_actualizacion", com.google.firebase.Timestamp.now());

        // NUEVO: Agregar URL de imagen
        data.put("imagen_url", producto.getImagenUrl() != null ? producto.getImagenUrl() : "");

        db.collection(COLLECTION_PRODUCTOS)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();

                    // Actualizar con id_producto para compatibilidad
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("firestore_id", docId);
                    updates.put("id_producto", docId.hashCode());

                    documentReference.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✓ Producto agregado: " + docId);
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al agregar producto: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Actualizar producto existente
     */
    public void actualizarProducto(String documentId, Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_producto", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
        data.put("precio_unitario", producto.getPrecioUnitario());
        data.put("stock_actual", producto.getStockActual());
        data.put("stock_minimo", producto.getStockMinimo());
        data.put("codigo_barras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "");
        data.put("id_categoria", producto.getIdCategoria());
        data.put("id_proveedor", producto.getIdProveedor());
        data.put("ultima_actualizacion", com.google.firebase.Timestamp.now());

        // NUEVO: Actualizar URL de imagen
        data.put("imagen_url", producto.getImagenUrl() != null ? producto.getImagenUrl() : "");

        db.collection(COLLECTION_PRODUCTOS)
                .document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Producto actualizado: " + documentId);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al actualizar: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Eliminar producto (soft delete - marca como inactivo)
     */
    public void eliminarProducto(String documentId, OnSuccessListener listener) {
        if (documentId == null || documentId.isEmpty()) {
            Log.e(TAG, "❌ Error: documentId es nulo o vacío");
            listener.onError("ID de documento inválido");
            return;
        }

        Log.d(TAG, "Eliminando producto con docId: " + documentId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("activo", false);
        updates.put("ultima_actualizacion", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_PRODUCTOS)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Producto marcado como inactivo: " + documentId);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al eliminar: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== CATEGORÍAS ====================

    public interface OnCategoriasListener {
        void onSuccess(List<Categoria> categorias);
        void onError(String error);
    }

    public void getCategorias(OnCategoriasListener listener) {
        db.collection(COLLECTION_CATEGORIAS)
                .orderBy("nombre_categoria")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Categoria> categorias = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Categoria categoria = documentToCategoria(doc);
                            if (categoria != null) {
                                categorias.add(categoria);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar categoría: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "✓ Categorías cargadas: " + categorias.size());
                    listener.onSuccess(categorias);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al cargar categorías: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void agregarCategoria(Categoria categoria, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_categoria", categoria.getNombreCategoria());
        data.put("descripcion", categoria.getDescripcion() != null ? categoria.getDescripcion() : "");
        data.put("fecha_creacion", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_CATEGORIAS)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Agregar id_categoria para compatibilidad
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("id_categoria", documentReference.getId().hashCode());
                    updates.put("firestore_id", documentReference.getId());

                    documentReference.update(updates)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== PROVEEDORES ====================

    public interface OnProveedoresListener {
        void onSuccess(List<Proveedor> proveedores);
        void onError(String error);
    }

    public void getProveedores(OnProveedoresListener listener) {
        db.collection(COLLECTION_PROVEEDORES)
                .orderBy("nombre_proveedor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Proveedor> proveedores = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Proveedor proveedor = documentToProveedor(doc);
                            if (proveedor != null) {
                                proveedores.add(proveedor);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar proveedor: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "✓ Proveedores cargados: " + proveedores.size());
                    listener.onSuccess(proveedores);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al cargar proveedores: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void agregarProveedor(Proveedor proveedor, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_proveedor", proveedor.getNombreProveedor());
        data.put("telefono", proveedor.getTelefono() != null ? proveedor.getTelefono() : "");
        data.put("email", proveedor.getEmail() != null ? proveedor.getEmail() : "");
        data.put("direccion", proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
        data.put("ciudad", proveedor.getCiudad() != null ? proveedor.getCiudad() : "");
        data.put("pais", proveedor.getPais() != null ? proveedor.getPais() : "");
        data.put("fecha_registro", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_PROVEEDORES)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Agregar id_proveedor para compatibilidad
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("id_proveedor", documentReference.getId().hashCode());
                    updates.put("firestore_id", documentReference.getId());

                    documentReference.update(updates)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== CONVERSORES ====================

    /**
     * Convertir documento de Firestore a objeto Producto
     * Compatible con estructura de desktop
     */
    private Producto documentToProducto(QueryDocumentSnapshot doc) {
        Producto producto = new Producto();

        // Guardar el ID del documento de Firestore
        producto.setDocId(doc.getId());

        // ID numérico (compatible con Room)
        Long idProductoLong = doc.getLong("id_producto");
        producto.setIdProducto(idProductoLong != null ? idProductoLong.intValue() : doc.getId().hashCode());

        // Datos del producto
        producto.setNombreProducto(doc.getString("nombre_producto"));
        producto.setDescripcion(doc.getString("descripcion"));

        // Precio (manejar Double y Long)
        Object precioObj = doc.get("precio_unitario");
        if (precioObj instanceof Double) {
            producto.setPrecioUnitario((Double) precioObj);
        } else if (precioObj instanceof Long) {
            producto.setPrecioUnitario(((Long) precioObj).doubleValue());
        }

        // Stock actual
        Long stockActual = doc.getLong("stock_actual");
        producto.setStockActual(stockActual != null ? stockActual.intValue() : 0);

        // Stock mínimo
        Long stockMinimo = doc.getLong("stock_minimo");
        producto.setStockMinimo(stockMinimo != null ? stockMinimo.intValue() : 0);

        // Código de barras
        producto.setCodigoBarras(doc.getString("codigo_barras"));

        // IDs de categoría y proveedor
        Long idCategoria = doc.getLong("id_categoria");
        producto.setIdCategoria(idCategoria != null ? idCategoria.intValue() : 0);

        Long idProveedor = doc.getLong("id_proveedor");
        producto.setIdProveedor(idProveedor != null ? idProveedor.intValue() : 0);

        // Estado activo
        Boolean activo = doc.getBoolean("activo");
        producto.setActivo(activo != null ? activo : true);

        // NUEVO: URL de imagen
        producto.setImagenUrl(doc.getString("imagen_url"));

        // Timestamps
        com.google.firebase.Timestamp fechaRegistro = doc.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            producto.setFechaRegistro(fechaRegistro.toDate().getTime());
        }

        com.google.firebase.Timestamp ultimaAct = doc.getTimestamp("ultima_actualizacion");
        if (ultimaAct != null) {
            producto.setUltimaActualizacion(ultimaAct.toDate().getTime());
        }

        return producto;
    }

    /**
     * Convertir documento a Categoria
     */
    private Categoria documentToCategoria(QueryDocumentSnapshot doc) {
        Categoria categoria = new Categoria();

        Long idCategoria = doc.getLong("id_categoria");
        categoria.setIdCategoria(idCategoria != null ? idCategoria.intValue() : doc.getId().hashCode());

        categoria.setNombreCategoria(doc.getString("nombre_categoria"));
        categoria.setDescripcion(doc.getString("descripcion"));

        com.google.firebase.Timestamp fechaCreacion = doc.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            categoria.setFechaCreacion(fechaCreacion.toDate().getTime());
        }

        return categoria;
    }

    /**
     * Convertir documento a Proveedor
     */
    private Proveedor documentToProveedor(QueryDocumentSnapshot doc) {
        Proveedor proveedor = new Proveedor();

        Long idProveedor = doc.getLong("id_proveedor");
        proveedor.setIdProveedor(idProveedor != null ? idProveedor.intValue() : doc.getId().hashCode());

        proveedor.setNombreProveedor(doc.getString("nombre_proveedor"));
        proveedor.setTelefono(doc.getString("telefono"));
        proveedor.setEmail(doc.getString("email"));
        proveedor.setDireccion(doc.getString("direccion"));
        proveedor.setCiudad(doc.getString("ciudad"));
        proveedor.setPais(doc.getString("pais"));

        com.google.firebase.Timestamp fechaRegistro = doc.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            proveedor.setFechaRegistro(fechaRegistro.toDate().getTime());
        }

        return proveedor;
    }

    // ==================== INTERFACES ====================

    public interface OnSuccessListener {
        void onSuccess();
        void onError(String error);
    }
}