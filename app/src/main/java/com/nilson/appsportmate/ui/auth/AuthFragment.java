package com.nilson.appsportmate.ui.auth;

import static com.nilson.appsportmate.common.utils.NavControllerExtensions.navigateWithAnimation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentAuthBinding;
import com.nilson.appsportmate.ui.shared.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthFragment extends BaseFragment {
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
        setStatusBarIconsLight(false);

        nav = Navigation.findNavController(view);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
       logInBtn = binding.btnLogin;
       signInBtn = binding.btnSignIn;
    }

    private void setupClickListeners() {
        logInBtn.setOnClickListener(view ->  navigateWithAnimation(nav,R.id.action_authFragment_to_loginFragment));
        signInBtn.setOnClickListener(view -> navigateWithAnimation(nav,R.id.action_authFragment_to_signInFragment));
    }
}