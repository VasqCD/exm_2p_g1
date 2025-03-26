
package com.example.pm1e2p_grup1.model.api;

public class ApiMethods {
    // URL base del endpoint
    public static final String BASE_URL = "http://107.22.154.13/api/contactos/";

    // Endpoints específicos
    public static final String ENDPOINT_CONTACTS = BASE_URL + "read.php"; // Todos los contactos
    public static final String ENDPOINT_CONTACT_BY_ID = BASE_URL + "read_one.php?id="; // Contacto por ID
    public static final String ENDPOINT_CREATE_CONTACT = BASE_URL + "create_form.php"; // Crear contacto
    public static final String ENDPOINT_UPDATE_CONTACT = BASE_URL + "update.php"; // Actualizar contacto
    public static final String ENDPOINT_DELETE_CONTACT = BASE_URL + "delete.php"; // Eliminar contacto

    // Métodos HTTP
    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int METHOD_PUT = 2;
    public static final int METHOD_DELETE = 3;
}