package com.nilson.appsportmate.ui.splash;

import static com.nilson.appsportmate.common.utils.NavControllerExtensions.navigateWithAnimation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.nilson.appsportmate.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY_MS = 2000L; // 2s para ver el logo
    private boolean navigated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> goAuth(view), SPLASH_DELAY_MS);
    }

    private void goAuth(@NonNull View view) {
        if (!isAdded() || navigated) return;
        NavController nav = Navigation.findNavController(view);
        try {
            navigateWithAnimation(nav, R.id.action_splashFragment_to_authFragment);
            navigated = true;
        } catch (IllegalArgumentException ignore) {
            // acción no existe en el grafo → evita crash
        }
    }
}
