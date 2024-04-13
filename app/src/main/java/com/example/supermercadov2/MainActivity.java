package com.example.supermercadov2;


import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
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

import androidx.core.content.ContextCompat;
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

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

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
}