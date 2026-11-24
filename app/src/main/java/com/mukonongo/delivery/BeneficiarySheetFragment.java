package com.mukonongo.delivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
            BottomSheetBehavior<FrameLayout> behavior = ((BottomSheetDialog) getDialog()).getBehavior();

            // Journey ends when the sheet has finished expanding to half-screen.
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        PerfTracker.stop(PerfTracker.TRACE_SCAN_JOURNEY);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
            });

            // Half-screen height (ratio is set in styles.xml: behavior_halfExpandedRatio)
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Render time = sheet requested -> first draw with the profile content
        PerfTracker.stopOnFirstDraw(view, PerfTracker.TRACE_SHEET_RENDER);

        // TEMPORARY wiring for testing the UI only
        view.findViewById(R.id.btnConfirmDelivery).setOnClickListener(v -> {
            PerfTracker.start(PerfTracker.TRACE_CONFIRM_DELIVERY);
            dismiss();
            // TODO (logic team): move this stop() into the real action's success callback,
            // and use "error" in the failure callback. For now it stops instantly (~0 ms).
            PerfTracker.stop(PerfTracker.TRACE_CONFIRM_DELIVERY, "outcome", "success");
        });
        view.findViewById(R.id.btnReportIssue).setOnClickListener(v -> {
            PerfTracker.start(PerfTracker.TRACE_REPORT_TRANSITION); // stopped in ReportIssueSheetFragment
            dismiss();
            new ReportIssueSheetFragment().show(requireActivity().getSupportFragmentManager(),
                    ReportIssueSheetFragment.TAG);
        });
    }
}