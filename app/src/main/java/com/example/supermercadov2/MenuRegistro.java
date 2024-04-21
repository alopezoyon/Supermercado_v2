package com.example.supermercadov2;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

//Clase que implementa la pantalla de registro de un usuario
//Se piden los siguientes datos: nombre, apellidos, email, usuario y contraseña
public class MenuRegistro extends AppCompatActivity {

    private EditText edtName, edtLastName, edtEmail, edtUsername, edtPassword;
    private Button btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_registro);

        edtName = findViewById(R.id.edtName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtRegUsername);
        edtPassword = findViewById(R.id.edtRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        databaseHelper = new DatabaseHelper(this);

        edtName.setHint(getString(R.string.edtName));
        edtLastName.setHint(getString(R.string.edtLastName));
        edtEmail.setHint(getString(R.string.edtEmail));
        edtUsername.setHint(getString(R.string.hint_username));
        edtPassword.setHint(getString(R.string.hint_password));
        btnRegister.setText(getString(R.string.btnRegister));

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString();
                String lastName = edtLastName.getText().toString();
                String email = edtEmail.getText().toString();
                String username = edtUsername.getText().toString();
                String password = edtPassword.getText().toString();

                if (!name.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(MenuRegistro.this);
                    //Llamada a registrar pasando un objeto RegistroCallback
                    databaseHelper.registrar(name, lastName, email, username, password, new DatabaseHelper.RegistroCallback() {
                        @Override
                        public void onRegistroSuccess() {
                            Toast.makeText(MenuRegistro.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MenuRegistro.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onRegistroFailed() {
                            Toast.makeText(MenuRegistro.this, getString(R.string.username_exist), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MenuRegistro.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}