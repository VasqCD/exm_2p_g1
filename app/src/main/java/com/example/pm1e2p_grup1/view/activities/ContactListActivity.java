package com.example.pm1e2p_grup1.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.presenter.ContactListPresenter;
import com.example.pm1e2p_grup1.utils.Constants;
import com.example.pm1e2p_grup1.view.adapters.ContactAdapter;
import com.example.pm1e2p_grup1.view.interfaces.ContactListView;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity implements ContactListView, ContactAdapter.OnContactClickListener {

    private ListView listViewContacts;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private Button btnDeleteContact, btnUpdateContact;

    private ContactAdapter adapter;
    private List<Contact> contactList;
    private ContactListPresenter presenter;
    private Contact selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // Inicializar vistas
        initViews();

        // Inicializar presenter
        presenter = new ContactListPresenter(this, this);

        // Configurar adapter
        contactList = new ArrayList<>();
        adapter = new ContactAdapter(this, contactList, this);
        listViewContacts.setAdapter(adapter);

        // Configurar listeners
        setupListeners();

        // Cargar contactos al iniciar
        presenter.loadContacts();
    }

    private void initViews() {
        listViewContacts = findViewById(R.id.listViewContacts);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
        btnDeleteContact = findViewById(R.id.btnDeleteContact);
        btnUpdateContact = findViewById(R.id.btnUpdateContact);

        // Configurar la barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lista de Contactos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Deshabilitar botones inicialmente
        setButtonsEnabled(false);
    }

    private void setupListeners() {
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.loadContacts();
            clearSelection();
        });

        // Configurar búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.filterContacts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.filterContacts(newText);
                return true;
            }
        });

        // Configurar botones
        btnDeleteContact.setOnClickListener(v -> {
            if (selectedContact != null) {
                confirmDeleteContact(selectedContact);
            } else {
                showMessage("Seleccione un contacto primero");
            }
        });

        btnUpdateContact.setOnClickListener(v -> {
            if (selectedContact != null) {
                presenter.selectContactForEdit(selectedContact);
            } else {
                showMessage("Seleccione un contacto primero");
            }
        });
    }

    // Método para habilitar/deshabilitar botones
    private void setButtonsEnabled(boolean enabled) {
        btnDeleteContact.setEnabled(enabled);
        btnUpdateContact.setEnabled(enabled);

        // Opcional: cambiar opacidad para indicar visualmente el estado
        float alpha = enabled ? 1.0f : 0.5f;
        btnDeleteContact.setAlpha(alpha);
        btnUpdateContact.setAlpha(alpha);
    }

    private void clearSelection() {
        selectedContact = null;
        if (adapter != null) {
            adapter.setSelectedPosition(-1);
            adapter.notifyDataSetChanged();
        }
        setButtonsEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showContacts(List<Contact> contacts) {
        contactList.clear();
        contactList.addAll(contacts);
        adapter.notifyDataSetChanged();

        clearSelection();
        textViewEmpty.setVisibility(View.GONE);
        listViewContacts.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyView() {
        textViewEmpty.setVisibility(View.VISIBLE);
        listViewContacts.setVisibility(View.GONE);
        clearSelection();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContactDeleted(Contact contact) {
        contactList.remove(contact);
        adapter.notifyDataSetChanged();

        clearSelection();

        if (contactList.isEmpty()) {
            showEmptyView();
        }
    }

    @Override
    public void onContactSelected(Contact contact) {
        this.selectedContact = contact;
        setButtonsEnabled(true);
    }

    @Override
    public void filterContacts(String query) {
        presenter.filterContacts(query);
    }

    @Override
    public void navigateToUpdateContact(Contact contact) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_UPDATE);
        intent.putExtra(Constants.EXTRA_CONTACT_ID, contact.getId());
        intent.putExtra(Constants.EXTRA_CONTACT_NAME, contact.getName());
        intent.putExtra(Constants.EXTRA_PHONE, contact.getPhone());
        intent.putExtra(Constants.EXTRA_LATITUDE, contact.getLatitude());
        intent.putExtra(Constants.EXTRA_LONGITUDE, contact.getLongitude());
        // Nota: no podemos pasar la foto directamente, pero se obtendrá del servidor
        startActivityForResult(intent, Constants.REQUEST_UPDATE_CONTACT);
    }

    @Override
    public void navigateToMapView(Contact contact) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.EXTRA_CONTACT_NAME, contact.getName());
        intent.putExtra(Constants.EXTRA_LATITUDE, contact.getLatitude());
        intent.putExtra(Constants.EXTRA_LONGITUDE, contact.getLongitude());
        startActivity(intent);
    }

    @Override
    public void onContactClick(Contact contact) {
        // Actualizar el estado de selección en el adaptador
        adapter.setSelectedPosition(contactList.indexOf(contact));
        adapter.notifyDataSetChanged();

        // Guardar el contacto seleccionado y habilitar botones
        selectedContact = contact;
        setButtonsEnabled(true);

        showLocationDialog(contact);
    }

    // Método para mostrar diálogo de confirmación para ir a ubicación
    private void showLocationDialog(Contact contact) {
        String message = getString(R.string.location_message, contact.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.contact_action);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            presenter.selectContactForMap(contact);
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void confirmDeleteContact(Contact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Está seguro que desea eliminar a " + contact.getName() + "?");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            presenter.deleteContact(contact);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_UPDATE_CONTACT && resultCode == RESULT_OK) {
            // Recargar la lista después de una actualización exitosa
            presenter.loadContacts();
            showMessage("Contacto actualizado correctamente");
        }
    }
}