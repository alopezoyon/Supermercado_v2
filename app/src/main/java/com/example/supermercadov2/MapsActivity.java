package com.example.supermercadov2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Inicializar el Geocoder
        geocoder = new Geocoder(this, Locale.getDefault());

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para ser usado.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Aquí puedes añadir marcadores o realizar cualquier otra operación después de que el mapa esté listo.

        // Obtener la lista de localizaciones extraída del intent
        List<String> listaLocalizaciones = getIntent().getStringArrayListExtra("LOCALIZACIONES_EXTRA");

        // Agregar marcadores para cada localización
        if (listaLocalizaciones != null && !listaLocalizaciones.isEmpty()) {
            for (String localizacion : listaLocalizaciones) {
                LatLng location = getLocationFromAddress(getApplicationContext(), localizacion);
                if (location != null) {
                    mMap.addMarker(new MarkerOptions().position(location).title(localizacion));
                }
            }
        }
    }


    // Método para obtener las coordenadas de latitud y longitud a partir de una dirección
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        List<Address> addressList = null;
        LatLng latLng = null;
        try {
            // Usar Geocoder para obtener la lista de direcciones a partir de la dirección proporcionada
            addressList = geocoder.getFromLocationName(strAddress, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                // Obtener las coordenadas de latitud y longitud de la dirección
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                // Crear un objeto LatLng con las coordenadas obtenidas
                latLng = new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }
}



