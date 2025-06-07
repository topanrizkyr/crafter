package com.mojang.minecraftpe.input;

import android.content.Context;
import android.hardware.input.InputManager;

public class JellyBeanDeviceManager extends InputDeviceManager implements InputManager.InputDeviceListener {
    private final InputManager inputManager;

    native void onInputDeviceAddedNative(int i);

    native void onInputDeviceChangedNative(int i);

    native void onInputDeviceRemovedNative(int i);

    native void setControllerDetailsNative(int i, boolean z, boolean z2);

    native void setDoubleTriggersSupportedNative(boolean z);

    native void setFoundDualsenseControllerNative(boolean z);

    native void setFoundPlaystationControllerNative(boolean z);

    native void setFoundXboxControllerNative(boolean z);

    JellyBeanDeviceManager(Context context) {
        this.inputManager = (InputManager) context.getSystemService("input");
    }

    @Override
    public void register() {
        int[] inputDeviceIds = this.inputManager.getInputDeviceIds();
        this.inputManager.registerInputDeviceListener(this, null);
        setDoubleTriggersSupportedNative(InputCharacteristics.allControllersHaveDoubleTriggers());
        for (int i = 0; i < inputDeviceIds.length; i++) {
            int i2 = inputDeviceIds[i];
            setControllerDetailsNative(i2, InputCharacteristics.isCreteController(i2), InputCharacteristics.supportsAnalogTriggers(inputDeviceIds[i]));
        }
        checkForXboxAndPlaystationController();
    }

    @Override
    public void unregister() {
        this.inputManager.unregisterInputDeviceListener(this);
    }

    @Override
    public void onInputDeviceAdded(int i) {
        onInputDeviceAddedNative(i);
        setDoubleTriggersSupportedNative(InputCharacteristics.allControllersHaveDoubleTriggers());
        setControllerDetailsNative(i, InputCharacteristics.isCreteController(i), InputCharacteristics.supportsAnalogTriggers(i));
        if (InputCharacteristics.isXboxController(i)) {
            setFoundXboxControllerNative(true);
        } else if (InputCharacteristics.isPlaystationController(i)) {
            setFoundPlaystationControllerNative(true);
            if (InputCharacteristics.isDualsenseController(i)) {
                setFoundDualsenseControllerNative(true);
            }
        }
    }

    @Override
    public void onInputDeviceChanged(int i) {
        onInputDeviceChangedNative(i);
        setDoubleTriggersSupportedNative(InputCharacteristics.allControllersHaveDoubleTriggers());
        setControllerDetailsNative(i, InputCharacteristics.isCreteController(i), InputCharacteristics.supportsAnalogTriggers(i));
        checkForXboxAndPlaystationController();
    }

    @Override
    public void onInputDeviceRemoved(int i) {
        onInputDeviceRemovedNative(i);
        setDoubleTriggersSupportedNative(InputCharacteristics.allControllersHaveDoubleTriggers());
        setControllerDetailsNative(i, InputCharacteristics.isCreteController(i), InputCharacteristics.supportsAnalogTriggers(i));
        checkForXboxAndPlaystationController();
    }

    public void checkForXboxAndPlaystationController() {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        for (int i : this.inputManager.getInputDeviceIds()) {
            z |= InputCharacteristics.isXboxController(i);
            z2 |= InputCharacteristics.isPlaystationController(i);
            z3 |= InputCharacteristics.isDualsenseController(i);
            if (z && z2) {
                break;
            }
        }
        setFoundXboxControllerNative(z);
        setFoundPlaystationControllerNative(z2);
        setFoundDualsenseControllerNative(z3);
    }
}
