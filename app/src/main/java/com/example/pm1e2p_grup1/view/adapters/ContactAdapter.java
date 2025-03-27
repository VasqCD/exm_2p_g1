package com.example.pm1e2p_grup1.view.adapters;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.utils.Constants;
import com.example.pm1e2p_grup1.view.activities.MapActivity;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private final Context context;
    private final List<Contact> contacts;
    private final OnContactClickListener listener;
    private int selectedPosition = -1; // Ningún elemento seleccionado por defecto

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

        // Marcar elemento seleccionado
        if (position == selectedPosition) {
            listItemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_50));
        } else {
            listItemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // Configurar listener de clic
        listItemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(currentContact);
            }
        });

        // Configura long click para mostrar acciones adicionales (como ir a la ubicación)
        listItemView.setOnLongClickListener(v -> {
            showContactLocationDialog(currentContact);
            return true;
        });

        return listItemView;
    }

    // Método para mostrar diálogo de navegación a ubicación
    private void showContactLocationDialog(Contact contact) {
        String message = context.getString(R.string.location_message, contact.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.contact_action);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            // Navegar a la pantalla de mapa
            Intent intent = new Intent(context, MapActivity.class);
            intent.putExtra(Constants.EXTRA_CONTACT_NAME, contact.getName());
            intent.putExtra(Constants.EXTRA_LATITUDE, contact.getLatitude());
            intent.putExtra(Constants.EXTRA_LONGITUDE, contact.getLongitude());
            context.startActivity(intent);
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    // Métodos para manejar selección
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}