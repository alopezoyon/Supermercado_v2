package com.example.supermercadov2;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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



    public void getSupermercados(String username, List<Supermercado> supermercadoList, GetSupermercadosCallback callback){

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

        //Observar el estado de la solicitud de trabajo
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(otwr.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("datos");
                        try {
                            // Crear un JSONArray a partir del resultado JSON
                            JSONArray jsonArray = new JSONArray(resultado);

                            // Iterar a través del JSONArray para obtener cada objeto JSON
                            for (int i = 0; i < jsonArray.length(); i++) {
                                // Obtener el objeto JSON actual
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                // Obtener los datos del supermercado del objeto JSON
                                String nombreSupermercado = jsonObject.getString("nombre_super");
                                String localizacion = jsonObject.getString("localizacion");

                                // Llamada a obtenerProductosDelSupermercado con un callback para manejar la respuesta de manera asíncrona
                                obtenerProductosDelSupermercado(nombreSupermercado, productos -> {
                                    // Crear un objeto de supermercado con los datos obtenidos y la lista de productos
                                    Supermercado supermercado = new Supermercado(nombreSupermercado, localizacion, productos);

                                    // Agregar el supermercado al ArrayList
                                    supermercadoList.add(supermercado);

                                    // Llamar al método de callback cuando se haya terminado de agregar los supermercados
                                    callback.onSupermercadosLoaded(supermercadoList);
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    public void obtenerProductosDelSupermercado(String nombreSupermercado, GetProductosCallback callback){

        JSONObject postData = new JSONObject();
        try {
            postData.put("nombre_super", nombreSupermercado);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/obtenerProductosSupermercado.php";

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
                            // Crear un JSONArray a partir del resultado JSON
                            JSONArray jsonArray = new JSONArray(resultado);

                            // Crear una lista para almacenar los productos
                            List<Producto> listaProductos = new ArrayList<>();

                            // Iterar a través del JSONArray para obtener cada objeto JSON
                            for (int i = 0; i < jsonArray.length(); i++) {
                                // Obtener el objeto JSON actual
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                // Obtener los datos del producto del objeto JSON
                                String nombreProducto = jsonObject.getString("nombre_prod");
                                double precio = jsonObject.getDouble("precio");

                                // Crear un objeto de producto con los datos obtenidos
                                Producto producto = new Producto(nombreProducto, precio);

                                // Agregar el producto a la lista
                                listaProductos.add(producto);
                            }

                            // Llamar al método de callback con la lista de productos cuando se haya completado la obtención
                            callback.onProductosLoaded(listaProductos);
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

    // Interfaz de callback para manejar la respuesta de obtener productos
    public interface GetProductosCallback {
        void onProductosLoaded(List<Producto> productos);
    }


    /*
    //Método para añadir un supermercado
    public void addSupermercado(String nombre, String localizacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUPERMERCADO_NOMBRE, nombre);
        values.put(COLUMN_SUPERMERCADO_LOCALIZACION, localizacion);
        db.insert(TABLE_SUPERMERCADOS, null, values);
        db.close();
    }

    //Método para añadir un producto a un supemercado determinado
    public void addProductoASupermercado(String nombreSupermercado, String nombreProducto, Double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUPERMERCADO_NOMBRE, nombreSupermercado);
        values.put(COLUMN_PRODUCTO_NOMBRE, nombreProducto);
        values.put(COLUMN_PRODUCTO_PRECIO, precio);
        db.insert(TABLE_PRODUCTOS_SUPERMERCADO, null, values);
        db.close();
    }

    //Método para comprobar que el supermercado no existe ya en la base de datos
    public boolean supermercadoExiste(String nombreSupermercado) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUPERMERCADOS +
                " WHERE " + COLUMN_SUPERMERCADO_NOMBRE + " = ?", new String[]{nombreSupermercado});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    //Método para comprobar que el producto no existe ya en la base de datos
    public boolean productoExiste(String nombreSupermercado, String nombreProducto) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS_SUPERMERCADO +
                        " WHERE " + COLUMN_SUPERMERCADO_NOMBRE + " = ? AND " + COLUMN_PRODUCTO_NOMBRE + " = ?",
                new String[]{nombreSupermercado, nombreProducto});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

     */

    public void modificarPrecioProducto(String nombreSupermercado, String nombreProducto, double nuevoPrecio, ModificarPrecioCallback callback) {
        // Crear un objeto JSONObject con los datos del producto
        JSONObject postData = new JSONObject();
        try {
            postData.put("nombre_super", nombreSupermercado);
            postData.put("nombre_prod", nombreProducto);
            postData.put("nuevo_precio", nuevoPrecio);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/modificarPrecioProducto.php";

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
                            // Verificar si la modificación fue exitosa
                            boolean modificacionExitosa = new JSONObject(resultado).getBoolean("modificacion_exitosa");

                            // Llamar al método de callback con el resultado de la modificación
                            callback.onPrecioModificado(modificacionExitosa);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    // Interfaz de callback para manejar el resultado de la modificación del precio
    public interface ModificarPrecioCallback {
        void onPrecioModificado(boolean modificacionExitosa);
    }



    public void obtenerNombreSupermercado(String nombreProducto, GetNombreSupermercadoCallback callback) {
        // Crear un objeto JSONObject con los datos del producto
        JSONObject postData = new JSONObject();
        try {
            postData.put("nombre_prod", nombreProducto);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String serverAddress = "http://34.170.99.24:81/obtenerNombreSupermercado.php";

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
                            // Obtener el nombre del supermercado del resultado JSON
                            String nombreSupermercado = new JSONObject(resultado).getString("nombre_supermercado");

                            // Llamar al método de callback con el nombre del supermercado
                            callback.onNombreSupermercadoLoaded(nombreSupermercado);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(mContext).enqueue(otwr);
    }

    // Interfaz de callback para manejar la respuesta de obtener el nombre del supermercado
    public interface GetNombreSupermercadoCallback {
        void onNombreSupermercadoLoaded(String nombreSupermercado);
    }



}