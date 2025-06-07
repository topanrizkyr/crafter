package com.mojang.minecraftpe;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class WorldRecovery {
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mTotalFilesToCopy = 0;
    private long mTotalBytesRequired = 0;

    private static native void nativeComplete();

    private static native void nativeError(String str, long j, long j2);

    private static native void nativeUpdate(String str, int i, int i2, long j, long j2);

    WorldRecovery(Context context, ContentResolver contentResolver) {
        this.mContext = context;
        this.mContentResolver = contentResolver;
    }

    public String migrateFolderContents(String str, String str2) {
        final DocumentFile fromTreeUri = DocumentFile.fromTreeUri(this.mContext, Uri.parse(str));
        if (fromTreeUri == null) {
            return "Could not resolve URI to a DocumentFile tree: " + str;
        }
        if (!fromTreeUri.isDirectory()) {
            return "Root file of URI is not a directory: " + str;
        }
        final File file = new File(str2);
        if (!file.isDirectory()) {
            return "Destination folder does not exist: " + str2;
        }
        if (((String[]) Objects.requireNonNull(file.list())).length != 0) {
            return "Destination folder is not empty: " + str2;
        }
        new Thread(new Runnable() {
            @Override
            public final void run() {
                m158x3b51f2cf(fromTreeUri, file);
            }
        }).start();
        return "";
    }

    private void m158x3b51f2cf(DocumentFile documentFile, File file) {
        ArrayList<DocumentFile> arrayList = new ArrayList<>();
        this.mTotalFilesToCopy = 0;
        long j = 0;
        this.mTotalBytesRequired = 0L;
        generateCopyFilesRecursively(arrayList, documentFile);
        long availableBytes = new StatFs(file.getAbsolutePath()).getAvailableBytes();
        long j2 = this.mTotalBytesRequired;
        if (j2 >= availableBytes) {
            nativeError("Insufficient space", j2, availableBytes);
            return;
        }
        String path = documentFile.getUri().getPath();
        String str = file + "_temp";
        File file2 = new File(str);
        byte[] bArr = new byte[8192];
        Iterator<DocumentFile> it = arrayList.iterator();
        int i = 0;
        long j3 = 0;
        while (it.hasNext()) {
            DocumentFile next = it.next();
            String str2 = str + next.getUri().getPath().substring(path.length());
            if (next.isDirectory()) {
                File file3 = new File(str2);
                if (!file3.isDirectory()) {
                    Log.i("Minecraft", "Creating directory '" + str2 + "'");
                    if (!file3.mkdirs()) {
                        nativeError("Could not create directory: " + str2, j, j);
                        return;
                    }
                } else {
                    Log.i("Minecraft", "Directory '" + str2 + "' already exists");
                }
            } else {
                Log.i("Minecraft", "Copying '" + next.getUri().getPath() + "' to '" + str2 + "'");
                i++;
                nativeUpdate("Copying: " + str2, this.mTotalFilesToCopy, i, this.mTotalBytesRequired, j3);
                try {
                    InputStream openInputStream = this.mContentResolver.openInputStream(next.getUri());
                    FileOutputStream fileOutputStream = new FileOutputStream(str2);
                    while (true) {
                        int read = openInputStream.read(bArr, 0, 8192);
                        if (read < 0) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                        j3 += read;
                    }
                    fileOutputStream.close();
                    openInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    nativeError(e.getMessage(), 0L, 0L);
                    return;
                }
            }
            j = 0;
        }
        if (file.delete()) {
            if (file2.renameTo(file)) {
                nativeComplete();
                return;
            } else if (file.mkdir()) {
                nativeError("Could not replace destination directory: " + file.getAbsolutePath(), 0L, 0L);
                return;
            } else {
                nativeError("Could not recreate destination directory after failed replace: " + file.getAbsolutePath(), 0L, 0L);
                return;
            }
        }
        nativeError("Could not delete empty destination directory: " + file.getAbsolutePath(), 0L, 0L);
    }

    private void generateCopyFilesRecursively(ArrayList<DocumentFile> arrayList, DocumentFile documentFile) {
        for (DocumentFile documentFile2 : documentFile.listFiles()) {
            arrayList.add(documentFile2);
            if (documentFile2.isDirectory()) {
                generateCopyFilesRecursively(arrayList, documentFile2);
            } else {
                this.mTotalBytesRequired += documentFile2.length();
                this.mTotalFilesToCopy++;
            }
        }
    }
}
