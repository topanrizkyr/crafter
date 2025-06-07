package com.mojang.minecraftpe;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.braze.Braze;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

public class MinecraftActivityLifecycleCallbackListener implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
//        BrazeInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(activity.getApplicationContext());
    }

    @Override
    public void onActivityResumed(Activity activity) {
//        BrazeInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
//        BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
//        Braze.INSTANCE.getInstance(activity.getApplicationContext()).closeSession(activity);
    }
}
