package com.example.supermercadov2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Esta clase es un adaptador para un RecyclerView, que se utiliza para mostrar la lista de productos en la interfaz.
//También se implementa un dialog para modificar el precio de un producto.
public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder> implements DatabaseHelper.GetNombreSupermercadoCallback, DatabaseHelper.ModificarPrecioCallback {

    private List<Producto> listaProductos;
    private Context context;
    private DatabaseHelper databaseHelper;

    public ProductosAdapter(List<Producto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_productos, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.txtNombre.setText(producto.getNombre());
        holder.txtPrecio.setText(String.valueOf(producto.getPrecio()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoModificarPrecio(producto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    @Override
    public void onPrecioModificado(boolean modificacionExitosa) {

    }

    @Override
    public void onNombreSupermercadoLoaded(String nombreSupermercado) {

    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView txtPrecio;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreProducto);
            txtPrecio = itemView.findViewById(R.id.txtPrecioProducto);
        }
    }

    // Método para abrir un diálogo para modificar el precio del producto
    private void abrirDialogoModificarPrecio(final Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.modify_precio);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_dialog_modificar_precio, null);
        final EditText editTextPrecio = view.findViewById(R.id.editTextPrecio);
        editTextPrecio.setText(String.valueOf(producto.getPrecio()));
        databaseHelper.obtenerNombreSupermercado(producto.getNombre(), new DatabaseHelper.GetNombreSupermercadoCallback() {
            @Override
            public void onNombreSupermercadoLoaded(String nombreSupermercado) {
                // Una vez que se carga el nombre del supermercado, podemos usarlo en la llamada a modificarPrecioProducto
                builder.setView(view);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double nuevoPrecio = Double.parseDouble(editTextPrecio.getText().toString());
                        producto.setPrecio(nuevoPrecio);
                        notifyDataSetChanged();

                        // Llamada para modificar el precio del producto con el nombre del supermercado obtenido
                        databaseHelper.modificarPrecioProducto(nombreSupermercado, producto.getNombre(), nuevoPrecio, new DatabaseHelper.ModificarPrecioCallback() {
                            @Override
                            public void onPrecioModificado(boolean modificacionExitosa) {
                                if (modificacionExitosa) {
                                    // La modificación del precio fue exitosa
                                    Toast.makeText(context, "¡Precio modificado correctamente!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // La modificación del precio falló
                                    Toast.makeText(context, "Error al modificar el precio.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);

                builder.show();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        builder.show();
    }
}