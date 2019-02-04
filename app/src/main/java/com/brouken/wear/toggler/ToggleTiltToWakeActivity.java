package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;

public class ToggleTiltToWakeActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();

        TogglerAccessibilityService.toggle = true;
        Intent intent = new Intent("com.google.android.clockwork.settings.WRIST_GESTURE_SETTINGS_DIALOG"); // Gestures w/ Tilt-to-wake
        startActivity(intent);
        finish();
    }
}
