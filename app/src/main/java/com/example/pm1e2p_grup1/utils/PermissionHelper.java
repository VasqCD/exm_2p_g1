package com.example.pm1e2p_grup1.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PermissionHelper {

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static ActivityResultLauncher<String[]> registerForPermissions(AppCompatActivity activity, Consumer<Boolean> callback) {
        return activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    callback.accept(allGranted);
                });
    }

    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();

        // Permisos de cámara
        permissions.add(android.Manifest.permission.CAMERA);

        // Permisos de almacenamiento
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES);
        }

        // Permisos de ubicación
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissions.toArray(new String[0]);
    }
}