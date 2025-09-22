package com.nilson.appsportmate.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentAuthBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthFragment extends Fragment {
    private FragmentAuthBinding binding;

    private NavController nav;

    private Button logInBtn, signInBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nav = Navigation.findNavController(view);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
       logInBtn = binding.btnLogin;
       signInBtn = binding.btnSignIn;
    }

    private void setupClickListeners() {
        logInBtn.setOnClickListener(view -> {
            nav.navigate(R.id.action_authFragment_to_loginFragment);
        });

        signInBtn.setOnClickListener(view -> {
            nav.navigate(R.id.action_authFragment_to_signInFragment);
        });
    }
}