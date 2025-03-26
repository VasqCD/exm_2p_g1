package com.example.pm1e2p_grup1.view.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

public interface MainView {
    void showMessage(String message);
    void showError(String error);
    void displayImage(Bitmap image);
    void displayLocation(double latitude, double longitude);
    void clearForm();
    void navigateToContactList();
    void showLoading();
    void hideLoading();
    void launchCameraIntent(Uri photoUri);
}