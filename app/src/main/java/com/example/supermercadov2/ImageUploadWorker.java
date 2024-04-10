package com.example.supermercadov2;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploadWorker extends Worker {
    public ImageUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String serverUrl = getInputData().getString("direccion");
        byte[] imageData = getInputData().getByteArray("imageData");
        String title = getInputData().getString("title");

        try {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Crear un objeto JSON para enviar todos los datos al servidor
            JSONObject postData = new JSONObject();
            postData.put("imagen", Base64.encodeToString(imageData, Base64.DEFAULT));
            postData.put("titulo", title);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postData.toString());
            writer.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // La imagen se cargó correctamente
                return Result.success();
            } else {
                // Hubo un error al cargar la imagen
                return Result.failure();
            }
        } catch (Exception e) {
            Log.e("ImageUploadWorker", "Error uploading image: " + e.getMessage());
            return Result.failure();
        }
    }

}

