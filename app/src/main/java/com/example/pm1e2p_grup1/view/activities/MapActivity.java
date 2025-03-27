package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView txtContactName, txtCoordinates;
    private FloatingActionButton fabOpenMaps;
    private GoogleMap googleMap;

    private String contactName;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ubicación de Contacto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        initViews();

        // Obtener datos del intent
        getIntentData();

        // Configurar vistas con los datos
        setupViews();

        // Inicializar mapa
        initMap();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        txtContactName = findViewById(R.id.txtContactName);
        txtCoordinates = findViewById(R.id.txtCoordinates);
        fabOpenMaps = findViewById(R.id.fabOpenMaps);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            contactName = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
            latitude = intent.getDoubleExtra(Constants.EXTRA_LATITUDE, 0);
            longitude = intent.getDoubleExtra(Constants.EXTRA_LONGITUDE, 0);
        }
    }

    private void setupViews() {
        txtContactName.setText(contactName);
        txtCoordinates.setText(String.format("Lat: %s, Long: %s", latitude, longitude));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        fabOpenMaps.setOnClickListener(v -> openInGoogleMaps());
    }

    private void openInGoogleMaps() {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude + "&travelmode=driving");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir el mapa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng contactLocation = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions()
                .position(contactLocation)
                .title(contactName));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(contactLocation, 15f));

        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // Manejar la excepción si no se tienen los permisos
            Toast.makeText(this, "Se requieren permisos de ubicación para mostrar tu ubicación actual", Toast.LENGTH_SHORT).show();
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }
}