package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class Common {
    public static Boolean isAccessibilityEnabled(final Activity activity) {
        if (TogglerAccessibilityService.running)
            return true;

        final Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        activity.startActivity(intent);

        Toast.makeText(activity, "Enable Toggler in Accessibility to continue", Toast.LENGTH_LONG).show();

        activity.finish();

        return false;
    }
}
