package com.mojang.minecraftpe.store.googleplay;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import com.google.common.base.Ascii;
import com.googleplay.licensing.AESObfuscator;
import com.googleplay.licensing.LicenseChecker;
import com.googleplay.licensing.LicenseCheckerCallback;
import com.googleplay.licensing.ServerManagedPolicy;
import com.mojang.minecraftpe.ActivityListener;
import com.mojang.minecraftpe.MainActivity;
import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;

public class GooglePlayStore implements Store, ActivityListener {
    private static final byte[] SALT = {75, 1, -16, -127, 42, 49, 19, -102, -88, 56, 121, 99, Ascii.ETB, -24, -18, -111, -11, 33, -62, 87};
    private static boolean mReceivedLicenseResponse = false;
    private static boolean mVerifiedLicense = true;
    MainActivity mActivity;
    GooglePlayBillingImpl mBillingImpl;
    private LicenseChecker mChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    StoreListener mListener;
    private ServerManagedPolicy mPolicy;
    int mPurchaseRequestCode;

    @Override
    public String getProductSkuPrefix() {
        return "";
    }

    @Override
    public String getRealmsSkuPrefix() {
        return "";
    }

    @Override
    public String getStoreId() {
        return "android.googleplay";
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onStop() {
    }

    private synchronized boolean hasReceivedLicenseResponse() {
        return mReceivedLicenseResponse;
    }

    private synchronized void updateLicenseStatus(boolean z, boolean z2) {
        mVerifiedLicense = z2;
        mReceivedLicenseResponse = z;
    }

    private class MinecraftLicenseCheckerCallback implements LicenseCheckerCallback {
        private MinecraftLicenseCheckerCallback() {
        }

        @Override
        public void allow(int i) {
            String str;
            if (i == 291) {
                str = new String("RETRY");
            } else if (i == 256) {
                str = new String("LICENSED");
            } else {
                str = new String("UNKNOWN REASON");
            }
            GooglePlayStore.this.updateLicenseStatus(true, true);
            Log.i("MinecraftLicenseCheckerCallback", String.format("allowed reason: %s", str));
        }

        @Override
        public void dontAllow(int i) {
            String str;
            if (i == 561) {
                str = new String("NOT LICENSED");
            } else if (i == 291) {
                str = new String("RETRY");
            } else {
                str = new String("UNKNOWN REASON");
            }
            GooglePlayStore.this.updateLicenseStatus(true, false);
            Log.i("MinecraftLicenseCheckerCallback", String.format("denied reason: %s", str));
        }

        @Override
        public void applicationError(int i) {
            GooglePlayStore.this.updateLicenseStatus(true, false);
            Log.i("MinecraftLicenseCheckerCallback", String.format("error: %d", Integer.valueOf(i)));
        }
    }

    public GooglePlayStore(MainActivity mainActivity, String str, StoreListener storeListener) {
        this.mListener = storeListener;
        this.mActivity = mainActivity;
        mainActivity.addListener(this);
        this.mBillingImpl = new GooglePlayBillingImpl(this.mActivity, this.mListener, str);
        this.mPurchaseRequestCode = MainActivity.RESULT_GOOGLEPLAY_PURCHASE;
        this.mPolicy = new ServerManagedPolicy(mainActivity, new AESObfuscator(SALT, this.mActivity.getPackageName(), Settings.Secure.getString(this.mActivity.getContentResolver(), "android_id")));
        this.mLicenseCheckerCallback = new MinecraftLicenseCheckerCallback();
        LicenseChecker licenseChecker = new LicenseChecker(mainActivity, this.mPolicy, str);
        this.mChecker = licenseChecker;
        licenseChecker.checkAccess(this.mLicenseCheckerCallback);
        if (this.mActivity.isEduMode()) {
            mReceivedLicenseResponse = true;
            mVerifiedLicense = true;
        }
    }

    @Override
    public boolean hasVerifiedLicense() {
        return mVerifiedLicense;
    }

    @Override
    public boolean receivedLicenseResponse() {
        return hasReceivedLicenseResponse();
    }

    @Override
    public ExtraLicenseResponseData getExtraLicenseData() {
        long[] extraLicenseData = this.mPolicy.getExtraLicenseData();
        return new ExtraLicenseResponseData(extraLicenseData[0], extraLicenseData[1], extraLicenseData[2]);
    }

    @Override
    public void queryProducts(final String[] strArr) {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GooglePlayStore.this.mBillingImpl.queryProducts(strArr);
            }
        });
    }

    @Override
    public void purchase(final String str, final boolean z, final String str2) {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (z) {
                    GooglePlayStore.this.mBillingImpl.launchSubscriptionPurchaseFlow(GooglePlayStore.this.mActivity, str, str2);
                } else {
                    GooglePlayStore.this.mBillingImpl.launchInAppPurchaseFlow(GooglePlayStore.this.mActivity, str, str2);
                }
            }
        });
    }

    @Override
    public void purchaseGame() {
        this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://market.android.com/details?id=" + this.mActivity.getPackageName())));
    }

    @Override
    public void acknowledgePurchase(final String str, String str2) {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GooglePlayStore.this.mBillingImpl.consumeOrAckPurchase(str);
            }
        });
    }

    @Override
    public void queryPurchases() {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GooglePlayStore.this.mBillingImpl.queryPurchases();
            }
        });
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        Log.v("GooglePlayStore", "onActivityResult hit");
    }

    @Override
    public void onDestroy() {
        this.mActivity.removeListener(this);
    }

    @Override
    public void destructor() {
        onDestroy();
    }
}
