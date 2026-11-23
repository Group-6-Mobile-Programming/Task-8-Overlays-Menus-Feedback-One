# UI Handoff Notes: Mukono NGO Delivery (Overlays, Menus and Feedback)

XML is in `app/src/main/res/`. The Java files only show the screens. All real logic is for the logic team.
Some small **TEMPORARY** click handlers are in the Java files so the UI can be tested.
They are marked with comments. Replace them with the real logic.

## Screens

| Design page | Layout | Java |
|---|---|---|
| Scan QR code | `activity_scan.xml` | `ScanActivity` (tap the frame to open the sheet) |
| Bottom sheet 1: beneficiary profile | `bottom_sheet_beneficiary.xml` | `BeneficiarySheetFragment` |
| Bottom sheet 2: report issue | `bottom_sheet_report_issue.xml` | `ReportIssueSheetFragment` |
| Tablet: map + side sheet | `layout-sw600dp/activity_coordinator.xml` (phones use `layout/`, full-width sheet) | `CoordinatorActivity` |
| Manifest (inside the side sheet) | `view_manifest_content.xml`, `item_manifest_row.xml` | none yet |

The app has two launcher icons: "Mukono Volunteer" (scan) and "Mukono Coordinator" (tablet map), so both can be tested.

## Bottom sheets (modal, slide up from the bottom)

- Both are `BottomSheetDialogFragment`s using the themes in `values/themes.xml`.
- **Sheet 1** opens at **half-screen height**. The ratio is `behavior_halfExpandedRatio=0.5` in `Widget.Mukono.BottomSheet.Half`.
  The fragment sets `STATE_HALF_EXPANDED` in `onStart`. The user can drag it up to full height or down to dismiss it.
- **Sheet 2** (Report Issue) is opened from sheet 1 and uses `ThemeOverlay.Mukono.BottomSheetDialog.Report`.

## Design considerations

**1. How to prevent accidental dismissal while filling a long Report Issue text field**
- The sheet style has `behavior_hideable=false` (no swipe-down to close) and `behavior_draggable=false`.
  This also stops the sheet from moving when the volunteer scrolls inside the text field.
- Touching outside the sheet is disabled with `windowCloseOnTouchOutside=false`.
- `setCancelable(false)` also blocks the Back button.
- The only exits are two clear buttons: **Submit Report** and **Cancel**.
- The content is inside a `NestedScrollView` with `adjustResize`, so the keyboard never hides the field or the buttons.
- Suggested for the logic team: keep the draft text in a ViewModel, and show a "Discard report?" dialog on Cancel if text was typed.

**2. Visual difference between the primary and secondary action**
- **Primary** (Confirm Delivery, Submit Report): solid green fill (`mk_green`), white bold text, and a check icon on Confirm Delivery.
- **Secondary** (Report Issue, Cancel): same size and shape, but outlined with a 2dp dark grey border, no fill, dark text, and a flag icon on Report Issue.
- It is not red, because reporting an issue is a normal action, not a dangerous one.
- Filled vs outlined makes the main action clear without colour alone, which also helps people with colour blindness.

## Side sheet (tablet)

- `sideSheet` uses `SideSheetBehavior` from Material. It slides in from the right edge, with `layout_gravity="end"`, and is 440dp wide on tablets.
- The map stays visible behind it, so coordinators do not leave the map view.
- Default state is hidden. Open it with `SideSheetBehavior.from(sideSheet).setState(STATE_EXPANDED)`.
  The "Delivery map" card (`cardMapTitle`) is a good place to tap to reopen it.
- Zone filter: `chipGroupZones` (single selection, All / Kisenyi / Katwe / Bwaise).
  Filter the list in `rvManifest` and update `tvManifestSubtitle` (for example "5 stops in Kisenyi").
- Reassign volunteer: `spinnerVolunteer` in every row. Use `R.layout.spinner_item` and `R.layout.spinner_dropdown_item` for its adapter.

## View IDs

| Where | IDs |
|---|---|
| Scan | `cameraContainer`, `btnBack`, `scanFrame`, `tvScanHint` |
| Beneficiary sheet | `tvInitials`, `tvBeneficiaryName`, `tvBeneficiaryRole`, `tvHouseholdSize`, `chipGroupDiet`, `btnConfirmDelivery`, `btnReportIssue` |
| Report sheet | `tvReportBeneficiary`, `rgIssueType` (`rbWrongPerson`, `rbAddressChanged`, `rbNoShow`), `tilDetails`, `etDetails`, `btnSubmitReport`, `btnCancelReport` |
| Coordinator | `mapContainer`, `cardMapTitle`, `sideSheet`, `btnCloseManifest`, `tvManifestSubtitle`, `chipGroupZones` (`chipZoneAll`, `chipZoneKisenyi`, `chipZoneKatwe`, `chipZoneBwaise`), `rvManifest` |
| Manifest row | `tvStopNumber`, `tvBeneficiaryNameRow`, `tvStatus`, `tvHouseholdRow`, `spinnerVolunteer`, `viewDivider` |

## Things to watch

1. Dietary restriction chips in the beneficiary sheet are sample values. Add or remove `Chip`s in `chipGroupDiet` from the data, using style `Mk.Chip.Diet`.
2. For a "Delivered" status in a manifest row: set `tvStatus` background to `@drawable/bg_pill_delivered`, text colour to `@color/mk_green_dark`, and add a check icon (`ic_check`) as `drawableStart`.
3. Hide `viewDivider` on the last row if you do not want a line at the bottom.
4. The map is a placeholder picture (`ic_map_placeholder`). Put the real map in `mapContainer`.
5. The camera preview goes into `cameraContainer`. The `scanFrame` sits on top of it.
6. In Android Studio's preview, the manifest list shows the same sample row repeated. Real rows come from the adapter.
7. The icons are my own simple outline icons. Replace them in `drawable/` if the designer sends official ones.
8. If you need a new view, ask the UI developer to add it to the XML instead of editing the layouts yourself.
