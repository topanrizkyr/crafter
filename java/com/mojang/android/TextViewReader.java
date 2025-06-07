package com.mojang.android;

import android.widget.TextView;

public class TextViewReader implements StringValue {
    private TextView _view;

    public TextViewReader(TextView textView) {
        this._view = textView;
    }

    @Override
    public String getStringValue() {
        return this._view.getText().toString();
    }
}
