package com.unity3d.play;

import static com.unity3d.play.Ember.ADMOB_INTERS;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdMobManager {
    private static final String TAG = "AdMobManager";
    private static InterstitialAd interstitialAd;
    private static boolean isAdLoading = false;

    public static void initializeAds(Activity activity) {
        MobileAds.initialize(activity, initializationStatus -> {
            Log.d(TAG, "AdMob berhasil diinisialisasi.");
            loadInterstitialAd(activity);
        });
    }

    public static void loadInterstitialAd(Activity activity) {
        if (interstitialAd != null || isAdLoading) {
            Log.d(TAG, "Iklan sedang dimuat atau sudah tersedia.");
            return;
        }

        isAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, ADMOB_INTERS, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd ad) {
                interstitialAd = ad;
                isAdLoading = false;
                Log.d(TAG, "Iklan interstitial berhasil dimuat.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(TAG, "Gagal memuat iklan: " + adError.getMessage());
                interstitialAd = null;
                isAdLoading = false;
            }
        });
    }

    public static void showInterstitialAd(Activity activity) {
        if (interstitialAd == null) {
            Log.e(TAG, "Iklan belum siap.");
            loadInterstitialAd(activity);
            return;
        }

        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Iklan ditutup, memuat ulang...");
                interstitialAd = null;
                loadInterstitialAd(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.e(TAG, "Gagal menampilkan iklan: " + adError.getMessage());
                interstitialAd = null;
                loadInterstitialAd(activity);
            }
        });

        interstitialAd.show(activity);
    }
}