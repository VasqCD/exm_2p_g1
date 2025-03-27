package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.presenter.MainPresenter;
import com.example.pm1e2p_grup1.utils.LocationHelper;
import com.example.pm1e2p_grup1.utils.PermissionHelper;
import com.example.pm1e2p_grup1.view.interfaces.MainView;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements MainView {

    private MainPresenter presenter;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private TextInputEditText etNombre, etTelefono, etLatitud, etLongitud;
    private ImageView ivFotoPerfil;
    private Button btnTomarFoto, btnSalvarContacto, btnContactosSalvados;
    private double latitude = 0, longitude = 0;
    private ActivityResultLauncher<String[]> permissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar la barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_contacto);

        // Registrar launcher para permisos
        permissionsLauncher = PermissionHelper.registerForPermissions(this, allGranted -> {
            if (allGranted) {
                // Inicializar todo lo que requiere permisos
                initAfterPermissions();
            } else {
                showPermissionDialog();
            }
        });

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
        initViews();

        // Inicializar presentador
        presenter = new MainPresenter(this, this);

        // Verificar y solicitar permisos
        checkAndRequestPermissions();
    }

    private void initViews() {
        try {
            etNombre = findViewById(R.id.etNombre);
            etTelefono = findViewById(R.id.etTelefono);
            etLatitud = findViewById(R.id.etLatitud);
            etLongitud = findViewById(R.id.etLongitud);
            ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
            btnTomarFoto = findViewById(R.id.btnTomarFoto);
            btnSalvarContacto = findViewById(R.id.btnSalvarContacto);
            btnContactosSalvados = findViewById(R.id.btnContactosSalvados);

        } catch (Exception e) {
            logError("initViews", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionHelper.hasPermissions(this, PermissionHelper.getRequiredPermissions()) &&
                (latitude == 0 && longitude == 0)) {
            if (LocationHelper.isGpsEnabled(this)) {
                presenter.getCurrentLocation();
            }
        }
    }

    private void checkAndRequestPermissions() {
        String[] requiredPermissions = PermissionHelper.getRequiredPermissions();

        if (!PermissionHelper.hasPermissions(this, requiredPermissions)) {
            permissionsLauncher.launch(requiredPermissions);
        } else {
            initAfterPermissions();
        }
    }

    private void initAfterPermissions() {
        // Configurar listeners
        setupListeners();

        // Bloquear campos de coordenadas
        etLatitud.setEnabled(false);
        etLongitud.setEnabled(false);

        // Verificar GPS y obtener ubicación
        if (!LocationHelper.isGpsEnabled(this)) {
            showGpsAlert();
        } else {
            presenter.getCurrentLocation();
        }
    }

    private void setupListeners() {
        btnTomarFoto.setOnClickListener(v -> {
            if (PermissionHelper.hasPermission(this, android.Manifest.permission.CAMERA)) {
                presenter.takePicture();
            } else {
                showMessage("Se requiere permiso de cámara");
            }
        });

        btnSalvarContacto.setOnClickListener(v -> {
            if (validateInputs()) {
                String name = etNombre.getText().toString();
                String phone = etTelefono.getText().toString();

                try {
                    double latitude = Double.parseDouble(etLatitud.getText().toString());
                    double longitude = Double.parseDouble(etLongitud.getText().toString());

                    presenter.saveContact(name, phone, latitude, longitude, presenter.getCurrentPhotoPath());
                } catch (NumberFormatException e) {
                    showError("Error en el formato de coordenadas");
                }
            }
        });

        btnContactosSalvados.setOnClickListener(v -> navigateToContactList());
    }

    private boolean validateInputs() {
        if (etNombre.getText().toString().trim().isEmpty()) {
            showError("El nombre es requerido");
            return false;
        }

        if (etTelefono.getText().toString().trim().isEmpty()) {
            showError("El teléfono es requerido");
            return false;
        }

        if (etLatitud.getText().toString().trim().isEmpty() ||
                etLongitud.getText().toString().trim().isEmpty()) {
            showError("Se requieren coordenadas de ubicación");
            return false;
        }

        if (presenter.getCurrentPhotoPath() == null) {
            showError("Se requiere tomar una fotografía");
            return false;
        }

        return true;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos requeridos")
                .setMessage("Esta aplicación necesita permisos para funcionar correctamente.")
                .setPositiveButton("Configuración", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String error) {
        // Crear y mostrar una alerta en lugar de un Toast
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton("OK", null)
                .show();
    }

    public void showGpsAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("GPS no está activo")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Abrir configuración de ubicación
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public void showPhotoAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("No se ha tomado fotografía")
                .setPositiveButton("OK", null)
                .show();
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
        ivFotoPerfil.setImageResource(R.drawable.logo_utm);
        presenter.setCurrentPhotoPath(null);

        // Resetear los valores de latitud y longitud
        this.latitude = 0;
        this.longitude = 0;

        // Verificar GPS y obtener nueva ubicación
        if (LocationHelper.isGpsEnabled(this)) {
            presenter.getCurrentLocation();
        } else {
            showGpsAlert();
        }
    }

    @Override
    public void navigateToContactList() {
        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);
    }

    @Override
    public void launchCameraIntent(Uri photoUri) {
        takePictureLauncher.launch(photoUri);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    private void logError(String method, Exception e) {
        String errorMsg = "Error en " + method + ": " + e.getMessage();
        Log.e("MainActivity", errorMsg, e);
        showError(errorMsg);
    }
}