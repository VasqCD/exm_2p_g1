package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.presenter.MainPresenter;
import com.example.pm1e2p_grup1.utils.Constants;
import com.example.pm1e2p_grup1.utils.ImageHelper;
import com.example.pm1e2p_grup1.utils.LocationHelper;
import com.example.pm1e2p_grup1.utils.PermissionHelper;
import com.example.pm1e2p_grup1.view.interfaces.MainView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MainView {

    private MainPresenter presenter;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private TextInputEditText etNombre, etTelefono, etLatitud, etLongitud;
    private ImageView ivFotoPerfil;
    private Button btnTomarFoto, btnSalvarContacto, btnContactosSalvados;
    private double latitude = 0, longitude = 0;
    private ActivityResultLauncher<String[]> permissionsLauncher;

    private boolean isUpdateMode = false;
    private int contactId = -1;

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

        // Verificar y procesar los datos de actualizacion
        checkUpdateModeIntent();

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

            etTelefono.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    validatePhoneNumber(s.toString());
                }
            });

        } catch (Exception e) {
            logError("initViews", e);
        }
    }

    private void checkUpdateModeIntent() {
        Intent intent = getIntent();
        if (intent != null && Constants.MODE_UPDATE.equals(intent.getStringExtra(Constants.EXTRA_MODE))) {
            isUpdateMode = true;
            contactId = intent.getIntExtra(Constants.EXTRA_CONTACT_ID, -1);

            // Cargar datos del contacto
            if (contactId != -1) {
                String name = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
                String phone = intent.getStringExtra(Constants.EXTRA_PHONE);
                latitude = intent.getDoubleExtra(Constants.EXTRA_LATITUDE, 0);
                longitude = intent.getDoubleExtra(Constants.EXTRA_LONGITUDE, 0);

                // AÑADIR ESTO: Obtener URL de la foto si existe
                String photoUrl = intent.getStringExtra(Constants.EXTRA_PHOTO_URL);

                // Actualizar UI
                etNombre.setText(name);
                etTelefono.setText(phone);
                etLatitud.setText(String.valueOf(latitude));
                etLongitud.setText(String.valueOf(longitude));

                // AÑADIR ESTO: Cargar la foto si existe URL
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    cargarImagenDesdeUrl(photoUrl);
                }

                // Cambiar texto del botón
                btnSalvarContacto.setText("Actualizar Contacto");
            }
        }
    }

    private void cargarImagenDesdeUrl(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            showLoading();

            if (photoUrl.startsWith("http")) {
                // Si es una URL web, usar Glide
                Glide.with(this)
                        .load(photoUrl)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                hideLoading();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                hideLoading();
                                if (resource instanceof BitmapDrawable) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    ivFotoPerfil.setImageBitmap(bitmap);

                                    // Guardar la imagen localmente
                                    try {
                                        File photoFile = ImageHelper.createImageFile(MainActivity.this);
                                        FileOutputStream fos = new FileOutputStream(photoFile);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                        fos.close();

                                        // Actualizar la ruta de la foto
                                        presenter.setCurrentPhotoPath(photoFile.getAbsolutePath());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        showError("Error al guardar la imagen: " + e.getMessage());
                                    }
                                }
                                return false;
                            }
                        })
                        .into(ivFotoPerfil);
            } else if (photoUrl.length() > 100) {
                // Podría ser una imagen en base64
                try {
                    byte[] decodedString = Base64.decode(photoUrl, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivFotoPerfil.setImageBitmap(bitmap);

                    // Guardar la imagen localmente igual que arriba
                    File photoFile = ImageHelper.createImageFile(MainActivity.this);
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                    presenter.setCurrentPhotoPath(photoFile.getAbsolutePath());

                    hideLoading();
                } catch (Exception e) {
                    hideLoading();
                    showError("Error al decodificar la imagen: " + e.getMessage());
                }
            } else {
                hideLoading();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionHelper.hasPermissions(this, PermissionHelper.getRequiredPermissions()) &&
                !isUpdateMode && (latitude == 0 && longitude == 0)) {
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

        // En modo actualización, no actualizar la ubicación automáticamente
        if (!isUpdateMode) {
            // Verificar GPS y obtener ubicación
            if (!LocationHelper.isGpsEnabled(this)) {
                showGpsAlert();
            } else {
                presenter.getCurrentLocation();
            }
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

                    if (isUpdateMode && contactId != -1) {
                        presenter.updateContact(contactId, name, phone, latitude, longitude, presenter.getCurrentPhotoPath());
                    } else {
                        presenter.saveContact(name, phone, latitude, longitude, presenter.getCurrentPhotoPath());
                    }
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

        String phoneNumber = etTelefono.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            showError("El teléfono es requerido");
            return false;
        } else if (phoneNumber.length() != 8) {
            showError("El teléfono debe tener exactamente 8 dígitos");
            return false;
        } else if (!phoneNumber.matches("^[0-9]+$")) {
            showError("El teléfono solo debe contener números");
            return false;
        } else if (!phoneNumber.matches("^[389].*$")) {
            showError("El teléfono debe comenzar con 3, 8 o 9");
            return false;
        }

        if (etLatitud.getText().toString().trim().isEmpty() ||
                etLongitud.getText().toString().trim().isEmpty()) {
            showError("Se requieren coordenadas de ubicación");
            return false;
        }

        if (!isUpdateMode && presenter.getCurrentPhotoPath() == null) {
            showError("Se requiere tomar una fotografía");
            return false;
        }

        return true;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            etTelefono.setError("El teléfono es requerido");
        } else if (phoneNumber.length() != 8) {
            etTelefono.setError("El teléfono debe tener exactamente 8 dígitos");
        } else if (!phoneNumber.matches("^[0-9]+$")) {
            etTelefono.setError("El teléfono solo debe contener números");
        } else if (!phoneNumber.matches("^[389].*$")) {
            etTelefono.setError("El teléfono debe comenzar con 3, 8 o 9");
        } else {
            etTelefono.setError(null);
        }
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
    public void onContactUpdated() {
        // Devolver resultado OK a la actividad anterior
        setResult(RESULT_OK);
        finish();
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
        // Implementar si es necesario mostrar un indicador de carga
    }

    @Override
    public void hideLoading() {
        // Implementar si es necesario ocultar un indicador de carga
    }

    private void logError(String method, Exception e) {
        String errorMsg = "Error en " + method + ": " + e.getMessage();
        Log.e("MainActivity", errorMsg, e);
        showError(errorMsg);
    }
}