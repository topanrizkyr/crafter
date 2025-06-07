package com.mojang.minecraftpe;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class PopupView {
    private View mContentView;
    private Context mContext;
    private int mHeight;
    private int mOriginX;
    private int mOriginY;
    private View mParentView;
    private View mPopupView;
    private int mWidth;
    private WindowManager mWindowManager;

    private int computeFlags(int i) {
        return i | 32;
    }

    private void preparePopup(WindowManager.LayoutParams layoutParams) {
    }

    public PopupView(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public void setContentView(View view) {
        this.mContentView = view;
    }

    public void setParentView(View view) {
        this.mParentView = view;
    }

    public void setWidth(int i) {
        this.mWidth = i;
    }

    public void setHeight(int i) {
        this.mHeight = i;
    }

    public void setRect(int i, int i2, int i3, int i4) {
        this.mWidth = i2 - i;
        this.mHeight = i4 - i3;
        this.mOriginX = i;
        this.mOriginY = i3;
    }

    public void setVisible(boolean z) {
        if (z != getVisible()) {
            if (z) {
                addPopupView();
            } else {
                removePopupView();
            }
        }
    }

    public boolean getVisible() {
        View view = this.mPopupView;
        return (view == null || view.getParent() == null) ? false : true;
    }

    public void dismiss() {
        if (this.mPopupView != null) {
            removePopupView();
            View view = this.mPopupView;
            View view2 = this.mContentView;
            if (view != view2 && (view instanceof ViewGroup)) {
                ((ViewGroup) view).removeView(view2);
            }
            this.mPopupView = null;
        }
    }

    public void update() {
        if (getVisible()) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.mPopupView.getLayoutParams();
            int computeFlags = computeFlags(layoutParams.flags);
            if (computeFlags != layoutParams.flags) {
                layoutParams.flags = computeFlags;
            }
            setLayoutRect(layoutParams);
            this.mWindowManager.updateViewLayout(this.mPopupView, layoutParams);
        }
    }

    private void addPopupView() {
        this.mPopupView = this.mContentView;
        WindowManager.LayoutParams createPopupLayout = createPopupLayout(this.mParentView.getWindowToken());
        setLayoutRect(createPopupLayout);
        invokePopup(createPopupLayout);
    }

    private void removePopupView() {
        try {
            this.mParentView.requestFocus();
            this.mWindowManager.removeView(this.mPopupView);
        } catch (Exception unused) {
        }
    }

    private WindowManager.LayoutParams createPopupLayout(IBinder iBinder) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = -3;
        layoutParams.flags = computeFlags(layoutParams.flags);
        layoutParams.type = 1000;
        layoutParams.token = iBinder;
        layoutParams.softInputMode = 1;
        layoutParams.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));
        layoutParams.windowAnimations = -1;
        return layoutParams;
    }

    private void invokePopup(WindowManager.LayoutParams layoutParams) {
        layoutParams.packageName = this.mContext.getPackageName();
        this.mWindowManager.addView(this.mPopupView, layoutParams);
        this.mParentView.requestFocus();
    }

    private void setLayoutRect(WindowManager.LayoutParams layoutParams) {
        layoutParams.width = this.mWidth;
        layoutParams.height = this.mHeight;
        layoutParams.x = this.mOriginX;
        layoutParams.y = this.mOriginY;
        layoutParams.gravity = 51;
    }
}
