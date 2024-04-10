package com.example.supermercadov2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Esta clase implementa el menú donde añadir supermercados.
//Se muestra al principio un mensaje de bienvenida con el usuario que acaba de hacer logIn
public class MenuPrincipal extends AppCompatActivity implements DialogAgregarSupermercado.OnSupermercadoAddedListener,
        SupermercadosAdapter.OnSupermercadoClickListener, DatabaseHelper.GetSupermercadosCallback {

    private SupermercadosAdapter supermercadosAdapter;
    private List<Supermercado> listaSupermercados = new ArrayList<>();
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        loadPreferences();

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

    // Método para cambiar el color del background guardado en preferencias
    private void changeBackgroundColor(int color) {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(color);
    }

    // Método para cargar las preferencias de color
    private void loadSavedColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedColor = preferences.getInt("color", 0);

        if (savedColor != 0) {
            changeBackgroundColor(savedColor);
        }
    }

    // Método que sirve para cargar las preferencias de idioma y color
    private void loadPreferences() {
        loadSavedLanguage();
        loadSavedColor();
    }

    // Método para establecer el idioma de preferencia
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    // Método para cargar las preferencias de idioma
    private void loadSavedLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedLanguage = preferences.getString("language", "");

        if (!savedLanguage.isEmpty()) {
            setLocale(savedLanguage);
        }
    }

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

    @Override
    public void onSupermercadoClick(int position) {
    }

    @Override
    public void onSupermercadoClick(int position, Supermercado supermercado) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            byte[] imageData = bitmapToByteArray(imageBitmap);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
            databaseHelper.sendImageDataToRemoteDatabase(imageData, currentTime);
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
        // Implementación para cargar los supermercados desde la base de datos
    }
}
