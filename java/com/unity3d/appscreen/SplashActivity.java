package com.unity3d.appscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.craftsmanx.lancarjaya.R;
import com.unity3d.myads.AvatarInstaller;
import com.unity3d.myads.BehaviorsInstaller;
import com.unity3d.myads.MapsInstaller;
import com.unity3d.myads.ResourcesInstaller;
import com.unity3d.myads.SkinsInstaller;

import java.util.concurrent.atomic.AtomicInteger;


public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView percentageText;
    private TextView loadingText;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private int messageIndex = 0;

    private static MediaPlayer mediaPlayer;

    private String[] loadingMessages = {
            "You can change the controls, camera settings...",
            "Connect your account for a personalized experience...",
            "Adjust brightness and graphics to improve performance...",
            "Don't forget to check out the new game modes...",
            "We're almost ready, thanks for waiting!"
    };

    public static SplashActivity splashActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica si esta actividad es la raíz de la tarea
        if (!isTaskRoot()) {
            finish();
            return;
        }
        splashActivity = this;
        setContentView(R.layout.activity_splash);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        progressBar = findViewById(R.id.progress_bar);
        percentageText = findViewById(R.id.percentage_text);
        loadingText = findViewById(R.id.loading_text);

        updateDynamicMessage();
        installResourcesAndContinue(); // Instalar todos los recursos necesarios
    }

    private void updateDynamicMessage() {
        handler.postDelayed(() -> {
            loadingText.setText(loadingMessages[messageIndex]);
            messageIndex = (messageIndex + 1) % loadingMessages.length;
            updateDynamicMessage();
        }, 2000);
    }

    private void installResourcesAndContinue() {
        // Verifica si los recursos ya están instalados
        SharedPreferences preferences = getSharedPreferences("MinecraftPreferences", MODE_PRIVATE);
        boolean worldsInstalled = preferences.getBoolean("worldsInstalled", false);
        boolean skinsInstalled = preferences.getBoolean("skinsInstalled", false);
        boolean bpInstalled = preferences.getBoolean("bpInstalled", false);
        boolean resourcesInstalled = preferences.getBoolean("resourcesInstalled", false);
        if (worldsInstalled && skinsInstalled && bpInstalled && resourcesInstalled) {
            checkIfCanStartGame();
        }

//        if (resourcesInstalled) {
//            Log.i("SplashActivity", "Recursos ya están instalados. Avanzando al juego.");
//            startGame(); // Avanza directamente al juego si los recursos ya están instalados
//            return;
//        }

        // Total de instaladores
        int totalInstallers = 4;
        AtomicInteger pendingInstallations = new AtomicInteger(totalInstallers); // Contador para verificar progreso

        new Thread(() -> {
            try {
                // Instalar mapas
                MapsInstaller mapsInstaller = new MapsInstaller(SplashActivity.this);
                mapsInstaller.install(/*success -> {
                    if (success) {
                        Log.i("SplashActivity", "Mapas instalados correctamente.");
                    } else {
                        Log.e("SplashActivity", "Error al instalar mapas.");
                    }
                    checkInstallationsComplete(preferences, pendingInstallations);
                }*/);
//                new SkinsInstaller(SplashActivity.this).install();
//
//                // Instalar comportamientos
//                BehaviorsInstaller behaviorsInstaller = new BehaviorsInstaller(SplashActivity.this);
//                behaviorsInstaller.install(/*success -> {
//                    if (success) {
//                        Log.i("SplashActivity", "Comportamientos instalados correctamente.");
//                    } else {
//                        Log.e("SplashActivity", "Error al instalar comportamientos.");
//                    }
//                    checkInstallationsComplete(preferences, pendingInstallations);
//                }*/);
//
//                // Instalar avatares
////                AvatarInstaller avatarInstaller = new AvatarInstaller(SplashActivity.this);
////                avatarInstaller.install(success -> {
////                    if (success) {
////                        Log.i("SplashActivity", "Avatares instalados correctamente.");
////                    } else {
////                        Log.e("SplashActivity", "Error al instalar avatares.");
////                    }
//////                    checkInstallationsComplete(preferences, pendingInstallations);
////                });
//
//                // Instalar recursos
//                ResourcesInstaller resourcesInstaller = new ResourcesInstaller(SplashActivity.this);
//                resourcesInstaller.install(/*success -> {
//                    if (success) {
//                        Log.i("SplashActivity", "Recursos instalados correctamente.");
//                    } else {
//                        Log.e("SplashActivity", "Error al instalar recursos.");
//                    }
//                    checkInstallationsComplete(preferences, pendingInstallations);
//                }*/);


                // Simular progreso de la barra mientras se instalan recursos
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(100); // Ajusta para sincronizar con la instalación
                    progressStatus = i;
                    handler.post(() -> {
                        progressBar.setProgress(progressStatus);
                        percentageText.setText(progressStatus + "%");
                    });
                }
            } catch (Exception e) {
                Log.e("SplashActivity", "Error durante la instalación de recursos: " + e.getMessage());
            }
        }).start();
    }

    public static void checkIfCanStartGame() {
//        SharedPreferences preferences = splashActivity.getSharedPreferences("MinecraftPreferences", MODE_PRIVATE);
//        boolean resourcesInstalled = preferences.getBoolean("resourcesInstalled", false);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean("resourcesInstalled", true);
//        editor.apply();

        stopAndReleaseMediaPlayer();
        Intent intent = new Intent(splashActivity, com.mojang.minecraftpe.MainActivity.class);
        splashActivity.startActivity(intent);
        splashActivity.finish();
    }

    private void checkInstallationsComplete(SharedPreferences preferences, AtomicInteger pendingInstallations) {
        if (pendingInstallations.decrementAndGet() == 0) {
            Log.i("SplashActivity", "Todas las instalaciones completadas. Avanzando al juego.");
            // Marcar los recursos como instalados
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("resourcesInstalled", true);
            editor.apply();

            startGame();
        }
    }


    private void startGame() {
        handler.post(() -> {
            stopAndReleaseMediaPlayer();
            Intent intent = new Intent(SplashActivity.this, com.mojang.minecraftpe.MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private static void stopAndReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndReleaseMediaPlayer();
        handler.removeCallbacksAndMessages(null);
    }
}
