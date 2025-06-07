package com.mojang.minecraftpe;

import android.content.Intent;
import android.net.Uri;

public class Minecraft_Market_Demo extends MainActivity {
    @Override
    protected boolean isDemo() {
        return true;
    }

    @Override
    public void buyGame() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.mojang.minecraftpe")));
    }
}
