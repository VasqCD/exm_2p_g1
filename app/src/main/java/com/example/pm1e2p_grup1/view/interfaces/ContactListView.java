package com.example.pm1e2p_grup1.view.interfaces;

import com.example.pm1e2p_grup1.model.Contact;

import java.util.List;

public interface ContactListView {
    void showLoading();
    void hideLoading();
    void showContacts(List<Contact> contacts);
    void showEmptyView();
    void showError(String message);
    void showMessage(String message);
    void onContactDeleted(Contact contact);
    void onContactSelected(Contact contact);
    void filterContacts(String query);
    void navigateToUpdateContact(Contact contact);
    void navigateToMapView(Contact contact);
}