package com.nilson.appsportmate.ui.splash;

import android.os.Bundle;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkAuth(view);
    }

    private void checkAuth(View view) {
        NavController nav = Navigation.findNavController(view);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            nav.navigate(R.id.action_splashFragment_to_authFragment);
            Log.i("SplashFragment", "The user is not logged in. Navigating to Auth...");
            return;
        }

        AuthRole role = AuthRole.USER; // TODO: Change to get the role from the device
        Log.i("SplashFragment", "Logged as " + role.toString());

        switch (role) {
            case AuthRole.USER:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
            case AuthRole.TOWNHALL:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
            default:
                nav.navigate(R.id.action_splashFragment_to_authFragment);
                break;
        }
    }
}