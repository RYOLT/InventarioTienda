package com.tienda.inventario.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tienda.inventario.R;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.databinding.ActivityMainBinding;
import com.tienda.inventario.database.FirestoreManager;
import com.tienda.inventario.ui.adapter.FormProductoActivity;
import com.tienda.inventario.ui.adapter.ProductoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirestoreManager firestoreManager;
    private ActivityMainBinding binding;
    private ProductoAdapter adapter;

    private List<Producto> listaProductos = new ArrayList<>();
    private List<Producto> listaProductosOriginal = new ArrayList<>();
    private List<Categoria> listaCategorias = new ArrayList<>();

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "onCreate iniciado");

        firestoreManager = FirestoreManager.getInstance();
        setSupportActionBar(binding.toolbar);

        setupRecyclerView();
        setupListeners();

        cargarCategorias();
        cargarProductos();
    }

    private void setupRecyclerView() {
        adapter = new ProductoAdapter();
        binding.recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProductos.setAdapter(adapter);
        binding.recyclerViewProductos.setHasFixedSize(true);

        adapter.setOnProductoClickListener(new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                mostrarDetallesProducto(producto);
            }

            @Override
            public void onProductoLongClick(Producto producto) {
                mostrarOpcionesProducto(producto);
            }
        });
    }

    private void cargarCategorias() {
        firestoreManager.getCategorias(new FirestoreManager.OnCategoriasListener() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                listaCategorias = categorias;
                configurarSpinnerCategorias();
                Log.d(TAG, "✓ Categorías cargadas: " + categorias.size());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error al cargar categorías: " + error);
            }
        });
    }

    private void configurarSpinnerCategorias() {
        List<String> nombresCategoria = new ArrayList<>();
        nombresCategoria.add("-- Todas las categorías --");

        for (Categoria cat : listaCategorias) {
            nombresCategoria.add(cat.getNombreCategoria());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresCategoria);

        binding.spinnerFiltroCategoria.setAdapter(spinnerAdapter);
        binding.spinnerFiltroCategoria.setText(nombresCategoria.get(0), false);

        binding.spinnerFiltroCategoria.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                adapter.setProductos(listaProductosOriginal);
            } else {
                int idCategoria = listaCategorias.get(position - 1).getIdCategoria();
                filtrarPorCategoria(idCategoria);
            }
        });
    }

    private void cargarProductos() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestoreManager.getProductos(new FirestoreManager.OnProductosListener() {
            @Override
            public void onSuccess(List<Producto> productos) {
                binding.progressBar.setVisibility(View.GONE);

                listaProductos = productos;
                listaProductosOriginal = new ArrayList<>(productos);
                adapter.setProductos(productos);

                actualizarEstadisticas(productos);

                Log.d(TAG, "✓ Productos cargados: " + productos.size());

                if (productos.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "No hay productos. Agrega algunos desde el botón +",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "❌ Error al cargar productos: " + error);
                Toast.makeText(MainActivity.this,
                        "Error: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarEstadisticas(List<Producto> productos) {
        int total = productos.size();
        double valorTotal = 0;

        for (Producto p : productos) {
            valorTotal += p.getPrecioUnitario() * p.getStockActual();
        }

        binding.tvTotalProductos.setText(String.valueOf(total));
        binding.tvValorInventario.setText(
                String.format(Locale.getDefault(), "$%.2f", valorTotal)
        );
    }

    private void filtrarPorCategoria(int idCategoria) {
        List<Producto> productosFiltrados = new ArrayList<>();

        for (Producto p : listaProductosOriginal) {
            if (p.getIdCategoria() == idCategoria) {
                productosFiltrados.add(p);
            }
        }

        adapter.setProductos(productosFiltrados);

        if (productosFiltrados.isEmpty()) {
            Toast.makeText(this, "No hay productos en esta categoría", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, productosFiltrados.size() + " productos encontrados", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        binding.btnBuscar.setOnClickListener(v -> {
            String termino = binding.etBuscar.getText().toString().trim();
            if (!termino.isEmpty()) {
                buscarProductos(termino);
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMostrarTodos.setOnClickListener(v -> {
            binding.etBuscar.setText("");
            if (!listaCategorias.isEmpty()) {
                binding.spinnerFiltroCategoria.setText("-- Todas las categorías --", false);
            }
            adapter.setProductos(listaProductosOriginal);
        });

        binding.btnStockBajo.setOnClickListener(v -> mostrarStockBajo());

        binding.fabAgregarProducto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
            startActivity(intent);
        });

        binding.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (s.length() > 2) {
                        buscarProductos(s.toString());
                    } else if (s.length() == 0) {
                        adapter.setProductos(listaProductosOriginal);
                    }
                };

                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarProductos(String termino) {
        List<Producto> resultados = new ArrayList<>();
        String terminoLower = termino.toLowerCase();

        for (Producto p : listaProductosOriginal) {
            if (p.getNombreProducto().toLowerCase().contains(terminoLower)) {
                resultados.add(p);
            }
        }

        adapter.setProductos(resultados);

        if (resultados.isEmpty()) {
            Toast.makeText(this, "No se encontraron productos", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarStockBajo() {
        List<Producto> stockBajo = new ArrayList<>();

        for (Producto p : listaProductosOriginal) {
            if (p.isBajoStock()) {
                stockBajo.add(p);
            }
        }

        adapter.setProductos(stockBajo);

        if (stockBajo.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("✅ Stock OK")
                    .setMessage("No hay productos con stock bajo")
                    .setPositiveButton("Aceptar", null)
                    .show();
        } else {
            Toast.makeText(this,
                    "⚠️ " + stockBajo.size() + " productos con stock bajo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDetallesProducto(Producto producto) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Agregar ImageView para la imagen del producto
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                400,
                400
        );
        imageParams.gravity = android.view.Gravity.CENTER;
        imageParams.setMargins(0, 0, 0, 30);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Cargar imagen con Glide (soporta URLs y rutas locales)
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            String imagenUrl = producto.getImagenUrl();

            if (imagenUrl.startsWith("file://")) {
                // Es una ruta local
                File imagenFile = new File(imagenUrl.replace("file://", ""));
                Glide.with(this)
                        .load(imagenFile)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imageView);
            } else {
                // Es una URL normal
                Glide.with(this)
                        .load(imagenUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imageView);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        layout.addView(imageView);

        // Crear TextView para la información
        TextView textView = new TextView(this);
        textView.setTextSize(14);
        textView.setPadding(0, 20, 0, 0);

        String mensaje = "Nombre: " + producto.getNombreProducto() + "\n\n" +
                "Descripción: " + (producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción") + "\n\n" +
                "Precio: $" + String.format(Locale.getDefault(), "%.2f", producto.getPrecioUnitario()) + "\n\n" +
                "Stock: " + producto.getStockActual() + " (Min: " + producto.getStockMinimo() + ")\n\n" +
                "Código: " + (producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "N/A");

        textView.setText(mensaje);
        layout.addView(textView);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Detalles del Producto")
                .setView(layout)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Editar", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
                    intent.putExtra("DOC_ID", producto.getDocId());
                    startActivity(intent);
                })
                .show();
    }

    private void mostrarOpcionesProducto(Producto producto) {
        String[] opciones = {"Ver detalles", "Editar", "Actualizar stock", "Eliminar"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(producto.getNombreProducto())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mostrarDetallesProducto(producto);
                            break;
                        case 1:
                            Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
                            intent.putExtra("DOC_ID", producto.getDocId());
                            startActivity(intent);
                            break;
                        case 2:
                            mostrarDialogoActualizarStock(producto);
                            break;
                        case 3:
                            confirmarEliminarProducto(producto);
                            break;
                    }
                })
                .show();
    }

    private void mostrarDialogoActualizarStock(Producto producto) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nuevo stock");
        input.setText(String.valueOf(producto.getStockActual()));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 0, 50, 0);
        input.setLayoutParams(lp);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Actualizar Stock")
                .setMessage("Producto: " + producto.getNombreProducto() + "\nStock actual: " + producto.getStockActual())
                .setView(input)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    try {
                        int nuevoStock = Integer.parseInt(input.getText().toString());
                        if (nuevoStock >= 0) {
                            producto.setStockActual(nuevoStock);

                            firestoreManager.actualizarProducto(
                                    producto.getDocId(),
                                    producto,
                                    new FirestoreManager.OnSuccessListener() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(MainActivity.this,
                                                    "✅ Stock actualizado",
                                                    Toast.LENGTH_SHORT).show();
                                            cargarProductos();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(MainActivity.this,
                                                    "❌ Error: " + error,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        } else {
                            Toast.makeText(this, "❌ El stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Valor inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminarProducto(Producto producto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar '" + producto.getNombreProducto() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d(TAG, "Eliminando producto con DocID: " + producto.getDocId());

                    firestoreManager.eliminarProducto(producto.getDocId(),
                            new FirestoreManager.OnSuccessListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "✅ Producto eliminado de Firestore");
                                    Toast.makeText(MainActivity.this,
                                            "✅ Producto eliminado",
                                            Toast.LENGTH_SHORT).show();
                                    cargarProductos();
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "❌ Error al eliminar: " + error);
                                    Toast.makeText(MainActivity.this,
                                            "❌ Error: " + error,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            cargarProductos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}