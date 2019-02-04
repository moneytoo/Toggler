package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

public class ToggleAlwaysOnScreenActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();

        TogglerAccessibilityService.run = true;
        Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS); // Display w/ Always-on screen
        startActivity(intent);
        finish();
    }
}
