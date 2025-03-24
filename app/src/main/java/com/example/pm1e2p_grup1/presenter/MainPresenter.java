package com.example.pm1e2p_grup1.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.model.api.ApiMethods;
import com.example.pm1e2p_grup1.model.api.VolleyHandler;
import com.example.pm1e2p_grup1.utils.ImageHelper;
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

    public MainPresenter(MainView view, Context context) {
        this.view = view;
        this.context = context;
        this.volleyHandler = VolleyHandler.getInstance(context);
    }

    public void saveContact(String name, String phone, double latitude, double longitude, String imagePath) {
        if (!validateInput(name, phone)) {
            return;
        }

        view.showLoading();

        try {
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
                }
            }

            // Enviar petición a la API
            volleyHandler.postFormData(ApiMethods.ENDPOINT_CONTACTS, params,
                    new VolleyHandler.VolleyCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            view.hideLoading();
                            view.showMessage("Contacto guardado exitosamente");
                            view.clearForm();
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
        // Implementar lógica para obtener la ubicación actual
        // Este es solo un método de ejemplo, necesitaría usar FusedLocationProviderClient
        double defaultLatitude = 15.5063;
        double defaultLongitude = -88.0249;
        view.displayLocation(defaultLatitude, defaultLongitude);
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