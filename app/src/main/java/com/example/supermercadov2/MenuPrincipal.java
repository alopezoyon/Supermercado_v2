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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Esta clase implementa el menú donde añadir supermercados.
//Se muestra al principio un mensaje de bienvenida con el usuario que acaba de hacer logIn
public  class MenuPrincipal extends AppCompatActivity implements
        DialogAgregarSupermercado.OnSupermercadoAddedListener,
        SupermercadosAdapter.OnSupermercadoClickListener,
        ProductosFragment.listenerDelFragment, DatabaseHelper.GetSupermercadosCallback {

    private SupermercadosAdapter supermercadosAdapter;
    private List<Supermercado> listaSupermercados = new ArrayList<>();

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


    //Método para cambiar el color del background guardado en preferencias
    private void changeBackgroundColor(int color) {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(color);
    }


    //Método para cargar las preferencias de color
    private void loadSavedColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedColor = preferences.getInt("color", 0);

        if (savedColor != 0) {
            changeBackgroundColor(savedColor);
        }
    }

    //Método que sirve para cargar las preferencias de idioma y color
    private void loadPreferences() {
        loadSavedLanguage();
        loadSavedColor();
    }


    //Método para establecer el idioma de preferencia
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

    }

    //Método para cargar las preferencias de idioma
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

    //Método que implementa Fragments. En el caso de posición horizontal, si se pulsa un supermercado,
    //se muestran los productos de ese supermercado.
    //Sino vas al menú de productos del supermercado.
    @Override
    public void onSupermercadoClick(int position, Supermercado supermercado) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ProductosFragment productosFragment = new ProductosFragment();
            Bundle args = new Bundle();
            args.putString("nombreSupermercado", supermercado.getNombre());
            productosFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.productoContainer, productosFragment)
                    .commit();
        } else {
            Intent intent = new Intent(MenuPrincipal.this, ProductosSupermercadoActivity.class);
            intent.putExtra("NOMBRE_SUPERMERCADO", supermercado.getNombre());
            intent.putExtra("LOCALIZACION_SUPERMERCADO", supermercado.getLocalizacion());
            startActivity(intent);
        }
    }

    @Override
    public void seleccionarElemento(String elemento) {
    }

    @Override
    public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {

    }
}