package com.mukonongo.delivery;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;

// UI skeleton. Map, manifest data, zone filtering and volunteer reassignment are added by the logic team.
public class CoordinatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator);

        PerfTracker.init(this);

        // Manifest load: screen open -> list drawn with data. Stopped in onManifestLoaded().
        PerfTracker.start(PerfTracker.TRACE_MANIFEST_LOAD);

        // TEMPORARY wiring for testing the UI only: the side sheet opens at start,
        // the X button closes it, and tapping the "Delivery map" card opens it again.
        View sideSheet = findViewById(R.id.sideSheet);
        SideSheetBehavior<View> behavior = SideSheetBehavior.from(sideSheet);

        behavior.addCallback(new SideSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View sheet, int newState) {
                if (newState == SideSheetBehavior.STATE_EXPANDED) {
                    PerfTracker.stop(PerfTracker.TRACE_SIDE_SHEET_OPEN);
                }
            }

            @Override
            public void onSlide(@NonNull View sheet, float slideOffset) { }
        });

        sideSheet.post(() -> {
            PerfTracker.start(PerfTracker.TRACE_SIDE_SHEET_OPEN);
            behavior.setState(SideSheetBehavior.STATE_EXPANDED);
        });
        findViewById(R.id.btnCloseManifest).setOnClickListener(v ->
                behavior.setState(SideSheetBehavior.STATE_HIDDEN));
        findViewById(R.id.cardMapTitle).setOnClickListener(v -> {
            if (behavior.getState() != SideSheetBehavior.STATE_EXPANDED) {
                PerfTracker.start(PerfTracker.TRACE_SIDE_SHEET_OPEN);
                behavior.setState(SideSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    // ---- Performance hooks: call these from the real manifest code ----

    /** Call ONCE right after the manifest RecyclerView is set up. Tracks slow/frozen frames per scroll. */
    private void trackManifestScroll(RecyclerView manifestList) {
        PerfTracker.trackScrollJank(manifestList, PerfTracker.TRACE_MANIFEST_SCROLL);
    }

    /** Call right after the adapter receives the manifest data. */
    private void onManifestLoaded(RecyclerView manifestList, int itemCount) {
        PerfTracker.putAttribute(PerfTracker.TRACE_MANIFEST_LOAD, "item_count", String.valueOf(itemCount));
        PerfTracker.stopOnFirstDraw(manifestList, PerfTracker.TRACE_MANIFEST_LOAD);
    }

    @Override
    protected void onDestroy() {
        PerfTracker.discard(PerfTracker.TRACE_MANIFEST_LOAD);
        super.onDestroy();
    }
}