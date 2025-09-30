package com.nilson.appsportmate.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.domain.models.AuthRole;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY_MS = 4000L; // 4 segundos

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Espera 4 segundos mostrando fragment_splash.xml antes de navegar
        new Handler(Looper.getMainLooper()).postDelayed(() -> checkAuth(view), SPLASH_DELAY_MS);
    }

    private void checkAuth(View view) {
        if (!isAdded()) return; // seguridad: evita crash si el fragment ya no está

        NavController nav = Navigation.findNavController(view);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            nav.navigate(R.id.action_splashFragment_to_authFragment);
            Log.i("SplashFragment", "The user is not logged in. Navigating to Auth...");
            return;
        }

        AuthRole role = AuthRole.USER; // TODO: Change to get the role from the device
        Log.i("SplashFragment", "Logged as " + role.toString());

        switch (role) {
            case USER:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
            case TOWNHALL:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
            default:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
        }
    }
}
