package com.example.supermercadov2;

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
        loadPreferences();

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

                    // Crear un objeto JSONObject con los datos de inicio de sesión
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("name", name);
                        postData.put("lastName", lastName);
                        postData.put("email", email);
                        postData.put("username", username);
                        postData.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String serverAddress = "http://34.170.99.24:81/registro.php";
                    // Crear una solicitud de trabajo OneTimeWorkRequest
                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                            .setInputData(new Data.Builder()
                                    .putString("direccion", serverAddress)
                                    .putString("datos", postData.toString())
                                    .build())
                            .build();

                    // Observar el estado de la solicitud de trabajo
                    WorkManager.getInstance(MenuRegistro.this).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(MenuRegistro.this, workInfo -> {
                                if (workInfo != null && workInfo.getState().isFinished()) {
                                    String resultado = workInfo.getOutputData().getString("datos");
                                    // Aquí puedes manejar la respuesta del servidor
                                    Log.d("Resultado", resultado);
                                    // Verificar si el usuario existe
                                    if (!resultado.equals("El nombre de usuario " + username + " ya está en uso.")) {
                                        databaseHelper.addUser(username, password, email, name, lastName);
                                        Toast.makeText(MenuRegistro.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MenuRegistro.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(MenuRegistro.this, getString(R.string.username_exist), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    // Encolar la solicitud de trabajo
                    WorkManager.getInstance(MenuRegistro.this).enqueue(otwr);
                } else {
                    Toast.makeText(MenuRegistro.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //Método para cargar las preferencias de idioma y color
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

    //Método para cargar las preferencias de color
    private void loadSavedColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedColor = preferences.getInt("color", 0);

        if (savedColor != 0) {
            changeBackgroundColor(savedColor);
        }
    }

    //Método para establecer el idioma de preferencia
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

    }

    //Método para cambiar el color del background establecido en preferencias
    private void changeBackgroundColor(int color) {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(color);
    }
}