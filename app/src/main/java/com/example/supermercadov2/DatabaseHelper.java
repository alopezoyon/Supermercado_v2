package com.example.supermercadov2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//Esta clase implementa la base de datos.
//Contiene los datos de registro (username, password, email, name y lastname) de los usuarios (se guardan en la tabla "users")
//Contiene los datos del supermercado (nombre_super, localizacion) en la tabla "supermercados"
//Contiene también los productos de cada supermercado en la tabla "productos_supermercado".
//Cada producto tiene su nombre y precio.

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "supermercado_database";
    private static final int DATABASE_VERSION = 20;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LASTNAME = "lastname";
    private static final String TABLE_SUPERMERCADOS = "supermercados";
    private static final String COLUMN_SUPERMERCADO_NOMBRE = "nombre_super";
    private static final String COLUMN_SUPERMERCADO_LOCALIZACION = "localizacion";
    private static final String COLUMN_PRODUCTO_NOMBRE = "nombre_prod";
    private static final String COLUMN_PRODUCTO_PRECIO = "precio";
    private static final String TABLE_PRODUCTOS_SUPERMERCADO = "productos_supermercado";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Método para crear la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTableQuery = "CREATE TABLE " + TABLE_USERS +
                " (" + COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LASTNAME + " TEXT)";
        db.execSQL(createUserTableQuery);

        String createSupermercadosTableQuery = "CREATE TABLE " + TABLE_SUPERMERCADOS +
                " (" + COLUMN_SUPERMERCADO_NOMBRE + " TEXT PRIMARY KEY, " +
                COLUMN_SUPERMERCADO_LOCALIZACION + " TEXT)";
        db.execSQL(createSupermercadosTableQuery);

        String createProductosSupermercadoTableQuery = "CREATE TABLE " + TABLE_PRODUCTOS_SUPERMERCADO +
                " (" + COLUMN_SUPERMERCADO_NOMBRE + " TEXT, " +
                COLUMN_PRODUCTO_NOMBRE + " TEXT PRIMARY KEY, " +
                COLUMN_PRODUCTO_PRECIO + " REAL, " +
                " FOREIGN KEY(" + COLUMN_SUPERMERCADO_NOMBRE + ") REFERENCES " + TABLE_SUPERMERCADOS + "(" + COLUMN_SUPERMERCADO_NOMBRE + "))";
        db.execSQL(createProductosSupermercadoTableQuery);
    }



    //Método usado si queremos resetear la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPERMERCADOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS_SUPERMERCADO);

        onCreate(db);

         */

    }


    //Método para añadir un usuario
    public void addUser(String username, String password, String email, String name, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LASTNAME, lastName);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }


    //Método para obtener los supermercados guardados
    public List<Supermercado> getSupermercados() {
        List<Supermercado> listaSupermercados = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUPERMERCADOS, null);

        while (cursor.moveToNext()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUPERMERCADO_NOMBRE));
            String localizacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUPERMERCADO_LOCALIZACION));
            List<Producto> productos = getProductosPorSupermercado(nombre);

            listaSupermercados.add(new Supermercado(nombre, localizacion, productos));
        }

        cursor.close();
        db.close();
        return listaSupermercados;
    }

    //Obtener los productos de cada supermercado
    public List<Producto> getProductosPorSupermercado(String nombreSupermercado) {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTOS_SUPERMERCADO +
                " WHERE " + COLUMN_SUPERMERCADO_NOMBRE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{nombreSupermercado});

        while (cursor.moveToNext()) {
            String nombreProducto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_NOMBRE));
            double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_PRECIO));
            listaProductos.add(new Producto(nombreProducto, precio));
        }

        cursor.close();

        return listaProductos;
    }

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

    // Método para modificar el precio de un producto en la base de datos
    public void modificarPrecioProducto(String nombreSupermercado, String nombreProducto, double nuevoPrecio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCTO_PRECIO, nuevoPrecio);

        String whereClause = COLUMN_SUPERMERCADO_NOMBRE + " = ? AND " + COLUMN_PRODUCTO_NOMBRE + " = ?";
        String[] whereArgs = {nombreSupermercado, nombreProducto};

        db.update(TABLE_PRODUCTOS_SUPERMERCADO, values, whereClause, whereArgs);
        db.close();
    }

    //Método para obtener el nombre de un supemercado dado un producto
    public String obtenerNombreSupermercado(Producto producto) {
        SQLiteDatabase db = this.getReadableDatabase();
        String nombreSupermercado = null;
        String query = "SELECT " + COLUMN_SUPERMERCADO_NOMBRE +
                " FROM " + TABLE_PRODUCTOS_SUPERMERCADO +
                " WHERE " + COLUMN_PRODUCTO_NOMBRE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{producto.getNombre()});
        if (cursor.moveToFirst()) {
            nombreSupermercado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUPERMERCADO_NOMBRE));
        }
        cursor.close();
        db.close();
        return nombreSupermercado;
    }

}