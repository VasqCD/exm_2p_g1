package com.example.pm1e2p_grup1.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.model.Contact;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private final Context context;
    private final List<Contact> contacts;
    private final OnContactClickListener listener;

    // Interfaz para manejar clics en los elementos
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactAdapter(@NonNull Context context, List<Contact> contacts, OnContactClickListener listener) {
        super(context, 0, contacts);
        this.context = context;
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
        }

        Contact currentContact = contacts.get(position);

        TextView txtName = listItemView.findViewById(R.id.txtName);
        TextView txtPhone = listItemView.findViewById(R.id.txtPhone);

        txtName.setText(currentContact.getName());
        txtPhone.setText(currentContact.getPhone());

        // Configurar listener de clic
        listItemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(currentContact);
            }
        });

        return listItemView;
    }
}