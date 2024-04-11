package com.example.supermercadov2;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

//Esta clase implementa la base de datos.
//Contiene los datos de registro (username, password, email, name y lastname) de los usuarios (se guardan en la tabla "users")
//Contiene los datos del supermercado (nombre_super, localizacion) en la tabla "supermercados"
//Contiene también los productos de cada supermercado en la tabla "productos_supermercado".
//Cada producto tiene su nombre y precio.

public class DatabaseHelper {
    private Context mContext;

    public DatabaseHelper(Context context) {
        mContext = context;
    }

    public interface LoginCallback {
        void onLoginResult(String result);
    }

    public void login(String username, String password, LoginCallback callback){

        AtomicReference<String> respuesta = new AtomicReference<>("fallo");
        // Crear un objeto JSONObject con los datos de inicio de sesión
        JSONObject postData = new JSONObject();
        try {
            postData.put("username", username);
            postData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/login.php";
        // Crear una solicitud de trabajo OneTimeWorkRequest
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                .setInputData(new Data.Builder()
                        .putString("direccion", serverAddress)
                        .putString("datos", postData.toString())
                        .build())
                .build();

        // Observar el estado de la solicitud de trabajo
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(otwr.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("datos");
                        // Verificar si el login fue exitoso
                        if (resultado.equals("Inicio de sesión exitoso. Bienvenido, " + username + "!")) {
                            respuesta.set("exito");
                        }
                        callback.onLoginResult(respuesta.get());
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    public void registrar(String name, String lastName, String email, String username, String password, RegistroCallback callback){

        // Crear un objeto JSONObject con los datos de registro
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
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(otwr.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("datos");
                        // Verificar si el usuario se registró exitosamente
                        if (!resultado.equals("El nombre de usuario " + username + " ya está en uso.")) {
                            // Llamar al método de callback con el resultado de éxito
                            callback.onRegistroSuccess();
                        } else {
                            // Llamar al método de callback con el resultado de fallo
                            callback.onRegistroFailed();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    // Interfaz de callback para manejar el resultado del registro
    public interface RegistroCallback {
        void onRegistroSuccess();
        void onRegistroFailed();
    }



    public void getSupermercados(String username, GetSupermercadosCallback callback){

        // Crear un objeto JSONObject con los datos de inicio de sesión
        JSONObject postData = new JSONObject();
        try {
            postData.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/mostrarSupermercados.php";
        // Crear una solicitud de trabajo OneTimeWorkRequest
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                .setInputData(new Data.Builder()
                        .putString("direccion", serverAddress)
                        .putString("datos", postData.toString())
                        .build())
                .build();

        // Observar el estado de la solicitud de trabajo
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(otwr.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("datos");
                        try {
                            JSONArray jsonArray = new JSONArray(resultado);
                            Log.d("","..." + jsonArray);
                            if (!jsonArray.getJSONObject(0).has("message")) {
                                List<Supermercado> supermercadoList = new ArrayList<>();

                                // Iterar a través del JSONArray para obtener cada objeto JSON
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    // Obtener el objeto JSON actual
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    // Obtener los datos del supermercado del objeto JSON
                                    String nombreSupermercado = jsonObject.getString("nombre_super");
                                    String localizacion = jsonObject.getString("localizacion");

                                    // Crear un objeto de supermercado con los datos obtenidos
                                    Supermercado supermercado = new Supermercado(nombreSupermercado, localizacion);

                                    Log.d("DatabaseHelper", "Datos super: " + nombreSupermercado + " " + localizacion);
                                    // Agregar el supermercado al ArrayList
                                    supermercadoList.add(supermercado);
                                }

                                // Llamar al método de callback cuando se haya terminado de agregar los supermercados
                                callback.onSupermercadosLoaded(supermercadoList);
                            } else {
                                // Si la respuesta indica que no hay supermercados, llamar al método de callback con una lista vacía
                                callback.onSupermercadosLoaded(new ArrayList<>());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }


    // Interfaz de callback para manejar la respuesta de obtener supermercados
    public interface GetSupermercadosCallback {
        void onSupermercadosLoaded(List<Supermercado> supermercadoList);
    }



    public void registrarSupermercado(String nombreSupermercado, String localizacion, String usernameRef, RegistroSupermercadoCallback callback){

        // Crear un objeto JSONObject con los datos del supermercado
        JSONObject postData = new JSONObject();
        try {
            postData.put("nombre_super", nombreSupermercado);
            postData.put("localizacion", localizacion);
            postData.put("username_ref", usernameRef);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/registrarSupermercado.php";

        // Crear una solicitud de trabajo OneTimeWorkRequest
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                .setInputData(new Data.Builder()
                        .putString("direccion", serverAddress)
                        .putString("datos", postData.toString())
                        .build())
                .build();

        // Observar el estado de la solicitud de trabajo
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(otwr.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("datos");
                        // Verificar si el supermercado se registró exitosamente
                        if (!resultado.equals("El supermercado '" + nombreSupermercado + "' ya está registrado.")) {
                            // Llamar al método de callback con el resultado de éxito
                            callback.onSupermercadoRegistrado();
                        } else {
                            // Llamar al método de callback con el resultado de fallo
                            callback.onRegistroSupermercadoFallido();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    // Interfaz de callback para manejar el resultado del registro de supermercado
    public interface RegistroSupermercadoCallback {
        void onSupermercadoRegistrado();
        void onRegistroSupermercadoFallido();
    }

    public void sendImageDataToRemoteDatabase(Bitmap imageData, String title) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageData.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] fototransformada = stream.toByteArray();
        String fotoen64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);

        // Crear un objeto JSONObject con los datos del supermercado
        JSONObject postData = new JSONObject();
        try {
            postData.put("title", title);
            postData.put("imageData", fotoen64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String serverAddress = "http://34.170.99.24:81/guardarImagen.php";

        // Crear una solicitud de trabajo OneTimeWorkRequest para enviar la imagen al servidor
        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                .setInputData(new Data.Builder()
                        .putString("direccion", serverAddress)
                        .putString("datos", postData.toString())
                        .build())
                .build();

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(uploadWorkRequest);
    }

    public void getImagenes(GetImagenesCallback callback) {
        String serverAddress = "http://34.170.99.24:81/mostrarImagenes.php";

        // Crear una solicitud de trabajo OneTimeWorkRequest para obtener las imágenes
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                .setInputData(new Data.Builder()
                        .putString("direccion", serverAddress)
                        .build())
                .build();

        // Observar el estado de la solicitud de trabajo
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(request.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Obtener el resultado de la solicitud de trabajo
                        String resultado = workInfo.getOutputData().getString("datos");

                        if (resultado != null) {
                            try {
                                // Convertir la respuesta JSON en un JSONArray
                                JSONArray jsonArray = new JSONArray(resultado);

                                // Crear una lista para almacenar las imágenes
                                List<String> imagenes = new ArrayList<>();

                                // Recorrer el JSONArray y agregar cada imagen a la lista
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    imagenes.add(jsonArray.getString(i));
                                }

                                // Llamar al método de callback con la lista de imágenes cargadas
                                callback.onImagenesLoaded(imagenes);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // Si hay un error al procesar la respuesta JSON, llamar al método de callback con una lista vacía
                                callback.onImagenesLoaded(new ArrayList<>());
                            }
                        } else {
                            // Si no hay resultado, llamar al método de callback con una lista vacía
                            callback.onImagenesLoaded(new ArrayList<>());
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(request);
    }

    public interface GetImagenesCallback {
        void onImagenesLoaded(List<String> imagenes);
    }


}