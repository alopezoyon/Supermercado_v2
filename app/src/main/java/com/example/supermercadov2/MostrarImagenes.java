package com.example.supermercadov2;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class MostrarImagenes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_imagenes);

        // Obtener la lista de imágenes del intent
        List<String> imagenes = getIntent().getStringArrayListExtra("IMAGENES_EXTRA");

        // Mostrar las imágenes en la actividad
        mostrarImagenes(imagenes);
    }

    // Método para mostrar las imágenes en la actividad
    private void mostrarImagenes(List<String> imagenes) {
        for (String url : imagenes) {
            // Crear un ImageView dinámicamente para cada imagen y mostrarla usando Picasso o Glide
            ImageView imageView = new ImageView(this);
            Picasso.get().load(url).into(imageView);
            // Agregar el ImageView al layout principal
            setContentView(imageView);
        }
    }
}
