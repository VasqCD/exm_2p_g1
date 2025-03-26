
package com.example.pm1e2p_grup1.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
        ImageView imgContact = listItemView.findViewById(R.id.imgContact);

        txtName.setText(currentContact.getName());
        txtPhone.setText(currentContact.getPhone());

        // Cargar imagen con Glide si hay una URL de foto disponible
        if (currentContact.getPhotoUrl() != null && !currentContact.getPhotoUrl().isEmpty()) {
            if (currentContact.getPhotoUrl().startsWith("http")) {
                // Si es una URL web
                Glide.with(context)
                        .load(currentContact.getPhotoUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.logo_utm)
                                .error(R.drawable.logo_utm)
                                .circleCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(imgContact);
            } else {
                // Si es base64
                try {
                    byte[] decodedString = Base64.decode(currentContact.getPhotoUrl(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    Glide.with(context)
                            .load(bitmap)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.logo_utm)
                                    .error(R.drawable.logo_utm)
                                    .circleCrop())
                            .into(imgContact);
                } catch (Exception e) {
                    imgContact.setImageResource(R.drawable.logo_utm);
                }
            }
        } else {
            imgContact.setImageResource(R.drawable.logo_utm);
        }

        // Configurar listener de clic
        listItemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(currentContact);
            }
        });

        return listItemView;
    }
}