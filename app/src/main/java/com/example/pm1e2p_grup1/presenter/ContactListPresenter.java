
package com.example.pm1e2p_grup1.presenter;

import android.content.Context;
import android.util.Log;

import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.model.api.ApiMethods;
import com.example.pm1e2p_grup1.model.api.VolleyHandler;
import com.example.pm1e2p_grup1.view.interfaces.ContactListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactListPresenter {
    private static final String TAG = "ContactListPresenter";
    private final ContactListView view;
    private final VolleyHandler volleyHandler;

    public ContactListPresenter(ContactListView view, Context context) {
        this.view = view;
        this.volleyHandler = VolleyHandler.getInstance(context);
    }


    public void loadContacts() {
        view.showLoading();

        volleyHandler.getJsonObject(ApiMethods.ENDPOINT_CONTACTS,
                new VolleyHandler.VolleyCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        List<Contact> contacts = new ArrayList<>();

                        try {
                            // Obtener el array de contactos dentro del objeto JSON
                            JSONArray contactsArray = result.getJSONArray("contactos");

                            // Procesar cada contacto en el array
                            for (int i = 0; i < contactsArray.length(); i++) {
                                JSONObject jsonObject = contactsArray.getJSONObject(i);
                                Contact contact = parseJsonToContact(jsonObject);
                                contacts.add(contact);
                            }

                            view.hideLoading();

                            if (contacts.isEmpty()) {
                                view.showEmptyView();
                            } else {
                                view.showContacts(contacts);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                            view.hideLoading();
                            view.showError("Error al procesar los datos: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        view.hideLoading();
                        view.showError("Error al cargar los contactos: " + error);
                    }
                });
    }

    public void getContactDetail(int contactId) {
        view.showLoading();

        String url = ApiMethods.ENDPOINT_CONTACT_BY_ID + contactId;
        volleyHandler.getJsonObject(url,
                new VolleyHandler.VolleyCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        view.hideLoading();
                        try {
                            // Comprobar si el resultado contiene un objeto "contacto"
                            JSONObject contactObject;
                            if (result.has("contacto")) {
                                contactObject = result.getJSONObject("contacto");
                            } else {
                                // Si no tiene la clave "contacto", asumimos que el contacto está directamente en el objeto principal
                                contactObject = result;
                            }

                            Contact contact = parseJsonToContact(contactObject);
                            // Aquí podrías navegar a la vista de detalle o actualizar UI
                        } catch (JSONException e) {
                            view.showError("Error al procesar los datos: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        view.hideLoading();
                        view.showError("Error al obtener detalles del contacto: " + error);
                    }
                });
    }

    public void deleteContact(Contact contact) {
        view.showLoading();

        try {
            // Crear JSON para enviar el ID a eliminar
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("id", contact.getId());

            volleyHandler.postJsonObject(ApiMethods.ENDPOINT_DELETE_CONTACT, jsonRequest,
                    new VolleyHandler.VolleyCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            view.hideLoading();
                            try {
                                boolean success = result.getBoolean("success");
                                String message = result.getString("message");

                                if (success) {
                                    view.onContactDeleted(contact);
                                    view.showMessage(message);
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
                            view.showError("Error al eliminar el contacto: " + error);
                        }
                    });
        } catch (JSONException e) {
            view.hideLoading();
            view.showError("Error al crear la solicitud: " + e.getMessage());
        }
    }

        // Parsear respuesta JSON a objeto Contact (adaptado a la estructura de la API)
        private Contact parseJsonToContact(JSONObject json) throws JSONException {
            Contact contact = new Contact();
            contact.setId(json.getInt("id"));
            contact.setName(json.getString("nombre"));
            contact.setPhone(json.getString("telefono"));
            contact.setLatitude(json.getDouble("latitud"));
            contact.setLongitude(json.getDouble("longitud"));

            if (json.has("foto") && !json.isNull("foto")) {
                contact.setPhotoUrl(json.getString("foto"));
            }

            if (json.has("created_at") && !json.isNull("created_at")) {
                contact.setCreatedAt(json.getString("created_at"));
            }

            if (json.has("updated_at") && !json.isNull("updated_at")) {
                contact.setUpdatedAt(json.getString("updated_at"));
            }

            return contact;
        }
    }