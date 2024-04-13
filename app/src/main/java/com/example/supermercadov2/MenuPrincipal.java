package com.example.supermercadov2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuPrincipal extends AppCompatActivity implements DialogAgregarSupermercado.OnSupermercadoAddedListener,
        SupermercadosAdapter.OnSupermercadoClickListener, DatabaseHelper.GetSupermercadosCallback {

    private SupermercadosAdapter supermercadosAdapter;
    private List<Supermercado> listaSupermercados = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CAMERA = 1;

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

            Button btnMostrarImagenes = findViewById(R.id.btnMostrarFotos);
            btnMostrarImagenes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mostrarImagenesDesdeBDRemota();
                }
            });

            Button btnEnviarMensajes = findViewById(R.id.btnEnviarMensajes);
            btnEnviarMensajes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MenuPrincipal.this, EnviarMensajesFCM.class);
                    startActivity(intent);
                }
            });

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
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
                    //ImageView elImageView = findViewById(R.id.imgThumbnail);
                    //elImageView.setImageBitmap(laminiatura);
                    File eldirectorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String nombrefichero = "IMG_" + timeStamp + "_";
                    File fichImg = null;
                    Uri uriimagen = null;
                    try {
                        fichImg = File.createTempFile(nombrefichero, ".jpg",eldirectorio);
                        uriimagen = FileProvider.getUriForFile(this, "com.example.supermercadov2.fileprovider", fichImg);

                        // Enviar la imagen a la base de datos remota
                        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
                        Log.d("MenuPrincipal","El título de la imagen es " + nombrefichero);
                        databaseHelper.sendImageDataToRemoteDatabase(laminiatura, nombrefichero);
                    }
                    catch (Exception e) { e.printStackTrace(); }
                    Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    elIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriimagen);
                    startActivityForResult(elIntent);
                }
                else {
                    Log.d("TakenPicture", "No photo taken"); }
            });

    private void mostrarImagenesDesdeBDRemota() {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
        databaseHelper.getImagenes(new DatabaseHelper.GetImagenCallback() {
            @Override
            public void onImagenLoaded(Bitmap imagen) {
                // Verificar si se cargó la imagen
                if (imagen != null) {
                    // Iniciar la nueva actividad y pasar la imagen como un extra
                    Intent intent = new Intent(MenuPrincipal.this, MostrarImagenes.class);
                    intent.putExtra("IMAGEN_EXTRA", imagen);
                    startActivity(intent);
                } else {
                    // Manejar el caso en el que no se cargó la imagen
                    Toast.makeText(MenuPrincipal.this, "No se encontró la imagen.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void startActivityForResult(Intent elIntent) {
    }

    // Métodos para manejar la carga de supermercados desde la base de datos
    @Override
    public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
    }

    @Override
    public void onSupermercadoClick(int position) {
    }

    @Override
    public void onSupermercadoClick(int position, Supermercado supermercado) {
    }
}