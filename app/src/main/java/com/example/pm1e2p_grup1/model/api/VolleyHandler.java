package com.example.pm1e2p_grup1.model.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyHandler {
    private static final String TAG = "VolleyHandler";
    private static VolleyHandler instance;
    private RequestQueue requestQueue;
    private final Context context;

    private VolleyHandler(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyHandler getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyHandler(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    // Método para peticiones GET que devuelven un JSONArray
    public void getJsonArray(String url, final VolleyCallback<JSONArray> callback) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para peticiones GET que devuelven un JSONObject
    public void getJsonObject(String url, final VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para peticiones POST con un JSONObject
    public void postJsonObject(String url, JSONObject jsonRequest, final VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para peticiones PUT con un JSONObject
    public void putJsonObject(String url, JSONObject jsonRequest, final VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonRequest,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para peticiones DELETE
    public void deleteRequest(String url, final VolleyCallback<String> callback) {
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para cargar imágenes con Volley
    public void getImage(String url, final VolleyCallback<Bitmap> callback) {
        ImageRequest request = new ImageRequest(url,
                response -> {
                    Log.d(TAG, "Image loaded");
                    callback.onSuccess(response);
                }, 0, 0, null,
                error -> {
                    Log.e(TAG, "Error loading image: " + error.toString());
                    callback.onError(error.toString());
                });

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Método para peticiones con parámetros
    public void postFormData(String url, final Map<String, String> params, final VolleyCallback<String> callback) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.toString());
                    callback.onError(error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        setupRequestTimeout(request);
        getRequestQueue().add(request);
    }

    // Configurar timeout para las peticiones
    private void setupRequestTimeout(Request<?> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 segundos de timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    // Interfaz de callback para manejar respuestas
    public interface VolleyCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}