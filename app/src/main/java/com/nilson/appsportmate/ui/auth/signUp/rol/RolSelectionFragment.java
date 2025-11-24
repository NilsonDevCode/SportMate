package com.nilson.appsportmate.ui.auth.signUp.rol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentRoleSelectionBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RolSelectionFragment extends Fragment {

    private FragmentRoleSelectionBinding binding;
    private RolSelectionViewModel viewModel;

    public RolSelectionFragment() {
        super(R.layout.fragment_role_selection);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoleSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RolSelectionViewModel.class);

        MaterialButton btnRolUsuario = binding.btnRolUsuario;
        MaterialButton btnRolAyuntamiento = binding.btnRolAyuntamiento;

        // → Usuario estándar
        btnRolUsuario.setOnClickListener(v -> {
            viewModel.selectRole(UserRol.STANDARD);
            NavHostFragment.findNavController(RolSelectionFragment.this)
                    .navigate(R.id.action_rolSelectionFragment_to_signInFragment);
        });

        // → Ayuntamiento
        btnRolAyuntamiento.setOnClickListener(v -> {
            viewModel.selectRole(UserRol.TOWNHALL);
            NavHostFragment.findNavController(RolSelectionFragment.this)
                    .navigate(R.id.action_rolSelectionFragment_to_signInFragment);
        });
    }
}
