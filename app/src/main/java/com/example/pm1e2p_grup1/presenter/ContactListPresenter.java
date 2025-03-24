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
import java.util.List;

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

        volleyHandler.getJsonArray(ApiMethods.ENDPOINT_CONTACTS,
                new VolleyHandler.VolleyCallback<JSONArray>() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        List<Contact> contacts = new ArrayList<>();

                        try {
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject jsonObject = result.getJSONObject(i);
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
                            Contact contact = parseJsonToContact(result);
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

        String url = ApiMethods.ENDPOINT_CONTACT_BY_ID + contact.getId();
        volleyHandler.deleteRequest(url,
                new VolleyHandler.VolleyCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        view.hideLoading();
                        view.onContactDeleted(contact);
                        view.showMessage("Contacto eliminado exitosamente");
                    }

                    @Override
                    public void onError(String error) {
                        view.hideLoading();
                        view.showError("Error al eliminar el contacto: " + error);
                    }
                });
    }

    // Parsear respuesta JSON a objeto Contact
    private Contact parseJsonToContact(JSONObject json) throws JSONException {
        Contact contact = new Contact();
        contact.setId(json.getInt("id"));
        contact.setName(json.getString("name"));
        contact.setPhone(json.getString("phone"));
        contact.setLatitude(json.getDouble("latitude"));
        contact.setLongitude(json.getDouble("longitude"));

        if (json.has("photo_url") && !json.isNull("photo_url")) {
            contact.setPhotoUrl(json.getString("photo_url"));
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