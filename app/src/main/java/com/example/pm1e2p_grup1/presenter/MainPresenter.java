package com.example.pm1e2p_grup1.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.model.api.ApiMethods;
import com.example.pm1e2p_grup1.model.api.VolleyHandler;
import com.example.pm1e2p_grup1.utils.ImageHelper;
import com.example.pm1e2p_grup1.utils.LocationHelper;
import com.example.pm1e2p_grup1.utils.PermissionHelper;
import com.example.pm1e2p_grup1.view.interfaces.MainView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainPresenter {
    private final MainView view;
    private final Context context;
    private final VolleyHandler volleyHandler;
    private String currentPhotoPath;
    private LocationHelper locationHelper;

    public MainPresenter(MainView view, Context context) {
        this.view = view;
        this.context = context;
        this.volleyHandler = VolleyHandler.getInstance(context);
        this.locationHelper = new LocationHelper(context);
    }

    public void saveContact(String name, String phone, double latitude, double longitude, String imagePath) {
        if (!validateInput(name, phone, imagePath)) {
            return;
        }

        view.showLoading();

        try {
            // URL del endpoint
            String url = "http://localhost/CRUDPHP/POSTContacto.php";

            // Preparar datos para enviar
            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            params.put("phone", phone);
            params.put("latitude", String.valueOf(latitude));
            params.put("longitude", String.valueOf(longitude));

            // Convertir imagen a Base64 si existe
            if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bitmap = ImageHelper.loadImageFromPath(imagePath);
                if (bitmap != null) {
                    String base64Image = convertBitmapToBase64(bitmap);
                    params.put("photo", base64Image);
                } else {
                    view.hideLoading();
                    view.showError("Error al procesar la imagen");
                    return;
                }
            } else {
                view.hideLoading();
                view.showError("Se requiere una fotografía");
                return;
            }

            // Enviar petición a la API
            volleyHandler.postFormData(url, params,
                    new VolleyHandler.VolleyCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            view.hideLoading();
                            try {
                                JSONObject response = new JSONObject(result);
                                boolean success = response.getBoolean("success");
                                String message = response.getString("message");

                                if (success) {
                                    view.showMessage(message);
                                    view.clearForm();
                                } else {
                                    view.showError(message);
                                }
                            } catch (JSONException e) {
                                view.showError("Error al procesar la respuesta: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(String error) {
                            view.hideLoading();
                            view.showError("Error al guardar el contacto: " + error);
                        }
                    });

        } catch (Exception e) {
            view.hideLoading();
            view.showError("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    private boolean validateInput(String name, String phone, String imagePath) {
        if (name == null || name.trim().isEmpty()) {
            view.showError("El nombre es requerido");
            return false;
        }

        if (phone == null || phone.trim().isEmpty()) {
            view.showError("El teléfono es requerido");
            return false;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            view.showError("Se requiere tomar una fotografía");
            return false;
        }

        return true;
    }

    private boolean validateInput(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            view.showError("El nombre es requerido");
            return false;
        }

        if (phone == null || phone.trim().isEmpty()) {
            view.showError("El teléfono es requerido");
            return false;
        }

        return true;
    }

    public void takePicture() {
        try {
            File photoFile = ImageHelper.createImageFile(context);
            Uri photoUri = ImageHelper.getUriForFile(context, photoFile);
            currentPhotoPath = photoFile.getAbsolutePath();
            view.launchCameraIntent(photoUri);  // Cambiado de startCameraIntent a launchCameraIntent
        } catch (IOException e) {
            view.showError("Error al crear archivo para la foto: " + e.getMessage());
        }
    }

    public void processPhoto() {
        if (currentPhotoPath != null) {
            Bitmap photo = ImageHelper.loadImageFromPath(currentPhotoPath);
            if (photo != null) {
                view.displayImage(photo);
            } else {
                view.showError("Error al procesar la imagen");
            }
        }
    }

    public void getCurrentLocation() {
        if (!PermissionHelper.hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                !PermissionHelper.hasPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            view.showError("Se requieren permisos de ubicación");
            return;
        }

        view.showLoading();
        locationHelper.getLastLocation(
                location -> {
                    view.hideLoading();
                    view.displayLocation(location.getLatitude(), location.getLongitude());
                },
                error -> {
                    view.hideLoading();
                    view.showError("Error de ubicación: " + error);
                    // Valores por defecto
                    view.displayLocation(15.5063, -88.0249);
                }
        );
    }

    // Método auxiliar para convertir Bitmap a Base64
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String path) {
        this.currentPhotoPath = path;
    }
}