package com.mojang.minecraftpe;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import java.util.ArrayList;

public class TextInputProxyEditTextbox extends EditText {
    private MCPEKeyWatcher _mcpeKeyWatcher;
    public int allowedLength;

    public interface MCPEKeyWatcher {
        boolean onBackKeyPressed();

        void onDeleteKeyPressed();
    }

    public TextInputProxyEditTextbox(Context context) {
        super(context);
        this._mcpeKeyWatcher = null;
    }

    public void updateFilters(int i, boolean z) {
        this.allowedLength = i;
        ArrayList arrayList = new ArrayList();
        if (i != 0) {
            arrayList.add(new InputFilter.LengthFilter(this.allowedLength));
        }
        if (z) {
            arrayList.add(createSingleLineFilter());
        }
        arrayList.add(createUnicodeFilter());
        setFilters((InputFilter[]) arrayList.toArray(new InputFilter[arrayList.size()]));
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        return new MCPEInputConnection(super.onCreateInputConnection(editorInfo), true, this);
    }

    @Override
    public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
        if (i == 4 && keyEvent.getAction() == 1) {
            MCPEKeyWatcher mCPEKeyWatcher = this._mcpeKeyWatcher;
            if (mCPEKeyWatcher != null) {
                return mCPEKeyWatcher.onBackKeyPressed();
            }
            return false;
        }
        return super.onKeyPreIme(i, keyEvent);
    }

    public void setOnMCPEKeyWatcher(MCPEKeyWatcher mCPEKeyWatcher) {
        this._mcpeKeyWatcher = mCPEKeyWatcher;
    }

    private InputFilter createSingleLineFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
                for (int i5 = i; i5 < i2; i5++) {
                    if (charSequence.charAt(i5) == '\n') {
                        return charSequence.subSequence(i, i5);
                    }
                }
                return null;
            }
        };
    }

    private InputFilter createUnicodeFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
                StringBuilder sb = null;
                for (int i5 = i; i5 < i2; i5++) {
                    if (charSequence.charAt(i5) == 12288) {
                        if (sb == null) {
                            sb = new StringBuilder(charSequence);
                        }
                        sb.setCharAt(i5, ' ');
                    }
                }
                if (sb != null) {
                    return sb.subSequence(i, i2);
                }
                return null;
            }
        };
    }

    private class MCPEInputConnection extends InputConnectionWrapper {
        TextInputProxyEditTextbox textbox;

        public MCPEInputConnection(InputConnection inputConnection, boolean z, TextInputProxyEditTextbox textInputProxyEditTextbox) {
            super(inputConnection, z);
            this.textbox = textInputProxyEditTextbox;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent keyEvent) {
            if (this.textbox.getText().length() == 0 && keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 67) {
                if (TextInputProxyEditTextbox.this._mcpeKeyWatcher == null) {
                    return false;
                }
                TextInputProxyEditTextbox.this._mcpeKeyWatcher.onDeleteKeyPressed();
                return false;
            }
            return super.sendKeyEvent(keyEvent);
        }
    }
}
