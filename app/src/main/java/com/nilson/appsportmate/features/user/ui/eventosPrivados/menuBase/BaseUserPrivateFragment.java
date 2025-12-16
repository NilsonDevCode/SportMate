package com.nilson.appsportmate.features.user.ui.eventosPrivados.menuBase;

import android.app.AlertDialog;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;

public abstract class BaseUserPrivateFragment extends Fragment {

    protected void configurarMenuPrivado(@NonNull MaterialToolbar toolbar) {

        toolbar.setOnMenuItemClickListener(this::onMenuItemClicked);
    }

    private boolean onMenuItemClicked(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cambiar_ayuntamiento) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_seleccionarNuevoAyuntamientoFragment);
            return true;

        } else if (id == R.id.action_crear_evento_privado) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_crearEventoUserPrivateFragment);
            return true;

        } else if (id == R.id.action_ver_evento_privado) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_eventosDisponiblesUserPrivateFragment);
            return true;

        } else if (id == R.id.action_ver_eventos_creados_privado) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_verEventosApuntadoPrivateFragment);
            return true;

        } else if (id == R.id.action_cerrarSesion) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Seguro que quieres cerrar sesión?")
                    .setPositiveButton("Sí, salir", (d, w) -> cerrarSesion())
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        }

        return false;
    }

    private void cerrarSesion() {
        Preferencias.borrarTodo(requireContext());

        NavOptions opts = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();

        Navigation.findNavController(requireView())
                .navigate(R.id.authFragment, null, opts);
    }
}
