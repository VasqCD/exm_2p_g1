package com.example.pm1e2p_grup1.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.function.Consumer;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Context context;

    public LocationHelper(Context context) {
        this.context = context; // Guarda el contexto
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(Consumer<Location> locationConsumer, Consumer<String> errorConsumer) {
        // Verificar permisos
        if (!PermissionHelper.hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                !PermissionHelper.hasPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            errorConsumer.accept("Permisos de ubicación no concedidos");
            return;
        }

        // Verificar si el GPS está activo
        if (!isGpsEnabled(context)) {
            errorConsumer.accept("GPS_DISABLED");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        locationConsumer.accept(location);
                    } else {
                        requestNewLocationData(locationConsumer, errorConsumer);
                    }
                })
                .addOnFailureListener(e -> errorConsumer.accept("Error al obtener ubicación: " + e.getMessage()));
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(Consumer<Location> locationConsumer, Consumer<String> errorConsumer) {
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    locationConsumer.accept(locationResult.getLastLocation());
                    stopLocationUpdates(); // Detener después de obtener una ubicación
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}