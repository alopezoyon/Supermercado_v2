package com.example.supermercadov2;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {

    private static final String TAG = "ServicioFirebase";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Manejar el nuevo token aquí
        Log.d(TAG, "Nuevo token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            // Aquí puedes manejar los datos del mensaje
            // Por ejemplo, extraer el texto del mensaje y mostrarlo en la consola
            String mensaje = remoteMessage.getData().get("mensaje");
            Log.d(TAG, "Mensaje recibido: " + mensaje);

            // También puedes mostrar el mensaje en una notificación o en la segunda actividad si lo deseas
        }

        if (remoteMessage.getNotification() != null) {
            // Aquí puedes manejar la notificación
            // Por ejemplo, mostrarla en una notificación en la barra de estado
        }
    }
}


