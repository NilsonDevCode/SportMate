package com.nilson.appsportmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

// private Button btnIniciarSesion;
// private Button btnRegistrarse;

// btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
// btnRegistrarse  = findViewById(R.id.boton1_ActivityRegister);
//
//         btnIniciarSesion.setOnClickListener(v ->
// startActivity(new Intent(this, LoginActivity.class)));
//
//         btnRegistrarse.setOnClickListener(v ->
// startActivity(new Intent(this, RegisterActivity.class)));
