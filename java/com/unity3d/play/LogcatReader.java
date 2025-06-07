package com.unity3d.play;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LogcatReader extends Thread {
    private Context context;
    private boolean hasDetectedPlayerSpawned = false;
    private boolean hasDetectedPlayerDisconnected = false;
    private static final int AD_COOLDOWN = 5000; // Cooldown 5 detik

    public LogcatReader(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = Runtime.getRuntime().exec("logcat");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Player Spawned") && !hasDetectedPlayerSpawned) {
                    Log.d("LogcatReader", "🎮 'Player Spawned' terdeteksi! Menampilkan iklan...");
                    hasDetectedPlayerSpawned = true;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (context instanceof Activity) {
                            PlayerSpawnedAd.showInterstitial(context);
                        } else {
                            Log.e("LogcatReader", "Context bukan Activity, tidak bisa menampilkan iklan.");
                        }
                    });

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hasDetectedPlayerSpawned = false;
                    }, AD_COOLDOWN);
                }

                if (line.contains("Player disconnected") && !hasDetectedPlayerDisconnected) {
                    Log.d("LogcatReader", "🚪 'Player disconnected' terdeteksi! Menampilkan iklan...");
                    hasDetectedPlayerDisconnected = true;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (context instanceof Activity) {
                            PlayerSpawnedAd.showInterstitial(context);
                        } else {
                            Log.e("LogcatReader", "Context bukan Activity, tidak bisa menampilkan iklan.");
                        }
                    });

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hasDetectedPlayerDisconnected = false;
                    }, AD_COOLDOWN);
                }
            }
        } catch (IOException e) {
            Log.e("LogcatReader", "Error menjalankan logcat: " + e.getMessage());
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (IOException e) {
                Log.e("LogcatReader", "Error menutup BufferedReader: " + e.getMessage());
            }
            if (process != null) process.destroy();
        }
    }
}