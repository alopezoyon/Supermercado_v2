package com.example.supermercadov2;

import android.app.Application;
import android.app.NotificationChannel;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class obtenerTokenFirebase extends Application {
    public void onCreate() {
        super.onCreate();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    Log.d("obtenerTokenFirebase", token);
                    //Guardar el token en la bd
                }
                else {
                    return;
                }
            }
        });
    }
}