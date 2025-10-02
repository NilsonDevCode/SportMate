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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.R;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {

    private boolean navigated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lanzar precarga y después navegar
        prefetchThenNavigate(view);
    }

    private void prefetchThenNavigate(@NonNull View view) {
        if (!isAdded()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Colecciones que usarás más adelante (ajusta según tus necesidades)
        List<Task<?>> tasks = new ArrayList<>();
        tasks.add(db.collection("comunidades").get());
        tasks.add(db.collection("provincias").get());
        tasks.add(db.collection("ciudades").get());
        tasks.add(db.collection("pueblos").get());
        tasks.add(db.collection("deportes_ayuntamiento").get());

        // Esperar a que todas terminen
        Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> {
            if (!isAdded() || navigated) return;

            NavController nav = Navigation.findNavController(view);

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Log.i("SplashFragment", "No hay sesión → AuthFragment");
                safeNavigate(nav, R.id.action_splashFragment_to_authFragment);
            } else {
                Log.i("SplashFragment", "Sesión activa → AuthFragment (ajusta si quieres ir a Main directamente)");
                safeNavigate(nav, R.id.action_splashFragment_to_authFragment);
            }
        });
    }

    private void safeNavigate(@NonNull NavController nav, int actionId) {
        if (navigated) return;
        try {
            nav.navigate(actionId);
            navigated = true;
        } catch (IllegalArgumentException ignore) {
            // Acción inválida → ignora sin crashear
        }
    }
}
