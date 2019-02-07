package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;

public class ToggleTiltToWakeActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();

        if (Common.isAccessibilityEnabled(this)) {
            TogglerAccessibilityService.tap = true;

            Intent intent = new Intent("com.google.android.clockwork.settings.WRIST_GESTURE_SETTINGS_DIALOG");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
