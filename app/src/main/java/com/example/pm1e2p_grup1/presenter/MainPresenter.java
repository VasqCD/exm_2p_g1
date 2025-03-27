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
import com.example.pm1e2p_grup1.view.activities.MainActivity;
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
            // URL del endpoint actualizada
            String url = ApiMethods.ENDPOINT_CREATE_CONTACT;

            // Crear JSON con los parámetros correctos
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("nombre", name);
            jsonRequest.put("telefono", phone);
            jsonRequest.put("latitud", latitude);
            jsonRequest.put("longitud", longitude);

            // Convertir imagen a Base64 si existe
            if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bitmap = ImageHelper.loadImageFromPath(imagePath);
                if (bitmap != null) {
                    String base64Image = convertBitmapToBase64(bitmap);
                    jsonRequest.put("foto", base64Image);
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
            volleyHandler.postJsonObject(url, jsonRequest,
                    new VolleyHandler.VolleyCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            view.hideLoading();
                            try {
                                boolean success = result.getBoolean("success");
                                String message = result.getString("message");

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

    public void updateContact(int contactId, String name, String phone, double latitude, double longitude, String imagePath) {
        if (!validateInput(name, phone)) {
            return;
        }

        view.showLoading();

        try {
            // actualizacion
            String url = ApiMethods.ENDPOINT_UPDATE_CONTACT;

            // Crear JSON con los parámetros correctos
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("id", contactId);
            jsonRequest.put("nombre", name);
            jsonRequest.put("telefono", phone);
            jsonRequest.put("latitud", latitude);
            jsonRequest.put("longitud", longitude);

            // Añadir foto solo si hay una nueva
            if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bitmap = ImageHelper.loadImageFromPath(imagePath);
                if (bitmap != null) {
                    String base64Image = convertBitmapToBase64(bitmap);
                    jsonRequest.put("foto", base64Image);
                }
            }

            // enviar peticion a la API
            volleyHandler.postJsonObject(url, jsonRequest,
                    new VolleyHandler.VolleyCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            view.hideLoading();
                            try {
                                boolean success = result.getBoolean("success");
                                String message = result.getString("message");

                                if (success) {
                                    view.showMessage(message);
                                    view.onContactUpdated();
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
                            view.showError("Error al actualizar el contacto: " + error);
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
            ((MainActivity) view).showPhotoAlert();
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

    public void checkGpsStatus() {
        if (!LocationHelper.isGpsEnabled(context)) {
            ((MainActivity) view).showGpsAlert();
            return;
        }
        getCurrentLocation();
    }


    public void takePicture() {
        try {
            if (!PermissionHelper.hasPermission(context, android.Manifest.permission.CAMERA)) {
                view.showError("Se requiere permiso de cámara");
                return;
            }

            File photoFile = ImageHelper.createImageFile(context);
            Uri photoUri = ImageHelper.getUriForFile(context, photoFile);
            if (photoUri != null) {
                currentPhotoPath = photoFile.getAbsolutePath();
                view.launchCameraIntent(photoUri);
            } else {
                ((MainActivity) view).showPhotoAlert();
            }
        } catch (Exception e) {
            view.showError("Error al tomar fotografía: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processPhoto() {
        if (currentPhotoPath != null) {
            Bitmap photo = ImageHelper.loadImageFromPathWithOrientation(currentPhotoPath);
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
                    view.showError("Error de ubicación GPS Desactivado");
                }
        );
    }

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