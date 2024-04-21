package com.example.supermercadov2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

//Esta clase es un intermerdiario entre las llamadas de la aplicación a la base de datos remota.
//En este caso se encarga de enviar y recibir la petición para mostrar las imágenes guaradadas en la bd
public class conexionBDImagenes extends Worker {

    public conexionBDImagenes(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        HttpURLConnection urlConnection = null;
        try {
            //Obtener la dirección del servidor desde la entrada de datos
            String direccion = getInputData().getString("direccion");

            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            //Configurar la solicitud HTTP POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            Log.d("conexionBD","La url es: " + urlConnection);

            //Escribir datos en la conexión
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(getInputData().getString("datos"));
            out.close();
            Log.d("conexionBD", "Datos escritos en el flujo: " + getInputData().getString("datos"));

            //Leer la respuesta del servidor, que es una imagen siempre en esta clase
            Bitmap elBitmap= BitmapFactory.decodeStream(urlConnection.getInputStream());
            Log.d("conexionBD","Esta es la respuesta del server: " + elBitmap);

            //Convertir el Bitmap a byte[]
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            elBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            //Devolver el resultado como datos de salida
            Data outputData = new Data.Builder().putByteArray("imagen", byteArray).build();
            Log.d("conexionBD","Esta es la respuesta de salida: " + outputData);
            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}

