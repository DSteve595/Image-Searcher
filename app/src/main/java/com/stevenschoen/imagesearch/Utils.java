package com.stevenschoen.imagesearch;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

public class Utils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupFab(View floatingActionButton) {
        if (hasLollipop()) {
            floatingActionButton.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                }
            });
            floatingActionButton.setClipToOutline(true);
            floatingActionButton.setElevation(
                    floatingActionButton.getResources().getDimension(R.dimen.fab_elevation));
        }
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
