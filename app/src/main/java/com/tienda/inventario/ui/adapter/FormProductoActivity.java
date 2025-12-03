package com.tienda.inventario.ui.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.tienda.inventario.database.FirestoreManager;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;
import com.tienda.inventario.databinding.ActivityFormProductoBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FormProductoActivity extends AppCompatActivity {

    private static final String TAG = "FormProductoActivity";
    private ActivityFormProductoBinding binding;
    private FirestoreManager firestoreManager;

    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Proveedor> listaProveedores = new ArrayList<>();

    private int categoriaSeleccionadaId = -1;
    private int proveedorSeleccionadoId = -1;

    private String documentoId = null;
    private boolean esEdicion = false;
    private Producto productoActual = null;

    private Uri imagenSeleccionadaUri = null;
    private String imagenUrlFinal = null;

    // Launcher para seleccionar imagen de la galería
    private final ActivityResultLauncher<String> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenSeleccionadaUri = uri;
                    mostrarImagenSeleccionada(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestoreManager = FirestoreManager.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Verificar si es edición
        if (getIntent().hasExtra("DOC_ID")) {
            documentoId = getIntent().getStringExtra("DOC_ID");
            esEdicion = true;

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Producto");
            }

            Log.d(TAG, "Modo EDICIÓN - DocID: " + documentoId);
        } else {
            Log.d(TAG, "Modo AGREGAR nuevo producto");
        }

        cargarCategorias();
        cargarProveedores();
        setupListeners();
    }

    private void setupListeners() {
        binding.btnGuardar.setOnClickListener(v -> guardarProducto());
        binding.btnCancelar.setOnClickListener(v -> finish());

        // Vista previa de URL
        binding.etImagenUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                cargarVistaPreviaUrl();
            }
        });

        binding.btnVistaPrevia.setOnClickListener(v -> cargarVistaPreviaUrl());

        // Botón para seleccionar desde galería
        binding.btnSeleccionarGaleria.setOnClickListener(v -> abrirGaleria());

        // Botón para eliminar imagen seleccionada
        binding.btnEliminarImagen.setOnClickListener(v -> eliminarImagenSeleccionada());
    }

    /**
     * Abrir galería para seleccionar imagen
     */
    private void abrirGaleria() {
        seleccionarImagenLauncher.launch("image/*");
    }

    /**
     * Mostrar imagen seleccionada de la galería
     */
    private void mostrarImagenSeleccionada(Uri uri) {
        Glide.with(this)
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(binding.ivVistaPrevia);

        binding.ivVistaPrevia.setVisibility(android.view.View.VISIBLE);
        binding.btnEliminarImagen.setVisibility(android.view.View.VISIBLE);

        // Limpiar URL si había una
        binding.etImagenUrl.setText("");

        Toast.makeText(this, "✅ Imagen seleccionada", Toast.LENGTH_SHORT).show();
    }

    /**
     * Eliminar imagen seleccionada
     */
    private void eliminarImagenSeleccionada() {
        imagenSeleccionadaUri = null;
        imagenUrlFinal = null;
        binding.ivVistaPrevia.setVisibility(android.view.View.GONE);
        binding.btnEliminarImagen.setVisibility(android.view.View.GONE);
        binding.ivVistaPrevia.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.etImagenUrl.setText("");
        Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
    }

    /**
     * Cargar vista previa desde URL
     */
    private void cargarVistaPreviaUrl() {
        String url = binding.etImagenUrl.getText().toString().trim();
        if (!url.isEmpty()) {
            imagenSeleccionadaUri = null; // Limpiar selección de galería

            Glide.with(this)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(binding.ivVistaPrevia);

            binding.ivVistaPrevia.setVisibility(android.view.View.VISIBLE);
            binding.btnEliminarImagen.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void cargarCategorias() {
        firestoreManager.getCategorias(new FirestoreManager.OnCategoriasListener() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                listaCategorias = categorias;
                configurarSpinnerCategorias();

                if (esEdicion && productoActual == null) {
                    cargarProductoParaEdicion();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FormProductoActivity.this,
                        "Error al cargar categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerCategorias() {
        List<String> nombresCategoria = new ArrayList<>();
        for (Categoria cat : listaCategorias) {
            nombresCategoria.add(cat.getNombreCategoria());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresCategoria);

        binding.spinnerCategoria.setAdapter(adapter);
        binding.spinnerCategoria.setOnItemClickListener((parent, view, position, id) -> {
            categoriaSeleccionadaId = listaCategorias.get(position).getIdCategoria();
            Log.d(TAG, "Categoría seleccionada: " + listaCategorias.get(position).getNombreCategoria());
        });
    }

    private void cargarProveedores() {
        firestoreManager.getProveedores(new FirestoreManager.OnProveedoresListener() {
            @Override
            public void onSuccess(List<Proveedor> proveedores) {
                listaProveedores = proveedores;
                configurarSpinnerProveedores();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FormProductoActivity.this,
                        "Error al cargar proveedores: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerProveedores() {
        List<String> nombresProveedor = new ArrayList<>();
        for (Proveedor prov : listaProveedores) {
            nombresProveedor.add(prov.getNombreProveedor());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresProveedor);

        binding.spinnerProveedor.setAdapter(adapter);
        binding.spinnerProveedor.setOnItemClickListener((parent, view, position, id) -> {
            proveedorSeleccionadoId = listaProveedores.get(position).getIdProveedor();
            Log.d(TAG, "Proveedor seleccionado: " + listaProveedores.get(position).getNombreProveedor());
        });
    }

    private void cargarProductoParaEdicion() {
        if (documentoId == null) {
            Toast.makeText(this, "Error: ID de documento inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Cargando producto con DocID: " + documentoId);

        firestoreManager.getProductos(new FirestoreManager.OnProductosListener() {
            @Override
            public void onSuccess(List<Producto> productos) {
                for (Producto p : productos) {
                    if (documentoId.equals(p.getDocId())) {
                        productoActual = p;
                        mostrarDatosProducto(p);
                        Log.d(TAG, "✓ Producto cargado: " + p.getNombreProducto());
                        return;
                    }
                }

                Log.e(TAG, "❌ Producto no encontrado con DocID: " + documentoId);
                Toast.makeText(FormProductoActivity.this,
                        "Error: Producto no encontrado",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error al cargar producto: " + error);
                Toast.makeText(FormProductoActivity.this,
                        "Error: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarDatosProducto(Producto producto) {
        Log.d(TAG, "✓ Mostrando datos del producto: " + producto.getNombreProducto());

        binding.etNombre.setText(producto.getNombreProducto());
        binding.etDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "");
        binding.etPrecio.setText(String.valueOf(producto.getPrecioUnitario()));
        binding.etStockActual.setText(String.valueOf(producto.getStockActual()));
        binding.etStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        binding.etCodigoBarras.setText(producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "");

        // Mostrar imagen
        String imagenUrl = producto.getImagenUrl();
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            // Si es una ruta local (file://)
            if (imagenUrl.startsWith("file://")) {
                binding.etImagenUrl.setText("");
                Glide.with(this)
                        .load(new File(imagenUrl.replace("file://", "")))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivVistaPrevia);
                binding.ivVistaPrevia.setVisibility(android.view.View.VISIBLE);
                binding.btnEliminarImagen.setVisibility(android.view.View.VISIBLE);
            } else {
                // Si es una URL normal
                binding.etImagenUrl.setText(imagenUrl);
                cargarVistaPreviaUrl();
            }
        }

        // Seleccionar categoría
        categoriaSeleccionadaId = producto.getIdCategoria();
        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getIdCategoria() == categoriaSeleccionadaId) {
                binding.spinnerCategoria.setText(listaCategorias.get(i).getNombreCategoria(), false);
                break;
            }
        }

        // Seleccionar proveedor
        proveedorSeleccionadoId = producto.getIdProveedor();
        for (int i = 0; i < listaProveedores.size(); i++) {
            if (listaProveedores.get(i).getIdProveedor() == proveedorSeleccionadoId) {
                binding.spinnerProveedor.setText(listaProveedores.get(i).getNombreProveedor(), false);
                break;
            }
        }
    }

    /**
     * Guardar imagen localmente en el almacenamiento interno de la app
     */
    private String guardarImagenLocalmente(Uri uri) {
        try {
            // Crear directorio para imágenes
            File imagenesDir = new File(getFilesDir(), "imagenes_productos");
            if (!imagenesDir.exists()) {
                imagenesDir.mkdirs();
            }

            // Generar nombre único para la imagen
            String nombreArchivo = "producto_" + UUID.randomUUID().toString() + ".jpg";
            File archivoImagen = new File(imagenesDir, nombreArchivo);

            // Leer la imagen desde la URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Comprimir y guardar
            FileOutputStream outputStream = new FileOutputStream(archivoImagen);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            Log.d(TAG, "✅ Imagen guardada localmente: " + archivoImagen.getAbsolutePath());

            // Retornar ruta en formato file://
            return "file://" + archivoImagen.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al guardar imagen localmente: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Guardar producto
     */
    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }

        binding.btnGuardar.setEnabled(false);
        binding.btnGuardar.setText("Guardando...");

        // Procesar imagen
        if (imagenSeleccionadaUri != null) {
            // Guardar imagen de galería localmente
            imagenUrlFinal = guardarImagenLocalmente(imagenSeleccionadaUri);
            if (imagenUrlFinal == null) {
                Toast.makeText(this, "❌ Error al guardar imagen", Toast.LENGTH_SHORT).show();
                binding.btnGuardar.setEnabled(true);
                binding.btnGuardar.setText("Guardar");
                return;
            }
        } else {
            // Usar URL si existe
            imagenUrlFinal = binding.etImagenUrl.getText().toString().trim();
        }

        // Guardar producto en Firestore
        guardarProductoEnFirestore();
    }

    /**
     * Guardar producto en Firestore
     */
    private void guardarProductoEnFirestore() {
        Producto producto = new Producto();
        producto.setNombreProducto(binding.etNombre.getText().toString().trim());
        producto.setDescripcion(binding.etDescripcion.getText().toString().trim());
        producto.setPrecioUnitario(Double.parseDouble(binding.etPrecio.getText().toString().trim()));
        producto.setStockActual(Integer.parseInt(binding.etStockActual.getText().toString().trim()));
        producto.setStockMinimo(Integer.parseInt(binding.etStockMinimo.getText().toString().trim()));
        producto.setCodigoBarras(binding.etCodigoBarras.getText().toString().trim());
        producto.setIdCategoria(categoriaSeleccionadaId);
        producto.setIdProveedor(proveedorSeleccionadoId);
        producto.setImagenUrl(imagenUrlFinal != null ? imagenUrlFinal : "");

        if (esEdicion && documentoId != null) {
            // ACTUALIZAR
            Log.d(TAG, "Actualizando producto en Firestore...");

            firestoreManager.actualizarProducto(documentoId, producto,
                    new FirestoreManager.OnSuccessListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "✓ Producto actualizado");
                            Toast.makeText(FormProductoActivity.this,
                                    "✅ Producto actualizado",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error al actualizar: " + error);
                            Toast.makeText(FormProductoActivity.this,
                                    "❌ Error: " + error,
                                    Toast.LENGTH_SHORT).show();
                            binding.btnGuardar.setEnabled(true);
                            binding.btnGuardar.setText("Guardar");
                        }
                    });
        } else {
            // AGREGAR NUEVO
            Log.d(TAG, "Agregando nuevo producto...");

            firestoreManager.agregarProducto(producto, new FirestoreManager.OnSuccessListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✓ Producto agregado");
                    Toast.makeText(FormProductoActivity.this,
                            "✅ Producto guardado",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error al guardar: " + error);
                    Toast.makeText(FormProductoActivity.this,
                            "Error: " + error,
                            Toast.LENGTH_SHORT).show();
                    binding.btnGuardar.setEnabled(true);
                    binding.btnGuardar.setText("Guardar");
                }
            });
        }
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(binding.etNombre.getText())) {
            binding.etNombre.setError("Campo obligatorio");
            return false;
        }

        if (TextUtils.isEmpty(binding.etPrecio.getText())) {
            binding.etPrecio.setError("Campo obligatorio");
            return false;
        }

        try {
            double precio = Double.parseDouble(binding.etPrecio.getText().toString().trim());
            if (precio <= 0) {
                binding.etPrecio.setError("El precio debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etPrecio.setError("Precio inválido");
            return false;
        }

        if (TextUtils.isEmpty(binding.etStockActual.getText())) {
            binding.etStockActual.setError("Campo obligatorio");
            return false;
        }

        if (TextUtils.isEmpty(binding.etStockMinimo.getText())) {
            binding.etStockMinimo.setError("Campo obligatorio");
            return false;
        }

        if (categoriaSeleccionadaId == -1) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (proveedorSeleccionadoId == -1) {
            Toast.makeText(this, "Seleccione un proveedor", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}