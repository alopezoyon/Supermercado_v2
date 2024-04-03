package com.example.supermercadov2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//Clase que implementa el Dialog que se abre al agregar un producto a un supermercado determinado.
//Hay que añadir un nombre y un precio.
//Hay dos botones: Agregar y Cancelar.
public class DialogAgregarProducto extends Dialog {

    private OnProductoAddedListener listener;

    public DialogAgregarProducto(Context context, ProductosSupermercadoActivity listener) {
        super(context);
        this.listener = listener;
    }

    //Método para crear el dialog con su botones y que implementa un listener para añadir el producto a la base de datos
    //en el caso de darle a "Agregar", si se da a "Cancelar" se pierde la información
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_agregar_producto);

        final EditText edtNombreProducto = findViewById(R.id.edtNombreProducto);
        final EditText edtPrecioProducto = findViewById(R.id.edtPrecioProducto);
        Button btnAgregar = findViewById(R.id.btnAgregar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreProducto = edtNombreProducto.getText().toString();
                double precioProducto = Double.parseDouble(edtPrecioProducto.getText().toString());

                if (listener != null) {
                    listener.onProductoAdded(nombreProducto, precioProducto);
                }

                dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    //Interfaz que implementa el método para añadir el producto a la bd
    public interface OnProductoAddedListener {
        void onProductoAdded(String nombre, double precio);
    }
}