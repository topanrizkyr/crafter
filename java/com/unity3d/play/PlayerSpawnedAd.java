package com.unity3d.play;

import static com.unity3d.play.Ember.ADMOB_INTERS_DUA;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class PlayerSpawnedAd {
    private static final String TAG = "PlayerSpawnedAd";
    private static InterstitialAd mInterstitialAd;
    private static boolean isInterstitialLoading = false;

    public static void initializeAds(Context context) {
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "AdMob berhasil diinisialisasi.");
            loadInterstitial(context);
        });
    }

    public static void loadInterstitial(Context context) {
        if (mInterstitialAd != null || isInterstitialLoading) {
            Log.d(TAG, "Iklan sedang dimuat atau sudah tersedia.");
            return;
        }

        isInterstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, ADMOB_INTERS_DUA, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                isInterstitialLoading = false;
                Log.d(TAG, "Iklan interstitial berhasil dimuat.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(TAG, "Gagal memuat iklan: " + adError.getMessage());
                mInterstitialAd = null;
                isInterstitialLoading = false;
            }
        });
    }

    public static void showInterstitial(Context context) {
        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context bukan Activity, tidak bisa menampilkan iklan.");
            return;
        }

        Activity activity = (Activity) context;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Iklan ditutup, memuat ulang.");
                        mInterstitialAd = null;
                        loadInterstitial(context);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e(TAG, "Gagal menampilkan iklan: " + adError.getMessage());
                        mInterstitialAd = null;
                        loadInterstitial(context);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Iklan ditampilkan.");
                        mInterstitialAd = null;
                    }
                });

                Log.d(TAG, "Menampilkan iklan interstitial...");
                mInterstitialAd.show(activity);
            } else {
                Log.e(TAG, "Iklan belum siap, mencoba memuat ulang.");
                loadInterstitial(context);
            }
        });
    }
}