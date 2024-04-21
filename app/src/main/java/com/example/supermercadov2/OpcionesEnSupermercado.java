package com.example.supermercadov2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//Esta clase muestra las opciones disponibles para cada supermercado
public class OpcionesEnSupermercado extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones_en_supermercado);

        //Agregar el clic del botón "Sacar foto"
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

        Button btnIr = findViewById(R.id.btnCalculateDistance);
        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGoogleMapsForSupermarket(getIntent().getStringExtra("LOCALIZACION_SUPERMERCADO"));
            }
        });
    }

    //Método para abrir Google Maps con la localización guardada
    private void openGoogleMapsForSupermarket(String localizacionSupermercado) {
        try {
            //Obtener la ubicación actual del usuario
            FusedLocationProviderClient proveedorDeUbicacion = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return;
            }
            proveedorDeUbicacion.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        //Ubicación actual obtenida con éxito
                        double latitud = location.getLatitude();
                        double longitud = location.getLongitude();
                        //Crear la URI para la ubicación del supermercado
                        String supermercadoUri = Uri.encode(localizacionSupermercado);
                        //Crear la URI para la navegación desde la ubicación actual hasta el supermercado
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + supermercadoUri + "&mode=d");
                        //Crear un Intent para abrir Google Maps con la ruta desde la ubicación actual hasta el supermercado
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        //Comprobar si hay una aplicación de mapas disponible para manejar el Intent
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            //Si no hay una aplicación de mapas disponible, mostrar un mensaje de error
                            Toast.makeText(OpcionesEnSupermercado.this, "No hay aplicación de mapas disponible", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //No se pudo obtener la ubicación actual, mostrar un mensaje de error
                        Toast.makeText(OpcionesEnSupermercado.this, "No se puede obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Ocurrió un error al obtener la ubicación actual, mostrar un mensaje de error
                    e.printStackTrace();
                    Toast.makeText(OpcionesEnSupermercado.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            //Ocurrió un error, mostrar un mensaje de error
            e.printStackTrace();
            Toast.makeText(OpcionesEnSupermercado.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }


    //Método para abrir la cámara y capturar una imagen
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(takePictureIntent);
    }

    //Método que maneja la imagen que se ha sacado
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
                        fichImg = File.createTempFile(nombrefichero, ".jpg", eldirectorio);
                        uriimagen = FileProvider.getUriForFile(this, "com.example.supermercadov2.fileprovider", fichImg);
                        //Enviar la imagen a la base de datos remota
                        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
                        Log.d("MenuPrincipal", "El título de la imagen es " + nombrefichero);
                        databaseHelper.sendImageDataToRemoteDatabase(laminiatura, nombrefichero, getIntent().getStringExtra("USERNAME_EXTRA"), getIntent().getStringExtra("NOMBRE_SUPERMERCADO"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    elIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriimagen);
                    startActivityForResult(elIntent);
                } else {
                    Log.d("TakenPicture", "No photo taken");
                }
            });

    //Método que llama a la bd para obtener las imágenes que se han guardado
    private void mostrarImagenesDesdeBDRemota(String titulo) {
        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
        databaseHelper.getImagenes(titulo, getIntent().getStringExtra("USERNAME_EXTRA"),
                getIntent().getStringExtra("NOMBRE_SUPERMERCADO"), new DatabaseHelper.GetImagenCallback() {
                    @Override
                    public void onImagenLoaded(Bitmap imagen) {
                        //Verificar si se cargó la imagen
                        if (imagen != null) {
                            //Iniciar la nueva actividad y pasar la imagen como un extra
                            Intent intent = new Intent(OpcionesEnSupermercado.this, MostrarImagenes.class);
                            intent.putExtra("IMAGEN_EXTRA", imagen);
                            startActivity(intent);
                        } else {
                            //Manejar el caso en el que no se cargó la imagen
                            Toast.makeText(OpcionesEnSupermercado.this, "No se encontró la imagen.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Método utilizado para llamar a la bd y obtener los títulos de las imágenes guardadas
    private void mostrarTitulosImagenesDesdeBDRemota() {
        DatabaseHelper databaseHelper = new DatabaseHelper(OpcionesEnSupermercado.this);
        databaseHelper.getTitulosImagenes(getIntent().getStringExtra("USERNAME_EXTRA"), getIntent().getStringExtra("NOMBRE_SUPERMERCADO"), new DatabaseHelper.GetTitulosImagenesCallback() {
            @Override
            public void onTitulosImagenesLoaded(String[] titulosImagenes) {
                //Mostrar los títulos de imágenes en un diálogo y permitir al usuario elegir uno
                AlertDialog.Builder builder = new AlertDialog.Builder(OpcionesEnSupermercado.this);
                builder.setTitle("Títulos de imágenes disponibles");
                String[] titulosSeparados = new String[titulosImagenes.length];
                for (int i = 0; i < titulosImagenes.length; i++) {
                    titulosSeparados[i] = titulosImagenes[i] + "\n";
                    Log.d("Imágenes", titulosSeparados[i]);
                }
                builder.setItems(titulosSeparados, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //El usuario ha seleccionado un título de imagen, ahora puedes mostrar la imagen asociada
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