package com.example.supermercadov2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EnviarMensajesFCM extends AppCompatActivity {

    EditText mensajeEditText;
    Button enviarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_mensajes_fcm);

        mensajeEditText = findViewById(R.id.mensaje_edit_text);
        enviarButton = findViewById(R.id.enviar_button);

        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje();
            }
        });
    }

    private void enviarMensaje() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // URL del script PHP para enviar mensajes FCM
                    URL url = new URL("http://34.170.99.24:81/enviarMensajes.php");

                    // Crear una conexión HTTP
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "key=AIzaSyAAnbdEBUuJto2LrcdTVffVBxo3KqaLXGM");
                    urlConnection.setDoOutput(true);

                    // Cuerpo del mensaje
                    String destino = DatabaseHelper.obtenerTokenAlmacenado(EnviarMensajesFCM.this);
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("to", destino);
                    JSONObject data = new JSONObject();
                    data.put("mensaje", mensajeEditText.getText().toString());
                    jsonMessage.put("data", data);

                    // Escribir los datos del mensaje en el cuerpo de la solicitud
                    OutputStream outputStream = urlConnection.getOutputStream();
                    outputStream.write(jsonMessage.toString().getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    // Obtener la respuesta del servidor
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Mensaje enviado correctamente
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("EnviarMensajesFCM","Se ha enviado el mensaje correctamente");
                                Toast.makeText(EnviarMensajesFCM.this, "Mensaje enviado correctamente", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Error al enviar el mensaje
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("EnviarMensajesFCM","Error al enviar el mensaje " + responseCode);
                                Toast.makeText(EnviarMensajesFCM.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // Cerrar la conexión
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
