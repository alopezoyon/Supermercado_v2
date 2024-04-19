package com.example.supermercadov2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Aquí puedes llamar a la clase EnviarMensajesFCM para enviar el mensaje
        EnviarMensajesFCM enviarMensajesFCM = new EnviarMensajesFCM();
        enviarMensajesFCM.enviarMensaje();
    }
}

