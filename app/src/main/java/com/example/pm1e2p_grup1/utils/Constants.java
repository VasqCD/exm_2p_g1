package com.example.pm1e2p_grup1.utils;

public class Constants {
    // Códigos de solicitud
    public static final int REQUEST_IMAGE_CAPTURE = 100;
    public static final int REQUEST_LOCATION_PERMISSION = 101;
    public static final int REQUEST_UPDATE_CONTACT = 102;

    // Claves para Intent extras
    public static final String EXTRA_CONTACT_ID = "contact_id";
    public static final String EXTRA_PHOTO_PATH = "photo_path";
    public static final String EXTRA_CONTACT_NAME = "contact_name";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_MODE = "mode";

    // Modos de operación
    public static final String MODE_UPDATE = "update";
    public static final String MODE_CREATE = "create";
}