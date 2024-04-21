package com.example.supermercadov2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

//Esta clase implementa el menú principal, donde se pueden ver los supermercados agregados
//y abrir la actividad de Google Maps con los marcadores de los supermercados guardados
public class MenuPrincipal extends AppCompatActivity implements DialogAgregarSupermercado.OnSupermercadoAddedListener,
        SupermercadosAdapter.OnSupermercadoClickListener, DatabaseHelper.GetSupermercadosCallback {

    private SupermercadosAdapter supermercadosAdapter;
    private List<Supermercado> listaSupermercados = new ArrayList<>();

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

        btnAgregarSupermercado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAgregarSupermercado dialog = new DialogAgregarSupermercado(MenuPrincipal.this, MenuPrincipal.this, username);
                dialog.show();
            }
        });

        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnAbrirMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirMapaConMarcadores();
            }
        });


    }

    //Método que llama a la actividad de MapsActivity para abrir el mapa con los marcadores
    private void abrirMapaConMarcadores() {
        ArrayList<String> localizaciones = obtenerLocalizaciones(listaSupermercados);
        Intent intent = new Intent(MenuPrincipal.this, MapsActivity.class);
        intent.putStringArrayListExtra("LOCALIZACIONES_EXTRA", localizaciones);
        startActivity(intent);
    }

    //Método para obtener solo las localizaciones de los supermercados de la lista
    private ArrayList<String> obtenerLocalizaciones(List<Supermercado> supermercados) {
        ArrayList<String> localizaciones = new ArrayList<>();
        for (Supermercado supermercado : supermercados) {
            localizaciones.add(supermercado.getLocalizacion());
        }
        return localizaciones;
    }


    //Método para cargar los supermercados guardados en la base de datos
    private void cargarSupermercadosDesdeDB(String username) {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
        databaseHelper.getSupermercados(username, new DatabaseHelper.GetSupermercadosCallback() {
            @Override
            public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
                //Cuando se carguen los supermercados, actualizar la lista y notificar al adaptador
                listaSupermercados.clear();
                listaSupermercados.addAll(supermercadoList);
                supermercadosAdapter.notifyDataSetChanged();
            }
        });
    }

    //Métodos para manejar la interacción con el usuario al agregar un supermercado
    @Override
    public void onSupermercadoAdded(String nombre, String localizacion, String username) {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuPrincipal.this);
        //Proceder con el registro del supermercado
        databaseHelper.registrarSupermercado(nombre, localizacion, username, new DatabaseHelper.RegistroSupermercadoCallback() {
            @Override
            public void onSupermercadoRegistrado() {
                // Supermercado registrado exitosamente
                cargarSupermercadosDesdeDB(username);
            }

            @Override
            public void onRegistroSupermercadoFallido() {
                //Error al registrar el supermercado
                Toast.makeText(MenuPrincipal.this,getString(R.string.supermarket_exists), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Métodos para manejar la carga de supermercados desde la base de datos
    @Override
    public void onSupermercadosLoaded(List<Supermercado> supermercadoList) {
    }


    //Método que dirige a OpcionesSupermercado, que ofrece las opciones de uso del supermercado elegido
    @Override
    public void onSupermercadoClick(int position, Supermercado supermercado) {
        Intent intent = new Intent(MenuPrincipal.this, OpcionesEnSupermercado.class);
        intent.putExtra("NOMBRE_SUPERMERCADO", supermercado.getNombre());
        intent.putExtra("USERNAME_EXTRA", getIntent().getStringExtra("USERNAME_EXTRA"));
        intent.putExtra("LOCALIZACION_SUPERMERCADO", supermercado.getLocalizacion());
        startActivity(intent);
    }
}