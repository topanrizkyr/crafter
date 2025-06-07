package com.mojang.minecraftpe.input;

import android.content.Context;
import android.util.Log;

public abstract class InputDeviceManager {
    public abstract void register();

    public abstract void unregister();

    public static InputDeviceManager create(Context context) {
        return new JellyBeanDeviceManager(context);
    }

    public static class DefaultDeviceManager extends InputDeviceManager {
        private DefaultDeviceManager() {
        }

        @Override
        public void register() {
            Log.w("MCPE", "INPUT Noop register device manager");
        }

        @Override
        public void unregister() {
            Log.w("MCPE", "INPUT Noop unregister device manager");
        }
    }
}
