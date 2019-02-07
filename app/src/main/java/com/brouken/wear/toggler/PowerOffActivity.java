package com.brouken.wear.toggler;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

public class PowerOffActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();

        TogglerAccessibilityService.log("onStart");

        if (Common.isAccessibilityEnabled(this)) {
            TogglerAccessibilityService.scroll = true;
            TogglerAccessibilityService.tap = true;

            Intent intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
