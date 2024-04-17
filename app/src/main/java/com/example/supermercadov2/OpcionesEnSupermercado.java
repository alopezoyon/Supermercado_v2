package com.example.supermercadov2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Locale;

public class OpcionesEnSupermercado extends AppCompatActivity {

    private static final int REQUEST_CODE_CALENDAR_PERMISSION = 100;
    private int annio, mes, dia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones_en_supermercado);

        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        annio = calendar.get(Calendar.YEAR);
        mes = calendar.get(Calendar.MONTH);
        dia = calendar.get(Calendar.DAY_OF_MONTH);

        // Agregar el clic del botón "Añadir Evento"
        findViewById(R.id.btnAgregarEvento).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarPermisosYAbrirDialogoAgregarEvento();
            }
        });
    }

    // Método para solicitar permisos de calendario y abrir el diálogo para agregar un evento
    private void solicitarPermisosYAbrirDialogoAgregarEvento() {
        // Verificar si los permisos de calendario ya están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // Los permisos no están concedidos, solicitarlos al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE_CALENDAR_PERMISSION);
        } else {
            // Los permisos de calendario ya están concedidos, abrir el diálogo para agregar un evento
            abrirDialogoAgregarEvento();
        }
    }

    // Método para manejar la respuesta de la solicitud de permisos de calendario
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CALENDAR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de calendario concedido, abrir el diálogo para agregar un evento
                abrirDialogoAgregarEvento();
            } else {
                // Permiso de calendario denegado, mostrar un mensaje o realizar otra acción
                Toast.makeText(this, "Se requiere permiso de calendario para agregar un evento.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para abrir el diálogo para agregar un evento en el calendario
    private void abrirDialogoAgregarEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Evento");

        // Inflar el layout personalizado para el diálogo
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.activity_dialog_agregar_evento, null);

        // Obtener referencias de los EditText en el layout del diálogo
        final EditText etTitulo = viewInflated.findViewById(R.id.etTituloEvento);
        final EditText etDescripcion = viewInflated.findViewById(R.id.etDescripcionEvento);
        final EditText etFechaEvento = viewInflated.findViewById(R.id.etFechaEvento);

        // Configurar el clic del campo de texto de la fecha para abrir el DatePickerDialog
        etFechaEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la fecha actual
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Crear un DatePickerDialog con la fecha actual como fecha inicial
                DatePickerDialog datePickerDialog = new DatePickerDialog(OpcionesEnSupermercado.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Formatear la fecha seleccionada en formato "aaaa/mm/dd"
                        String formattedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year, monthOfYear + 1, dayOfMonth);

                        // Establecer la fecha formateada en el EditText
                        etFechaEvento.setText(formattedDate);
                    }
                }, year, month, dayOfMonth);

                // Mostrar el DatePickerDialog
                datePickerDialog.show();
            }
        });

        // Establecer el layout personalizado al AlertDialog
        builder.setView(viewInflated);

        // Configurar los botones "Guardar" y "Cancelar" del diálogo
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el título, la descripción y la fecha ingresados por el usuario
                String titulo = etTitulo.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();
                String fecha = etFechaEvento.getText().toString().trim();

                // Validar que se haya ingresado un título y una fecha
                if (!titulo.isEmpty() && !fecha.isEmpty()) {
                    // Insertar el evento en el calendario
                    insertarEventoEnCalendario(titulo, descripcion);
                } else {
                    Toast.makeText(OpcionesEnSupermercado.this, "Debe ingresar un título y una fecha para el evento.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo sin realizar ninguna acción
                dialog.cancel();
            }
        });

        // Mostrar el AlertDialog
        builder.show();
    }

    // Método para insertar un evento en el calendario
    private void insertarEventoEnCalendario(String titulo, String descripcion) {
        // Crear un objeto ContentValues para almacenar los valores del evento
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, obtenerIdCalendario()); // ID del calendario (puedes obtenerlo consultando la base de datos de calendario)
        values.put(CalendarContract.Events.TITLE, titulo); // Título del evento
        values.put(CalendarContract.Events.DESCRIPTION, descripcion); // Descripción del evento

        // Establecer la fecha del evento utilizando las variables annio, mes y dia
        Calendar cal = Calendar.getInstance();
        cal.set(annio, mes, dia);
        values.put(CalendarContract.Events.DTSTART, cal.getTimeInMillis()); // Fecha de inicio del evento

        // Calcular la fecha de finalización del evento (por ejemplo, 1 hora después del inicio)
        cal.add(Calendar.HOUR, 1); // Ejemplo: Duración de 1 hora
        values.put(CalendarContract.Events.DTEND, cal.getTimeInMillis()); // Fecha de finalización del evento

        // Incluir la zona horaria en los valores del evento
        values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());

        // Obtener el ContentResolver
        ContentResolver resolver = getContentResolver();

        try {
            // Insertar el evento en el calendario
            resolver.insert(CalendarContract.Events.CONTENT_URI, values);
            Toast.makeText(this, "Evento agregado al calendario.", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            // Manejar el caso en que el permiso WRITE_CALENDAR no esté concedido
            Toast.makeText(this, "No se pudo agregar el evento al calendario. Asegúrate de conceder los permisos necesarios.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Manejar otros errores
            Toast.makeText(this, "Error al agregar el evento al calendario.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    // Método para obtener el ID del calendario
    private int obtenerIdCalendario() {
        int idCalendario = -1;

        // Define las columnas que deseas recuperar
        String[] columnas = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE
        };

        // Consulta al proveedor de contenido de calendario para obtener los calendarios
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    CalendarContract.Calendars.CONTENT_URI,
                    columnas,
                    null,
                    null,
                    null
            );

            // Verifica si se encontraron resultados
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Recupera el ID del calendario
                    idCalendario = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));

                    // Verifica si el calendario es de tipo "Google"
                    String tipoCuenta = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE));
                    if (tipoCuenta != null && tipoCuenta.equals("com.google")) {
                        // Si es un calendario de Google, detén el bucle y devuelve su ID
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (SecurityException e) {
            // Maneja el caso en que no se tienen los permisos necesarios para acceder al proveedor de contenido
            e.printStackTrace();
        } finally {
            // Cierra el cursor después de usarlo
            if (cursor != null) {
                cursor.close();
            }
        }

        return idCalendario;
    }

}
