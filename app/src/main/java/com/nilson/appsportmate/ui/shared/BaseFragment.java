package com.nilson.appsportmate.ui.shared;

import android.graphics.Color;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected void setStatusBarIconsLight(boolean isLight) {
        if (getActivity() == null) return;

        ///  Allows to draw behind the status bar
        WindowCompat.setDecorFitsSystemWindows(
                getActivity().getWindow(),
                false);

        ///  Set the status bar to transparent
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        ///  Change the status bar icons color
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getActivity().getWindow(),
                getActivity().getWindow().getDecorView());

        ///  True = dark icons, False = light icons
        controller.setAppearanceLightStatusBars(isLight);

    }
}
