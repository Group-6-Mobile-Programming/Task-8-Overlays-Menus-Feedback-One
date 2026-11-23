package com.mukonongo.delivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

// Modal bottom sheet with the beneficiary profile. Opens at half-screen height.
// UI skeleton: data binding and the real Confirm Delivery action are added by the logic team.
public class BeneficiarySheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "BeneficiarySheet";

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Mukono_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_beneficiary, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            // Half-screen height (ratio is set in styles.xml: behavior_halfExpandedRatio)
            ((BottomSheetDialog) getDialog()).getBehavior()
                    .setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TEMPORARY wiring for testing the UI only
        view.findViewById(R.id.btnConfirmDelivery).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnReportIssue).setOnClickListener(v -> {
            dismiss();
            new ReportIssueSheetFragment().show(requireActivity().getSupportFragmentManager(),
                    ReportIssueSheetFragment.TAG);
        });
    }
}
