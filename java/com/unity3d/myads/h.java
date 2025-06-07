package com.unity3d.myads;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.craftsmanx.lancarjaya.BuildConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class h extends Application {
    public AdBlocker adBlocker;
    private static h instance;
    public static boolean isNoads;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        FirebaseApp.initializeApp(this);  // Inicializa Firebase
        FirebaseAnalytics.getInstance(this);  // Obtiene una instancia de Firebase Analytics

        SharedPreferences preferences = getSharedPreferences("isNoads", 0);
        isNoads = preferences.getBoolean("isNoads", false);
        adBlocker = new AdBlocker();
        hookWebViewResources();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    // Aquí puedes enviar el token a tu servidor para notificaciones dirigidas
                });
    }

    public boolean shouldBlockAd(String url) {
        return isPremiumUser() && adBlocker.shouldBlockAd(url);
    }

    private boolean isPremiumUser() {
        return isNoads;
    }

    private void hookWebViewResources() {
        try {
            Resources res = getResources();
            Field field = res.getClass().getDeclaredField("mClassLoader");
            field.setAccessible(true);
            ClassLoader classLoader = (ClassLoader) field.get(res);
            Class<?> webViewClass = classLoader.loadClass("android.webkit.WebView");
            classLoader.loadClass(BuildConfig.APPLICATION_ID + ".AdBlockingWebView").getDeclaredConstructor(webViewClass).newInstance(null);
        } catch (Exception e) {
            Log.e("Adblock", "hookWebViewResources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class AdBlocker {
        private final Set<String> adHosts;

        public AdBlocker() {
            adHosts = new HashSet<>();
            adHosts.add("googleadservices.com");
            adHosts.add("googlesyndication.com");
            adHosts.add("doubleclick.net");
            adHosts.add("google-analytics.com");
            adHosts.add("googletagmanager.com");
            adHosts.add("googletagservices.com");
            adHosts.add("unityads.unity3d.com");
            adHosts.add("unityads.unitychina.cn");
            adHosts.add("admob.com");

        }

        public boolean shouldBlockAd(String url) {
            for (String adHost : adHosts) {
                if (url.contains(adHost)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class AdBlockingWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (h.getInstance().shouldBlockAd(url)) {
                return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    public class AdBlockingWebView extends WebView {
        public AdBlockingWebView(Context context) {
            super(context);
            init();
        }

        public AdBlockingWebView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public AdBlockingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            setWebViewClient(new AdBlockingWebViewClient());
        }
    }

    public static h getInstance() {
        return instance;
    }

}
