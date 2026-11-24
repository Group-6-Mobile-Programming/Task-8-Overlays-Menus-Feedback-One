package com.mukonongo.delivery;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.Choreographer;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Central helper for Firebase Performance custom traces.
 * Rules: a trace that is started but never stopped is NEVER reported.
 * Trace names: max 100 chars, no spaces. Max 5 attributes per trace.
 */
public final class PerfTracker {

    // ---- Journey traces (end-to-end) ----
    public static final String TRACE_SCAN_JOURNEY = "scan_to_profile_journey";   // QR detected -> sheet half-expanded
    public static final String TRACE_CONFIRM_DELIVERY = "confirm_delivery";      // tap -> action complete
    public static final String TRACE_REPORT_TRANSITION = "report_issue_transition"; // sheet 1 tap -> sheet 2 drawn
    public static final String TRACE_MANIFEST_LOAD = "manifest_load";            // screen open -> list drawn with data
    public static final String TRACE_SIDE_SHEET_OPEN = "side_sheet_open";        // setState(EXPANDED) -> expanded
    public static final String TRACE_MANIFEST_SCROLL = "manifest_scroll";        // one scroll gesture (jank metrics)

    // ---- QR scan step traces ----
    public static final String TRACE_CAMERA_INIT = "qr_camera_init";
    public static final String TRACE_QR_DECODE = "qr_decode";
    public static final String TRACE_BENEFICIARY_FETCH = "beneficiary_fetch";
    public static final String TRACE_SHEET_RENDER = "beneficiary_sheet_render";

    private static final ConcurrentHashMap<String, Trace> ACTIVE = new ConcurrentHashMap<>();
    private static Context appContext;

    private PerfTracker() {}

    /** Call once from each Activity's onCreate (safe to call repeatedly). */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    public static void start(String name) {
        Trace trace = FirebasePerformance.getInstance().newTrace(name);
        trace.putAttribute("network_type", networkType());
        trace.start();
        ACTIVE.put(name, trace); // a previous unfinished trace with this name is dropped (not reported)
    }

    public static void stop(String name) {
        Trace trace = ACTIVE.remove(name);
        if (trace != null) trace.stop();
    }

    public static void stop(String name, String attrKey, String attrValue) {
        Trace trace = ACTIVE.remove(name);
        if (trace != null) {
            trace.putAttribute(attrKey, attrValue);
            trace.stop();
        }
    }

    public static void putAttribute(String name, String key, String value) {
        Trace trace = ACTIVE.get(name);
        if (trace != null) trace.putAttribute(key, value);
    }

    /** Drop a trace without reporting it (user backed out, error path, etc). */
    public static void discard(String name) {
        ACTIVE.remove(name);
    }

    /** Stops the trace right before the view's next draw pass = "first frame with content". */
    public static void stopOnFirstDraw(@NonNull View view, @NonNull String traceName) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewTreeObserver observer = view.getViewTreeObserver();
                if (observer.isAlive()) observer.removeOnPreDrawListener(this);
                stop(traceName);
                return true;
            }
        });
    }

    /**
     * Jank tracker for a scrolling list. For every scroll gesture it reports one trace with:
     * total_frames, slow_frames, frozen_frames.
     * Frozen = frame took > 700 ms (same definition Firebase uses).
     * Slow = frame took > 1.25x the display's frame budget (adapts to 60/90/120 Hz screens).
     */
    public static void trackScrollJank(@NonNull RecyclerView list, @NonNull String traceName) {
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private Trace trace;
            private boolean running;
            private long lastFrameNanos;
            private int total, slow, frozen;
            private double slowThresholdMs;

            private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    if (lastFrameNanos != 0) {
                        double ms = (frameTimeNanos - lastFrameNanos) / 1_000_000.0;
                        total++;
                        if (ms > 700) frozen++;
                        else if (ms > slowThresholdMs) slow++;
                    }
                    lastFrameNanos = frameTimeNanos;
                    if (running) Choreographer.getInstance().postFrameCallback(this);
                }
            };

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && !running) {
                    Display display = rv.getDisplay();
                    float hz = display != null ? display.getRefreshRate() : 60f;
                    slowThresholdMs = (1000.0 / hz) * 1.25;
                    total = slow = frozen = 0;
                    lastFrameNanos = 0;
                    running = true;
                    trace = FirebasePerformance.getInstance().newTrace(traceName);
                    trace.putAttribute("network_type", networkType());
                    if (rv.getAdapter() != null) {
                        trace.putAttribute("item_count", String.valueOf(rv.getAdapter().getItemCount()));
                    }
                    trace.start();
                    Choreographer.getInstance().postFrameCallback(frameCallback);
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && running) {
                    running = false;
                    Choreographer.getInstance().removeFrameCallback(frameCallback);
                    trace.putMetric("total_frames", total);
                    trace.putMetric("slow_frames", slow);
                    trace.putMetric("frozen_frames", frozen);
                    trace.stop();
                    trace = null;
                }
            }
        });
    }

    private static String networkType() {
        if (appContext == null) return "unknown";
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = cm.getActiveNetwork();
            if (network == null) return "offline";
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps == null) return "offline";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "wifi";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "cellular";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "ethernet";
            return "other";
        } catch (SecurityException e) {
            return "unknown"; // ACCESS_NETWORK_STATE missing
        }
    }
}