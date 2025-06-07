package com.mojang.minecraftpe;

import android.content.Intent;

public class FilePickerManager implements ActivityListener {
    static final int PICK_DIRECTORY_REQUEST_CODE = 246242755;
    FilePickerManager2 mHandler;

    private static native void nativeDirectoryPickResult(String str, String str2);

    @Override
    public void onDestroy() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onStop() {
    }

    public FilePickerManager(FilePickerManager2 filePickerManager2) {
        this.mHandler = filePickerManager2;
    }

    public void pickDirectory(String str, String str2) {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        if (str != null && !str.isEmpty()) {
            intent.putExtra("android.provider.extra.PROMPT", str);
        }
        if (str2 != null && !str2.isEmpty()) {
            intent.putExtra("android.provider.extra.INITIAL_URI", str2);
        }
        this.mHandler.startPickerActivity(intent, PICK_DIRECTORY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == PICK_DIRECTORY_REQUEST_CODE) {
            if (i2 == -1) {
                nativeDirectoryPickResult(intent.getData().toString(), "");
            } else {
                nativeDirectoryPickResult("", "No directory selected");
            }
        }
    }
}
