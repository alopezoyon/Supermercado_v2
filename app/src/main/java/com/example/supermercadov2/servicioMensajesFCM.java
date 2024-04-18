package com.example.supermercadov2;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class servicioMensajesFCM extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Log para verificar la recepción del mensaje
        Log.d("servicioMensajesFCM", "From: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene una carga útil de datos
        if (remoteMessage.getData().size() > 0) {
            Log.d("servicioMensajesFCM", "Message data payload: " + remoteMessage.getData());

            // Mostrar la notificación
            mostrarNotificacion(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }

        // Verificar si el mensaje contiene una carga útil de notificación
        if (remoteMessage.getNotification() != null) {
            Log.d("servicioMensajesFCM", "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Mostrar la notificación
            mostrarNotificacion(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void mostrarNotificacion(String title, String body) {
        // Crear un ID de canal único
        String channelId = "default_channel";

        // Crear un NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Verificar si el dispositivo tiene una versión de Android Oreo (API nivel 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Crear un canal de notificación
            NotificationChannel channel = new NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Default Notification Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(channel);
        }

        // Construir la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        // Mostrar la notificación
        notificationManager.notify(0, notificationBuilder.build());
    }
}
