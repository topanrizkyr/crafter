package com.unity3d.myads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mojang.minecraftpe.MainActivity;

public class Starter extends Activity {
    @Override
    public void onCreate(Bundle app) {
        super.onCreate(app);
        startApp();

    }

    public void startApp() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.setFlags(131072);
        startActivityIfNeeded(mainActivity, 0);
    }
}
