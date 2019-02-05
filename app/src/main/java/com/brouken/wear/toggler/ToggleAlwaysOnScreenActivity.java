package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

public class ToggleAlwaysOnScreenActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();

        if (Common.isAccessibilityEnabled(this)) {
            TogglerAccessibilityService.scroll = true;
            TogglerAccessibilityService.toggle = true;

            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
            finish();
        }
    }
}
