package com.example.supermercadov2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//Esta clase es la que recibe la llamada de la alarma cuando se activa
//Llama al método enviarMensaje()
public class AlarmReceiver extends BroadcastReceiver {

    //El método onReceive se encarga de tratar la señal de alarma
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper enviarMensajesFCM = new DatabaseHelper(context);
        enviarMensajesFCM.enviarMensaje();
        Log.d("AlarmReceiver", "Alarma activada");
    }
}

