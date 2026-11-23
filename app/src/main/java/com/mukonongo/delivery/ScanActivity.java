package com.mukonongo.delivery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI skeleton. Camera, QR scanning and loading the beneficiary are added by the logic team.
public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // TEMPORARY, for testing the UI only: tap the scan frame to open the beneficiary sheet.
        // The logic team replaces this with the real scan result.
        findViewById(R.id.scanFrame).setOnClickListener(v ->
                new BeneficiarySheetFragment().show(getSupportFragmentManager(), BeneficiarySheetFragment.TAG));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
