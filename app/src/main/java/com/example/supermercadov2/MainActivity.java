package com.example.supermercadov2;


import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Data;


//Esta clase implementa la pantalla de logIn.
//Se pide el usuario y contraseña.
//También puedes registrar un usuario nuevo.
//Hay un actionBar que posibilita ajustar las preferencias de color y de idioma.
//Hay tres intentos de logIn, aparece un diálogo indicando si has fallado y el número de intentos restantes.
//Si fallas 3 veces se bloquea los intentos durante 30 segundos.
public class MainActivity extends AppCompatActivity implements DatabaseHelper.LoginCallback{

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnRegister;
    private int loginAttempts = 3;
    private boolean preferencesLoaded = false;
    private Timer loginTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!preferencesLoaded) {
            loadPreferences();
            preferencesLoaded = true;
        }

        mostrarNotificacion();
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);


        edtUsername.setHint(getString(R.string.hint_username));
        edtPassword.setHint(getString(R.string.hint_password));
        btnLogin.setText(getString(R.string.btnLogin));
        btnRegister.setText(getString(R.string.btnRegister));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtUsername.getText().toString();
                String password = edtPassword.getText().toString();
                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.login(username,password, MainActivity.this);
            }

        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MenuRegistro.class);
                startActivity(intent);
            }
        });

        setSupportActionBar(findViewById(R.id.toolbar));
    }

    @Override
    public void onLoginResult(String result) {
        if (result.equals("exito")) {
            Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
            intent.putExtra("USERNAME_EXTRA", edtUsername.getText().toString());
            startActivity(intent);
            finish();
        } else if (result.equals("fallo")) {
            loginAttempts--;
            if (loginAttempts > 0) {
                Toast.makeText(MainActivity.this, "Login fallido. Intentos restantes: " + loginAttempts, Toast.LENGTH_SHORT).show();
            } else {
                blockLoginAttemptsFor30Seconds();
            }
        }
    }

    //Método para bloquear los intentos de logIn
    private void blockLoginAttemptsFor30Seconds() {
        if (loginTimer != null) {
            loginTimer.cancel();
        }

        loginTimer = new Timer();
        loginTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                resetLoginAttempts();
            }
        }, 30000);
    }

    //Método para resetear el número de logIn a 3
    private void resetLoginAttempts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginAttempts = 3;
            }
        });
    }

    /*
    //Añadir el menú que posibilita cambiar las preferencias de color e idioma
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_productos, menu);
        return true;
    }

    //Método que implementa las opciones de pulsar cada uno de los botones del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_change_language) {
            showLanguageDialog();
            return true;
        } else if (itemId == R.id.menu_change_color) {
            showColorDialog();
            return true;
        }
        else {
            return true;
        }
    }

    //Método que muestra el diálogo con los tres estilos posibles a elegir
    private void showColorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_color));

        String[] colorOptions = getResources().getStringArray(R.array.colorOptions);

        if (colorOptions != null && colorOptions.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, colorOptions);

            builder.setAdapter(adapter, (dialog, which) -> {
                int selectedColor = getColorForStyle(colorOptions[which]);
                changeBackgroundColor(selectedColor);
            });
        } else {
            builder.setMessage("No hay estilos disponibles.");
        }

        builder.create().show();
    }

    //Método para cambiar el color según la elección del estilo
    private int getColorForStyle(String style) {
        switch (style) {
            case "Estilo 1":
                return getResources().getColor(R.color.white);
            case "Estilo 2":
                return getResources().getColor(R.color.orange);
            case "Estilo 3":
                return getResources().getColor(R.color.yellow);
            default:
                return getResources().getColor(R.color.white);
        }
    }

     */

    //Método para cambiar el color del background establecido en preferencias
    private void changeBackgroundColor(int color) {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(color);
        saveColorPreference(color);
    }


    //Método para cargar las preferencias de color
    private void loadSavedColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedColor = preferences.getInt("color", 0);

        if (savedColor != 0) {
            changeBackgroundColor(savedColor);
        }
    }

    //Método para guardar las preferencias de color
    private void saveColorPreference(int color) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("color", color);
        editor.apply();
    }


    //Método que muestra el diálogo con los tres idiomas posibles a elegir
    private void showLanguageDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_language))
                .setItems(R.array.language_options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            setLocale("es");
                            break;
                        case 1:
                            setLocale("en");
                            break;
                        case 2:
                            setLocale("fr");
                            break;
                    }
                });

        builder.create().show();
    }

    //Método que muestra un diálogo con el número de intentos restantes en caso de fallo en el logIn
    private void showAttemptsDialog(int attempts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_failure));
        builder.setMessage(getString(R.string.dialog_message_invalid_credentials) + attempts);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //Método para guardar las preferencias de idioma
    private void saveLanguagePreference(String languageCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("language", languageCode);
        editor.apply();
    }

    //Método que sirve para cargar las preferencias de idioma y color
    private void loadPreferences() {
        loadSavedLanguage();
        loadSavedColor();
    }

    //Método para cargar las preferencias de idioma
    private void loadSavedLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedLanguage = preferences.getString("language", "");

        if (!savedLanguage.isEmpty()) {
            setLocale(savedLanguage);
        }
    }

    //Método para establecer el idioma de preferencia
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        saveLanguagePreference(languageCode);

        if (preferencesLoaded) {
            recreate();
        }
    }

    private void mostrarNotificacion() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }
    }
}