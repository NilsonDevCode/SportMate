package com.nilson.appsportmate.caracteristicas.principal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.caracteristicas.ayuntamiento.ui.GestionDeportesAyuntamientoActivity;
import com.nilson.appsportmate.caracteristicas.usuario.ui.DeportesDisponiblesActivity;
import com.nilson.appsportmate.comun.utilidades.Preferencias;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        // Si hay sesión iniciada, redirige por rol guardado; si no, a MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String rol = Preferencias.obtenerRol(this);
            if (rol != null) {
                rol = rol.trim().toLowerCase();
                if ("ayuntamiento".equals(rol)) {
                    startActivity(new Intent(this, GestionDeportesAyuntamientoActivity.class));
                    finish();
                    return;
                } else if ("usuario".equals(rol)) {
                    startActivity(new Intent(this, DeportesDisponiblesActivity.class));
                    finish();
                    return;
                }
            }
        }
        // Sin sesión o sin rol -> pantalla principal
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
