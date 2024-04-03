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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Esta clase implementa la actividad de mostrar los productos de supermercado determinado.
//En esta actividad tenenemos la posibilidad de añadir un producto, incluyendo su nombre y precio.
//Además, podemos pulsar el botón "Ir", que abre Google Maps con la ubicación guardada en "localización" del supermercado
public class ProductosSupermercadoActivity extends AppCompatActivity implements DialogAgregarProducto.OnProductoAddedListener {
    private ProductosAdapter productosAdapter;
    private DatabaseHelper databaseHelper;
    private List<Producto> listaProductos;
    private String nombreSupermercado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos_supermercado);
        loadPreferences();

        nombreSupermercado = getIntent().getStringExtra("NOMBRE_SUPERMERCADO");
        String localizacionSupermercado = getIntent().getStringExtra("LOCALIZACION_SUPERMERCADO");

        TextView txtSupermercado = findViewById(R.id.txtSupermercado);
        txtSupermercado.setText(getString(R.string.producto_de) + " " + nombreSupermercado);

        databaseHelper = new DatabaseHelper(this);
        listaProductos = new ArrayList<>();

        cargarProductosDesdeDB(nombreSupermercado);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProductosSupermercado);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productosAdapter = new ProductosAdapter(listaProductos, this);
        recyclerView.setAdapter(productosAdapter);

        Button btnAddProduct = findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAgregarProducto dialog = new DialogAgregarProducto(ProductosSupermercadoActivity.this, ProductosSupermercadoActivity.this);
                dialog.show();
            }
        });

        Button btnIr = findViewById(R.id.btnCalculateDistance);
        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGoogleMapsForSupermarket(localizacionSupermercado);
            }
        });
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    //Método para abrir Google Maps con la localización guardada
    private void openGoogleMapsForSupermarket(String localizacionSupermercado) {
        try {
            String supermercadoUri = Uri.encode(localizacionSupermercado);
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + supermercadoUri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ProductosSupermercadoActivity.this, getString(R.string.error_maps), Toast.LENGTH_SHORT).show();
        }
    }

    //Método para crear el menú que posibilita guardar una nota relativa al supermercado
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nota, menu);
        return true;
    }

    //Método utilizado al pulsar el "agregar nota"
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_add_note) {
            mostrarDialogoAgregarNota();
            return true;
        }
        else {
            return true;
        }
    }

    //Método utilizado para guardar las notas en un archivo interno de la aplicación.
    //Crea un directorio por cada supermercado
    private void guardarNotaEnArchivo(String nota, String nombreSupermercado) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String folderName = "Notas_" + nombreSupermercado;
            String fileName = "nota_" + timeStamp + ".txt";

            File folder = new File(getFilesDir(), folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(nota);
            writer.close();

            mostrarNotificacion(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la nota en el archivo.", Toast.LENGTH_SHORT).show();
        }
    }

    //Método que muestra el diálogo para agregar una nota o ver las notas del supermercado
    private void mostrarDialogoAgregarNota() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.agregar_notas));
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nota = input.getText().toString();
                if (!nota.isEmpty()) {
                    guardarNotaEnArchivo(nota, nombreSupermercado);
                }
            }
        });

        builder.setNegativeButton(getString(R.string.see_notes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirRegistroNotas(nombreSupermercado);
            }
        });

        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //Método que sirve para abrir la actividad de ver notas
    private void abrirRegistroNotas(String nombreSupermercado) {
        Intent intent = new Intent(this, VerNotaActivity.class);
        intent.putExtra("NOMBRE_SUPERMERCADO", nombreSupermercado);
        startActivity(intent);
    }


    //Mismos métodos para cargar las preferencias

    private void loadPreferences() {
        loadSavedLanguage();
        loadSavedColor();
    }

    private void loadSavedLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedLanguage = preferences.getString("language", "");

        if (!savedLanguage.isEmpty()) {
            setLocale(savedLanguage);
        }
    }

    private void loadSavedColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedColor = preferences.getInt("color", 0);

        if (savedColor != 0) {
            changeBackgroundColor(savedColor);
        }
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

    }

    private void changeBackgroundColor(int color) {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(color);
    }


    //Método para cargar los productos del supermercado
    private void cargarProductosDesdeDB(String nombreSupermercado) {

        listaProductos.clear();
        listaProductos.addAll(databaseHelper.getProductosPorSupermercado(nombreSupermercado));

    }

    //Método utilizado en el caso de haber añadido un producto
    @Override
    public void onProductoAdded(String nombre, double precio) {
        if (!databaseHelper.productoExiste(nombreSupermercado,nombre)){
            databaseHelper.addProductoASupermercado(nombreSupermercado, nombre, precio);
            cargarProductosDesdeDB(nombreSupermercado);
            productosAdapter.notifyDataSetChanged();
        }
        else {
            Toast.makeText(this, getString(R.string.product_exists), Toast.LENGTH_SHORT).show();
        }
    }

    //Método que usa para mostrar una notificación en caso de haber guardado una nota
    private void mostrarNotificacion(String filename) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "IdCanal");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal= new NotificationChannel("IdCanal", "NombreCanal",
                    NotificationManager.IMPORTANCE_DEFAULT);
            elCanal.setDescription("Descripción del canal");
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.RED);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            elCanal.enableVibration(true);
            elManager.createNotificationChannel(elCanal);
        }

        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.message_alert))
                .setContentText(getString(R.string.noti_de) + " " + getString(R.string.app_name))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);

        Intent i = new Intent(this, VerNotaActivity.class);
        i.putExtra("NOMBRE_SUPERMERCADO", nombreSupermercado);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        elBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_foreground))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.message_alert))
                .setContentText(getString(R.string.noti_de) + " " + getString(R.string.app_name))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        elManager.notify(1, elBuilder.build());
    }
}