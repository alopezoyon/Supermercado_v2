package com.example.supermercadov2;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class conexionBDWebService extends Worker {

    public conexionBDWebService(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        HttpURLConnection urlConnection = null;
        try {
            // Obtener la dirección del servidor desde la entrada de datos
            String direccion = getInputData().getString("direccion");

            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            // Configurar la solicitud HTTP POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            Log.d("conexionBD","La url es: " + urlConnection);

            // Escribir datos en la conexión
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(getInputData().getString("datos"));
            out.close();
            Log.d("conexionBD", "Datos escritos en el flujo: " + getInputData().getString("datos"));

            // Leer la respuesta del servidor
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            Log.d("conexionBD","Esta es la respuesta del server: " + response);
            bufferedReader.close();

            // Devolver el resultado como datos de salida
            String resultado = response.toString();
            Data outputData = new Data.Builder().putString("datos", resultado).build();
            Log.d("conexionBD","Esta es la respuesta como salida: " + outputData);
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

