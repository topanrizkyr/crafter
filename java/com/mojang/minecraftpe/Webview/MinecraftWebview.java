package com.mojang.minecraftpe.Webview;

import android.R;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mojang.minecraftpe.MainActivity;
import com.mojang.minecraftpe.PopupView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MinecraftWebview {
    private MainActivity mActivity;
    private int mId;
    private WebView mWebView;
    private PopupView mWebViewPopup;

    private native void nativeOnWebError(int i, int i2, String str);

    private native void nativeSendToHost(int i, String str, String str2, String str3);

    public void setMuted(boolean z) {
    }

    public void sendToHost(String str, String str2, String str3) {
        nativeSendToHost(this.mId, str, str2, str3);
    }

    public void onWebError(int i, String str) {
        nativeOnWebError(this.mId, i, str);
    }

    public MinecraftWebview(int i) {
        this.mId = i;
        MainActivity mainActivity = MainActivity.mInstance;
        this.mActivity = mainActivity;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftWebview.this._createWebView();
            }
        });
    }

    public void teardown() {
        this.mWebViewPopup.dismiss();
        this.mWebViewPopup = null;
        this.mWebView = null;
        this.mActivity = null;
        this.mId = -1;
    }

    public void setRect(float f, float f2, float f3, float f4) {
        final int i = (int) f;
        final int i2 = (int) f2;
        final int i3 = (int) f3;
        final int i4 = (int) f4;
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftWebview.this.mWebViewPopup.setRect(i, i2, i3, i4);
                MinecraftWebview.this.mWebViewPopup.update();
            }
        });
    }

    public void setPropagatedAlpha(float f) {
        setShowView(((double) f) == 1.0d);
    }

    public void setUrl(final String str) {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftWebview.this.mWebView.loadUrl(str);
            }
        });
    }

    public void setShowView(final boolean z) {
        if (z) {
            _hideSystemBars();
        }
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftWebview.this.mWebViewPopup.setVisible(z);
                MinecraftWebview minecraftWebview = MinecraftWebview.this;
                Object[] objArr = new Object[1];
                objArr[0] = z ? "onShow" : "onHide";
                minecraftWebview.sendToWebView(String.format("window.ipcCodeScreenRenderer.%s();", objArr));
            }
        });
    }

    public void sendToWebView(final String str) {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MinecraftWebview.this.mWebView.evaluateJavascript(str, null);
            }
        });
    }

    public void _injectApi() {
        MainActivity mainActivity = this.mActivity;
        if (mainActivity != null) {
            String _readResource = _readResource(mainActivity.getResources().getIdentifier("code_builder_hosted_editor", "raw", this.mActivity.getPackageName()));
            if (_readResource != null) {
                this.mWebView.evaluateJavascript(_readResource, null);
                return;
            } else {
                onWebError(0, "Unable to inject api");
                return;
            }
        }
        onWebError(0, "_injectApi called after teardown");
    }

    private String _readResource(int i) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream openRawResource = this.mActivity.getResources().openRawResource(i);
        try {
            byte[] bArr = new byte[256];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read > 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                } else {
                    openRawResource.close();
                    return byteArrayOutputStream.toString();
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read resource " + i + " with error " + e.toString());
            return null;
        }
    }

    private Boolean _hideSystemBars(View view) {
        if (view == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController windowInsetsController = view.getWindowInsetsController();
            if (windowInsetsController == null) {
                return false;
            }
            windowInsetsController.setSystemBarsBehavior(2);
            windowInsetsController.hide(WindowInsets.Type.systemBars());
            return true;
        }
        view.setSystemUiVisibility(5894);
        return true;
    }

    private void _hideSystemBars() {
        MainActivity mainActivity = this.mActivity;
        if (mainActivity == null) {
            return;
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                m157xa7fbd3d6();
            }
        });
    }

    void m157xa7fbd3d6() {
        WebView webView = this.mWebView;
        if (webView == null || _hideSystemBars(webView).booleanValue()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public final void run() {
                m156xa87239d5();
            }
        }).start();
    }

    void m156xa87239d5() {
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _hideSystemBars();
    }

    private void _createWebView() {
        if (!MainActivity.mInstance.isPublishBuild()) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        WebView webView = new WebView(this.mActivity);
        this.mWebView = webView;
        webView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mWebView.setWebViewClient(new MinecraftWebViewClient(this));
        this.mWebView.setWebChromeClient(new MinecraftChromeClient(this));
        this.mWebView.addJavascriptInterface(new WebviewHostInterface(this), "codeBuilderHostInterface");
        WebSettings settings = this.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        this.mWebViewPopup = new PopupView(this.mActivity);
        View rootView = this.mActivity.findViewById(R.id.content).getRootView();
        this.mWebViewPopup.setContentView(this.mWebView);
        this.mWebViewPopup.setParentView(rootView);
    }
}
