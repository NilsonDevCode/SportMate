package com.nilson.appsportmate.caracteristicas.principal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.caracteristicas.autenticacion.ui.LoginActivity;
import com.nilson.appsportmate.caracteristicas.autenticacion.ui.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnIniciarSesion;
    private Button btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AppSportMate");
        }

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarse  = findViewById(R.id.boton1_ActivityRegister);

        btnIniciarSesion.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnRegistrarse.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
