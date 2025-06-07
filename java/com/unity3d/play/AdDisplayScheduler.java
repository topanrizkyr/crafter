package com.unity3d.play;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class AdDisplayScheduler {
    private static final String TAG = "AdDisplayScheduler";
    private static final Handler handler = new Handler();
    private static boolean isScheduled = false;

    public static final long DELAY_INTER_FIRST = 60_000;
    public static final long DELAY_INTER_REPEAT = 300_000;

    public static boolean isScheduled() {
        return isScheduled;
    }

    public static void startAdSchedule(Activity activity) {
        if (isScheduled) {
            Log.d(TAG, "Penjadwalan sudah dimulai, abaikan.");
            return;
        }
        Log.d(TAG, "Mulai penjadwalan iklan. Pertama dalam " + DELAY_INTER_FIRST + " ms");
        isScheduled = true;

        handler.postDelayed(() -> {
            Log.d(TAG, "Tayang iklan pertama (delay pertama)");
            AdMobManager.showInterstitialAd(activity);
            scheduleNextAd(activity);
        }, DELAY_INTER_FIRST);
    }

    private static void scheduleNextAd(Activity activity) {
        Log.d(TAG, "Menjadwalkan iklan berikutnya dalam " + DELAY_INTER_REPEAT + " ms");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Tayang iklan berikutnya (delay repeat)");
                AdMobManager.showInterstitialAd(activity);
                scheduleNextAd(activity);
            }
        }, DELAY_INTER_REPEAT);
    }

    public static void stopAdSchedule() {
        handler.removeCallbacksAndMessages(null);
        isScheduled = false;
        Log.d(TAG, "Penjadwalan iklan dihentikan.");
    }
}