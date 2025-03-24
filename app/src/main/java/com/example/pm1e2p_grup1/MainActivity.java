package com.example.pm1e2p_grup1;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pm1e2p_grup1.R;
import androidx.core.view.WindowCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar edge-to-edge (para dispositivos con notch o pantalla completa)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Configurar el contenido con nuestro layout de contacto
        setContentView(R.layout.activity_contacto);

        // Aplicar insets del sistema para manejar correctamente la barra de estado y navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Aquí se agregaría la lógica para inicializar los botones y eventos,
        // pero como es temporal solo para visualizar, no es necesario por ahora
    }
}