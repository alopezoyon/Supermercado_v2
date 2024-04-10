package com.example.supermercadov2;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuPrincipal extends AppCompatActivity implements DialogAgregarSupermercado.OnSupermercadoAddedListener,
        SupermercadosAdapter.OnSupermercadoClickListener, DatabaseHelper.GetSupermercadosCallback {

    private SupermercadosAdapter supermercadosAdapter;
    private String currentPhotoPath;
    private List<Supermercado> listaSupermercados = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        String username = getIntent().getStringExtra("USERNAME_EXTRA");

        TextView txtWelcome = findViewById(R.id.txtWelcome);

        if (txtWelcome != null) {
            txtWelcome.setText(getString(R.string.welcome_message) + username + "!");
        } else {
            Log.d("MenuPrincipal", "txtWelcome is null");
        }

        cargarSupermercadosDesdeDB(username);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewSupermercados);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        supermercadosAdapter = new SupermercadosAdapter(listaSupermercados, this);
        recyclerView.setAdapter(supermercadosAdapter);

        Button btnAgregarSupermercado = findViewById(R.id.btnAgregarSupermercado);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            btnAgregarSupermercado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogAgregarSupermercado dialog = new DialogAgregarSupermercado(MenuPrincipal.this, MenuPrincipal.this, username);
                    dialog.show();
                }
            });

            // Agregar el clic del botón "Sacar foto"
            Button btnSacarFoto = findViewById(R.id.btnSacarFoto);
            btnSacarFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    abrirCamara();
                }
            });

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
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
                Log.d("MenuPrincipal","Permisos concedidos");
            } else {
                // Permiso de cámara denegado, mostrar un mensaje o realizar otra acción
                Toast.makeText(this, "Se requiere permiso de cámara para usar esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para cargar los supermercados guardados en la base de datos
    private void cargarSupermercadosDesdeDB(String username) {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
        databaseHelper.getSupermercados(username, new DatabaseHelper.GetSupermercadosCallback() {
            @Override
            public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
                // Cuando se carguen los supermercados, actualiza la lista y notifica al adaptador
                listaSupermercados.clear();
                listaSupermercados.addAll(supermercadoList);
                supermercadosAdapter.notifyDataSetChanged();
            }
        });
    }

    // Métodos para manejar la interacción con el usuario al agregar un supermercado
    @Override
    public void onSupermercadoAdded(String nombre, String localizacion, String username) {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
        // Proceder con el registro del supermercado
        databaseHelper.registrarSupermercado(nombre, localizacion, username, new DatabaseHelper.RegistroSupermercadoCallback() {
            @Override
            public void onSupermercadoRegistrado() {
                // Supermercado registrado exitosamente
                cargarSupermercadosDesdeDB(username);
            }

            @Override
            public void onRegistroSupermercadoFallido() {
                // Error al registrar el supermercado
                Toast.makeText(MenuPrincipal.this,getString(R.string.supermarket_exists), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para manejar el resultado de la captura de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // La imagen se capturó correctamente, ahora puedes procesarla
            processCapturedImage();
        } else {
            // Error o cancelación en la captura de la imagen
            Toast.makeText(this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para procesar la imagen capturada
    private void processCapturedImage() {
        // Se obtiene la imagen capturada del archivo
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        if (imageBitmap != null) {
            // Convertir la imagen en un array de bytes
            byte[] imageData = bitmapToByteArray(imageBitmap);
            // Obtener la marca de tiempo
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            // Enviar la imagen a la base de datos remota
            DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
            databaseHelper.sendImageDataToRemoteDatabase(imageData, currentTime);
        } else {
            // La imagen no se pudo cargar correctamente
            Toast.makeText(this, "Error al cargar la imagen capturada", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para convertir un bitmap en un array de bytes
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // Método para abrir la cámara y capturar una imagen
    private void abrirCamara() {
        Log.d("MenuPrincipal", "Se intenta abrir la cámara");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Crear un archivo donde guardar la imagen
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("MenuPrincipal", "Error al crear el archivo de imagen: " + ex.getMessage());
                }
                // Continuar solo si el archivo se creó correctamente
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.supermercadov2.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    // Iniciar la actividad de la cámara y esperar el resultado
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        } catch (ActivityNotFoundException e) {
            Log.e("MenuPrincipal", "Error al lanzar la actividad de la cámara: " + e.getMessage());
        }
    }

    // Método para crear un archivo de imagen temporal
    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único basado en la fecha y hora actuales
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );

        // Guardar la ruta del archivo para usarla más tarde
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Métodos para manejar la carga de supermercados desde la base de datos
    @Override
    public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
        // Implementación para cargar los supermercados desde la base de datos
    }

    @Override
    public void onSupermercadoClick(int position) {
    }

    @Override
    public void onSupermercadoClick(int position, Supermercado supermercado) {
    }
}

