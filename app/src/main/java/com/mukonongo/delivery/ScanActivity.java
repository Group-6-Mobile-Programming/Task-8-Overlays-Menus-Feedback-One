package com.mukonongo.delivery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI skeleton. Camera, QR scanning and loading the beneficiary are added by the logic team.
public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        PerfTracker.init(this);

        // Camera init: starts when the screen opens.
        // TODO (logic team): call onCameraPreviewReady() when the camera preview is actually showing.
        PerfTracker.start(PerfTracker.TRACE_CAMERA_INIT);

        // TEMPORARY, for testing the UI only: tap the scan frame to simulate a full scan.
        // The logic team replaces this with the real scan result.
        findViewById(R.id.scanFrame).setOnClickListener(v -> {
            onCameraPreviewReady();   // fake: remove when real camera exists
            onQrDetected();           // fake: call when the analyzer first sees a QR code
            onQrDecoded();            // fake: call when the decoder returns the text
            onBeneficiaryLoaded();    // fake: call when the beneficiary data is back
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ---- Performance hooks: call these from the real scan logic ----

    /** Camera preview is visible and ready to scan. */
    private void onCameraPreviewReady() {
        PerfTracker.stop(PerfTracker.TRACE_CAMERA_INIT);
    }

    /** A QR code was just detected in a frame. Starts the end-to-end journey + decode timing. */
    private void onQrDetected() {
        PerfTracker.start(PerfTracker.TRACE_SCAN_JOURNEY);
        PerfTracker.start(PerfTracker.TRACE_QR_DECODE);
    }

    /** Decoder finished and returned the beneficiary ID. Starts the network fetch timing. */
    private void onQrDecoded() {
        PerfTracker.stop(PerfTracker.TRACE_QR_DECODE);
        PerfTracker.start(PerfTracker.TRACE_BENEFICIARY_FETCH);
    }

    /** Beneficiary data arrived; show the profile sheet. */
    private void onBeneficiaryLoaded() {
        PerfTracker.stop(PerfTracker.TRACE_BENEFICIARY_FETCH, "result", "success");
        PerfTracker.start(PerfTracker.TRACE_SHEET_RENDER); // stopped in BeneficiarySheetFragment on first draw
        new BeneficiarySheetFragment().show(getSupportFragmentManager(), BeneficiarySheetFragment.TAG);
    }

    /** Fetch failed (timeout, no network, 404...). Records the failure and drops the journey. */
    private void onBeneficiaryFetchFailed() {
        PerfTracker.stop(PerfTracker.TRACE_BENEFICIARY_FETCH, "result", "error");
        PerfTracker.discard(PerfTracker.TRACE_SCAN_JOURNEY);
    }

    @Override
    protected void onDestroy() {
        // Never leave half-finished traces around if the user leaves mid-scan
        PerfTracker.discard(PerfTracker.TRACE_CAMERA_INIT);
        PerfTracker.discard(PerfTracker.TRACE_QR_DECODE);
        PerfTracker.discard(PerfTracker.TRACE_BENEFICIARY_FETCH);
        super.onDestroy();
    }
}