package com.nilson.appsportmate.common.utils;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.nilson.appsportmate.R;

public class NavControllerExtensions {

    public static NavOptions getDefaultNavOptions() {
        return new NavOptions.Builder()
                .setEnterAnim(R.anim.nav_enter)
                .setExitAnim(R.anim.nav_exit)
                .setPopEnterAnim(R.anim.nav_pop_enter)
                .setPopExitAnim(R.anim.nav_pop_exit)
                .build();
    }

    public static void navigateWithAnimation(NavController navController, int destinationId) {
        navController.navigate(destinationId, null, getDefaultNavOptions());
    }
}
