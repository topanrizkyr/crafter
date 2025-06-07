package com.mojang.minecraftpe.input;

import android.view.InputDevice;
import androidx.core.view.InputDeviceCompat;
import java.io.File;
import org.spongycastle.crypto.tls.CipherSuite;

public class InputCharacteristics {
    private static final int DUALSENSE_DEVICE_ID = 3302;
    private static final int SONY_VENDOR_ID = 1356;

    public static boolean allControllersHaveDoubleTriggers() {
        boolean z = false;
        for (int i : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(i);
            if (device != null && !device.isVirtual() && device.getControllerNumber() > 0 && (device.getSources() & InputDeviceCompat.SOURCE_GAMEPAD) != 0) {
                boolean[] hasKeys = device.hasKeys(102, 103, 104, CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256);
                boolean z2 = hasKeys.length == 4;
                int i2 = 0;
                while (true) {
                    if (i2 >= hasKeys.length) {
                        break;
                    }
                    if (!hasKeys[i2]) {
                        z2 = false;
                        break;
                    }
                    i2++;
                }
                if (!z2 && hasKeys[0] && hasKeys[1]) {
                    z2 = supportsAnalogTriggers(device);
                }
                z = (z2 && device.getName().contains("EI-GP20")) ? false : z2;
                if (!z) {
                    break;
                }
            }
        }
        return z;
    }

    public static boolean isCreteController(int i) {
        InputDevice device = InputDevice.getDevice(i);
        if (device != null && !device.isVirtual() && device.getControllerNumber() > 0 && (device.getSources() & InputDeviceCompat.SOURCE_GAMEPAD) != 0) {
            if ((device.getProductId() == 736) & (device.getVendorId() == 1118)) {
                String[] strArr = {"/system/usr/keylayout/Vendor_045e_Product_02e0.kl", "/data/system/devices/keylayout/Vendor_045e_Product_02e0.kl"};
                for (int i2 = 0; i2 < 2; i2++) {
                    if (new File(strArr[i2]).exists()) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean supportsAnalogTriggers(int i) {
        InputDevice device = InputDevice.getDevice(i);
        if (device != null) {
            return supportsAnalogTriggers(device);
        }
        return false;
    }

    private static boolean supportsAnalogTriggers(InputDevice inputDevice) {
        return (inputDevice.getMotionRange(17) != null || inputDevice.getMotionRange(23) != null) && (inputDevice.getMotionRange(18) != null || inputDevice.getMotionRange(22) != null);
    }

    public static boolean isXboxController(int i) {
        InputDevice device = InputDevice.getDevice(i);
        return device != null && (device.getSources() & InputDeviceCompat.SOURCE_GAMEPAD) == 1025 && device.getVendorId() == 1118;
    }

    public static boolean isPlaystationController(int i) {
        InputDevice device = InputDevice.getDevice(i);
        return device != null && (device.getSources() & InputDeviceCompat.SOURCE_GAMEPAD) == 1025 && device.getVendorId() == SONY_VENDOR_ID;
    }

    public static boolean isDualsenseController(int i) {
        InputDevice device = InputDevice.getDevice(i);
        return device != null && (device.getSources() & InputDeviceCompat.SOURCE_GAMEPAD) == 1025 && device.getVendorId() == SONY_VENDOR_ID && device.getProductId() == DUALSENSE_DEVICE_ID;
    }
}
