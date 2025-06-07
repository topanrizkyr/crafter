package com.mojang.minecraftpe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ThermalMonitor extends BroadcastReceiver {
    private Context mContext;
    private boolean mLowPowerModeEnabled = false;

    public ThermalMonitor(Context context) {
        this.mContext = context;
        context.registerReceiver(this, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
        readPowerMode(context);
    }

    protected void finalize() {
        this.mContext.unregisterReceiver(this);
    }

    public boolean getLowPowerModeEnabled() {
        return this.mLowPowerModeEnabled;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        readPowerMode(context);
    }

    private void readPowerMode(Context context) {
        this.mLowPowerModeEnabled = ((PowerManager) context.getSystemService("power")).isPowerSaveMode();
    }
}
