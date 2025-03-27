package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapActivity extends AppCompatActivity {

    private TextView txtContactName, txtCoordinates;
    private FloatingActionButton fabOpenMaps;
    private ImageView mapImageView, markerImageView;

    private String contactName;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Configurar barra de título
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

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        txtContactName = findViewById(R.id.txtContactName);
        txtCoordinates = findViewById(R.id.txtCoordinates);
        fabOpenMaps = findViewById(R.id.fabOpenMaps);
        mapImageView = findViewById(R.id.mapImageView);
        markerImageView = findViewById(R.id.markerImageView);
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

        // Establecer fondo de mapa simulado
        mapImageView.setBackgroundResource(R.drawable.map_background);

        // Mostrar marcador
        markerImageView.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        fabOpenMaps.setOnClickListener(v -> openInGoogleMaps());
    }

    private void openInGoogleMaps() {
        try {
            // Crear una URI para abrir Google Maps en las coordenadas específicas
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + contactName + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Si Google Maps no está instalado, abrir en el navegador
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
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
}