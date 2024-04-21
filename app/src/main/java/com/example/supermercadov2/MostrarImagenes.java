package com.example.supermercadov2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

//Esta actividad muestra la imagen elegida
public class MostrarImagenes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_imagenes);

        //Obtener la imagen del intent
        Bitmap imagen = getIntent().getParcelableExtra("IMAGEN_EXTRA");

        //Mostrar la imagen en la actividad
        mostrarImagen(imagen);
    }

    //Método para mostrar la imagen en la actividad
    private void mostrarImagen(Bitmap imagen) {
        //Obtener el ImageView del layout
        ImageView imageView = findViewById(R.id.imageView);

        //Establecer la imagen en el ImageView
        imageView.setImageBitmap(imagen);
    }
}
