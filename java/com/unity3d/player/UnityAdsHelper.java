package com.unity3d.player;

import static com.unity3d.play.Ember.AD_UNIT_ID;
import static com.unity3d.play.Ember.FIRST_AD_DELAY;
import static com.unity3d.play.Ember.REPEAT_AD_DELAY;
import static com.unity3d.play.Ember.TEST_MODE;
import static com.unity3d.play.Ember.UNITY_GAME_ID;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

public class UnityAdsHelper {

    private Handler handler = new Handler();
    private Activity activity;

    public UnityAdsHelper(Activity activity) {
        this.activity = activity;
    }

    public void initUnityAds() {
        UnityAds.initialize(activity, UNITY_GAME_ID, TEST_MODE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAndShowInterstitialAd();
            }
        }, FIRST_AD_DELAY);
    }

    private void loadAndShowInterstitialAd() {
        UnityAds.load(AD_UNIT_ID, loadListener);
    }

    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            if (activity != null) {
                UnityAds.show(activity, AD_UNIT_ID, new UnityAdsShowOptions(), showListener);
            }
        }

        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
        }
    };

    private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
        }

        @Override
        public void onUnityAdsShowStart(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsShowStart: " + placementId);
        }

        @Override
        public void onUnityAdsShowClick(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsShowClick: " + placementId);
        }

        @Override
        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
            scheduleNextAd();
        }
    };

    private void scheduleNextAd() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAndShowInterstitialAd();
            }
        }, REPEAT_AD_DELAY);
    }
}
