package com.example.pm1e2p_grup1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView ivFotoPerfil;
    private Button btnTomarFoto, btnSalvarContacto, btnContactosSalvados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Ocultar la barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configurar el contenido con nuestro layout de contacto
        setContentView(R.layout.activity_contacto);

        // Inicializar vistas
        initViews();

        // Para esta versión inicial de visualización, no solicitamos permisos
        // ni agregamos funcionalidad a los botones
    }

    private void initViews() {
        try {
            ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
            btnTomarFoto = findViewById(R.id.btnTomarFoto);
            btnSalvarContacto = findViewById(R.id.btnSalvarContacto);
            btnContactosSalvados = findViewById(R.id.btnContactosSalvados);

            // Esta es una implementación temporal solo para mostrar la interfaz
            // Sin funcionalidad real
            btnTomarFoto.setOnClickListener(v -> {
                // Por ahora, simplemente no hacemos nada
            });

            btnSalvarContacto.setOnClickListener(v -> {
                // Por ahora, simplemente no hacemos nada
            });

            btnContactosSalvados.setOnClickListener(v -> {
                // Por ahora, simplemente no hacemos nada
            });
        } catch (Exception e) {
            e.printStackTrace();
            // En una app real, deberías manejar esta excepción apropiadamente
        }
    }
}