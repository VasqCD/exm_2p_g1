
package com.example.pm1e2p_grup1.view.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pm1e2p_grup1.R;
import com.example.pm1e2p_grup1.model.Contact;
import com.example.pm1e2p_grup1.presenter.ContactListPresenter;
import com.example.pm1e2p_grup1.view.adapters.ContactAdapter;
import com.example.pm1e2p_grup1.view.interfaces.ContactListView;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity implements ContactListView, ContactAdapter.OnContactClickListener {

    private ListView listViewContacts;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ContactAdapter adapter;
    private List<Contact> contactList;
    private ContactListPresenter presenter;

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

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.loadContacts();
        });

        // Cargar contactos al iniciar
        presenter.loadContacts();
    }

    private void initViews() {
        listViewContacts = findViewById(R.id.listViewContacts);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Configurar la barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lista de Contactos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

        textViewEmpty.setVisibility(View.GONE);
        listViewContacts.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyView() {
        textViewEmpty.setVisibility(View.VISIBLE);
        listViewContacts.setVisibility(View.GONE);
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

        if (contactList.isEmpty()) {
            showEmptyView();
        }
    }

    @Override
    public void onContactClick(Contact contact) {
        // Aquí puedes implementar la lógica para mostrar detalles del contacto o acciones
        // Por ejemplo, mostrar un diálogo con opciones para editar o eliminar
        showContactOptions(contact);
    }

    private void showContactOptions(Contact contact) {
        String[] options = {"Ver detalles", "Eliminar"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Opciones para " + contact.getName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Ver detalles
                    // Implementar la navegación a una pantalla de detalles
                    // Por ahora, solo mostraremos un mensaje
                    showMessage("Ver detalles de: " + contact.getName());
                    break;
                case 1: // Eliminar
                    confirmDeleteContact(contact);
                    break;
            }
        });
        builder.show();
    }

    private void confirmDeleteContact(Contact contact) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Está seguro que desea eliminar a " + contact.getName() + "?");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            presenter.deleteContact(contact);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}