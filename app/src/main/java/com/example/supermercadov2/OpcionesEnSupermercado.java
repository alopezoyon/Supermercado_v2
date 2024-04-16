package com.example.supermercadov2;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OpcionesEnSupermercado extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_CAMERA = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opciones_en_supermercado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Agregar el clic del botón "Sacar foto"
        Button btnSacarFoto = findViewById(R.id.btnSacarFoto);
        btnSacarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara();
            }
        });

        Button btnMostrarImagenes = findViewById(R.id.btnMostrarFotos);
        btnMostrarImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarTitulosImagenesDesdeBDRemota();
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Opciones","Permisos concedidos");
            } else {
                // Permiso de cámara denegado, mostrar un mensaje o realizar otra acción
                Toast.makeText(this, "Se requiere permiso de cámara para usar esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Método para abrir la cámara y capturar una imagen
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(takePictureIntent);
    }

    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap laminiatura = (Bitmap) bundle.get("data");

                    File eldirectorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String nombrefichero = "IMG_" + timeStamp + "_";
                    File fichImg = null;
                    Uri uriimagen = null;
                    try {
                        fichImg = File.createTempFile(nombrefichero, ".jpg",eldirectorio);
                        uriimagen = FileProvider.getUriForFile(this, "com.example.supermercadov2.fileprovider", fichImg);

                        // Enviar la imagen a la base de datos remota
                        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
                        Log.d("MenuPrincipal","El título de la imagen es " + nombrefichero);
                        databaseHelper.sendImageDataToRemoteDatabase(laminiatura, nombrefichero, getIntent().getStringExtra("USERNAME_EXTRA"), getIntent().getStringExtra("NOMBRE_SUPERMERCADO"));
                    }
                    catch (Exception e) { e.printStackTrace(); }
                    Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    elIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriimagen);
                    startActivityForResult(elIntent);
                }
                else {
                    Log.d("TakenPicture", "No photo taken"); }
            });

    private void mostrarImagenesDesdeBDRemota(String titulo) {
        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
        databaseHelper.getImagenes(titulo, getIntent().getStringExtra("USERNAME_EXTRA"),
                getIntent().getStringExtra("NOMBRE_SUPERMERCADO"), new DatabaseHelper.GetImagenCallback() {
            @Override
            public void onImagenLoaded(Bitmap imagen) {
                // Verificar si se cargó la imagen
                if (imagen != null) {
                    // Iniciar la nueva actividad y pasar la imagen como un extra
                    Intent intent = new Intent(OpcionesEnSupermercado.this, MostrarImagenes.class);
                    intent.putExtra("IMAGEN_EXTRA", imagen);
                    startActivity(intent);
                } else {
                    // Manejar el caso en el que no se cargó la imagen
                    Toast.makeText(OpcionesEnSupermercado.this, "No se encontró la imagen.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void mostrarTitulosImagenesDesdeBDRemota() {
        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
        databaseHelper.getTitulosImagenes(getIntent().getStringExtra("USERNAME_EXTRA"), getIntent().getStringExtra("NOMBRE_SUPERMERCADO"), new DatabaseHelper.GetTitulosImagenesCallback() {
            @Override
            public void onTitulosImagenesLoaded(String[] titulosImagenes) {
                // Mostrar los títulos de imágenes en un diálogo y permitir al usuario elegir uno
                AlertDialog.Builder builder = new AlertDialog.Builder(OpcionesEnSupermercado.this);
                builder.setTitle("Títulos de imágenes disponibles");
                String[] titulosSeparados = new String[titulosImagenes.length];
                for (int i = 0; i < titulosImagenes.length; i++) {
                    titulosSeparados[i] = titulosImagenes[i] + "\n"; // Puedes añadir espacios u otros separadores si deseas
                    Log.d("Imágenes", titulosSeparados[i]);
                }
                builder.setItems(titulosSeparados, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // El usuario ha seleccionado un título de imagen, ahora puedes mostrar la imagen asociada
                        mostrarImagenesDesdeBDRemota(titulosSeparados[i]);
                    }
                });
                builder.show();
            }
        });
    }


    private void startActivityForResult(Intent elIntent) {
    }
}