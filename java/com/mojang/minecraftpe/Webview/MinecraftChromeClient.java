package com.mojang.minecraftpe.Webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.mojang.minecraftpe.MainActivity;

class MinecraftChromeClient extends WebChromeClient {
    private MinecraftWebview mView;

    public MinecraftChromeClient(MinecraftWebview minecraftWebview) {
        this.mView = minecraftWebview;
    }

    @Override
    public void onProgressChanged(WebView webView, int i) {
        super.onProgressChanged(webView, i);
        MainActivity.mInstance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftChromeClient.this.mView._injectApi();
            }
        });
    }
}
