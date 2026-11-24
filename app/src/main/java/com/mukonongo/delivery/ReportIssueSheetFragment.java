package com.mukonongo.delivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

// Second bottom sheet: report an issue. It cannot be dismissed by dragging, tapping outside,
// or the back button, so a half-written report is not lost by accident.
// The only ways out are the Submit Report and Cancel buttons.
// UI skeleton: saving the report is added by the logic team.
public class ReportIssueSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ReportIssueSheet";

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Mukono_BottomSheetDialog_Report;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false); // blocks back button and outside touch
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_report_issue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Transition ends when sheet 2 draws its first frame
        PerfTracker.stopOnFirstDraw(view, PerfTracker.TRACE_REPORT_TRANSITION);

        // TEMPORARY wiring for testing the UI only
        view.findViewById(R.id.btnSubmitReport).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnCancelReport).setOnClickListener(v -> dismiss());
    }
}