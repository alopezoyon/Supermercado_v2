package com.example.supermercadov2;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

//Clase que implementa el Dialog que se abre al agregar un supemercado.
//Hay que añadir un nombre y una localización.
//Hay dos botones: Agregar y Cancelar.
public class DialogAgregarSupermercado extends Dialog {

    private OnSupermercadoAddedListener listener;
    private String username;

    public DialogAgregarSupermercado(@NonNull Context context, OnSupermercadoAddedListener listener, String username) {
        super(context);
        this.listener = listener;
        this.username = username;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_agregar_supermercado);

        EditText edtNombre = findViewById(R.id.edtNombreSupermercado);
        EditText edtLocalizacion = findViewById(R.id.edtLocalizacionSupermercado);
        Button btnAgregar = findViewById(R.id.btnAgregarSupermercado);

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = edtNombre.getText().toString().trim();
                String localizacion = edtLocalizacion.getText().toString().trim();

                if (!nombre.isEmpty() && !localizacion.isEmpty()) {
                    //Pasar el nombre de usuario al método onSupermercadoAdded
                    listener.onSupermercadoAdded(nombre, localizacion, username);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.completar_campos, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface OnSupermercadoAddedListener {
        void onSupermercadoAdded(String nombre, String localizacion, String username);
    }
}