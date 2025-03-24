package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.presenter.MainPresenter;
import com.example.pm1e2p_grup1.view.interfaces.MainView;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements MainView {

    private MainPresenter presenter;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private TextInputEditText etNombre, etTelefono, etLatitud, etLongitud;
    private ImageView ivFotoPerfil;
    private Button btnTomarFoto, btnSalvarContacto, btnContactosSalvados;
    private ProgressBar progressBar;
    private double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar la barra de título si lo prefieres
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_contacto);

        // Registrar el launcher para la cámara
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        // La foto se tomó exitosamente
                        presenter.processPhoto();
                    }
                }
        );

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnSalvarContacto = findViewById(R.id.btnSalvarContacto);
        btnContactosSalvados = findViewById(R.id.btnContactosSalvados);

        // Añade un ProgressBar a tu layout o asegúrate de crear uno programáticamente
        // progressBar = findViewById(R.id.progressBar);

        // Inicializar presentador
        presenter = new MainPresenter(this, this);

        // Configurar listeners
        btnTomarFoto.setOnClickListener(v -> presenter.takePicture());

        btnSalvarContacto.setOnClickListener(v -> {
            String name = etNombre.getText().toString();
            String phone = etTelefono.getText().toString();

            // Leer latitud y longitud desde los campos si no tienes detección automática
            try {
                if (!etLatitud.getText().toString().isEmpty()) {
                    latitude = Double.parseDouble(etLatitud.getText().toString());
                }
                if (!etLongitud.getText().toString().isEmpty()) {
                    longitude = Double.parseDouble(etLongitud.getText().toString());
                }
            } catch (NumberFormatException e) {
                showError("Error en el formato de coordenadas");
                return;
            }

            presenter.saveContact(name, phone, latitude, longitude, presenter.getCurrentPhotoPath());
        });

        btnContactosSalvados.setOnClickListener(v -> navigateToContactList());

        // Obtener ubicación inicial
        presenter.getCurrentLocation();
    }

    // Ya no necesitamos el método onActivityResult al usar ActivityResultLauncher

    // Implementaciones de la interfaz MainView

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void displayImage(Bitmap image) {
        ivFotoPerfil.setImageBitmap(image);
    }

    @Override
    public void displayLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        etLatitud.setText(String.valueOf(latitude));
        etLongitud.setText(String.valueOf(longitude));
    }

    @Override
    public void clearForm() {
        etNombre.setText("");
        etTelefono.setText("");
        etLatitud.setText("");
        etLongitud.setText("");
        ivFotoPerfil.setImageResource(R.drawable.logo_utm); // Asegúrate de tener esta imagen
        presenter.setCurrentPhotoPath(null);
    }

    @Override
    public void navigateToContactList() {
        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void launchCameraIntent(Uri photoUri) {
        takePictureLauncher.launch(photoUri);
    }
}