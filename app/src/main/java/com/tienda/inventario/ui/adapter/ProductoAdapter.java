package com.tienda.inventario.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.tienda.inventario.R;
import com.tienda.inventario.database.entities.Producto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos = new ArrayList<>();
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onProductoLongClick(Producto producto);
    }

    public void setOnProductoClickListener(OnProductoClickListener listener) {
        this.listener = listener;
    }

    public void setProductos(List<Producto> productos) {
        this.listaProductos = productos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto, listener);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardProducto;
        private final ImageView ivImagenProducto;
        private final TextView tvNombreProducto;
        private final TextView tvPrecio;
        private final TextView tvDescripcion;
        private final TextView tvCategoria;
        private final TextView tvProveedor;
        private final TextView tvStock;
        private final TextView tvStockMinimo;
        private final TextView tvAlertaStock;
        private final TextView tvCodigoBarras;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            cardProducto = itemView.findViewById(R.id.cardProducto);
            ivImagenProducto = itemView.findViewById(R.id.ivImagenProducto);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvProveedor = itemView.findViewById(R.id.tvProveedor);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvStockMinimo = itemView.findViewById(R.id.tvStockMinimo);
            tvAlertaStock = itemView.findViewById(R.id.tvAlertaStock);
            tvCodigoBarras = itemView.findViewById(R.id.tvCodigoBarras);
        }

        public void bind(Producto producto, OnProductoClickListener listener) {
            // Cargar imagen con Glide (soporta URLs y rutas locales)
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
                String imagenUrl = producto.getImagenUrl();

                // Si es una ruta local (file://)
                if (imagenUrl.startsWith("file://")) {
                    File imagenFile = new File(imagenUrl.replace("file://", ""));
                    Glide.with(itemView.getContext())
                            .load(imagenFile)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(ivImagenProducto);
                } else {
                    // Si es una URL normal
                    Glide.with(itemView.getContext())
                            .load(imagenUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(ivImagenProducto);
                }
            } else {
                ivImagenProducto.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Datos básicos
            tvNombreProducto.setText(producto.getNombreProducto());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecioUnitario()));
            tvDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción");

            // Categoría y Proveedor
            tvCategoria.setText("📦 Cat. " + producto.getIdCategoria());
            tvProveedor.setText("🏢 Prov. " + producto.getIdProveedor());

            // Stock
            tvStock.setText(String.valueOf(producto.getStockActual()));
            tvStockMinimo.setText(String.format(Locale.getDefault(), "(Min: %d)", producto.getStockMinimo()));

            // Alerta de stock bajo
            if (producto.isBajoStock()) {
                tvAlertaStock.setVisibility(View.VISIBLE);
                tvStock.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvAlertaStock.setVisibility(View.GONE);
                tvStock.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
            }

            // Código de barras
            if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
                tvCodigoBarras.setText("Código: " + producto.getCodigoBarras());
                tvCodigoBarras.setVisibility(View.VISIBLE);
            } else {
                tvCodigoBarras.setVisibility(View.GONE);
            }

            // Click listeners
            cardProducto.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductoClick(producto);
                }
            });

            cardProducto.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProductoLongClick(producto);
                }
                return true;
            });
        }
    }
}