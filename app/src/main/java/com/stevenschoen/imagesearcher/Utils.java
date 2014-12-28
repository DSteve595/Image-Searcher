package com.stevenschoen.imagesearcher;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.util.Locale;

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

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1)
                + "";
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String wrapInBingQuotes(String string) {
//        Thanks, Microsoft. Super intuitive.
        return "'" + string + "'";
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
