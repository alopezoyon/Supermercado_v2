package com.example.supermercadov2;


import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
//Hay tres intentos de logIn, aparece un diálogo indicando si has fallado y el número de intentos restantes.
//Si fallas 3 veces se bloquea los intentos durante 30 segundos.
//Se inicializa una alarma que envía un mensaje FCM cada minuto.
public class MainActivity extends AppCompatActivity implements DatabaseHelper.LoginCallback{

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnRegister;
    private int loginAttempts = 3;
    private boolean preferencesLoaded = false;
    private Timer loginTimer;
    private static final int REQUEST_CODE = 123;
    private static final long INTERVAL = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        programarAlarma(this);

        //Se verifica si tenemos los permisos de POST_NOTIFICATIONS
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

    //Método para programar la alarma
    public static void programarAlarma(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
        Toast.makeText(context, "Alarma programada para enviar el mensaje cada 5 minutos", Toast.LENGTH_SHORT).show();
    }

    //Método que maneja el resultado del login en la bd
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