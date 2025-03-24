package com.example.pm1e2p_grup1.model.api;

public class ApiMethods {
    // URL base del endpoint
    public static final String BASE_URL = "https://tuapi.com/api/";

    // Endpoints específicos
    public static final String ENDPOINT_CONTACTS = BASE_URL + "contacts";
    public static final String ENDPOINT_CONTACT_BY_ID = BASE_URL + "contacts/";

    // Métodos HTTP
    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int METHOD_PUT = 2;
    public static final int METHOD_DELETE = 3;
}