package com.mukonongo.delivery;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.sidesheet.SideSheetBehavior;

// UI skeleton. Map, manifest data, zone filtering and volunteer reassignment are added by the logic team.
public class CoordinatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator);

        // TEMPORARY wiring for testing the UI only: the side sheet opens at start,
        // the X button closes it, and tapping the "Delivery map" card opens it again.
        View sideSheet = findViewById(R.id.sideSheet);
        SideSheetBehavior<View> behavior = SideSheetBehavior.from(sideSheet);
        sideSheet.post(() -> behavior.setState(SideSheetBehavior.STATE_EXPANDED));
        findViewById(R.id.btnCloseManifest).setOnClickListener(v ->
                behavior.setState(SideSheetBehavior.STATE_HIDDEN));
        findViewById(R.id.cardMapTitle).setOnClickListener(v ->
                behavior.setState(SideSheetBehavior.STATE_EXPANDED));
    }
}
