package com.mojang.minecraftpe;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.core.view.ViewCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFWriter {
    private Rect mImageRect;
    private PdfDocument mOpenDocument;
    private Rect mPageRect = new Rect(0, 0, 612, 792);
    private TextPaint mPageTextPaint;
    private Rect mTextRect;
    private Rect mTitleRect;
    private TextPaint mTitleTextPaint;

    public PDFWriter() {
        Rect rect = new Rect(0, 0, this.mPageRect.width(), (int) (this.mPageRect.height() * 0.7f));
        this.mTitleRect = rect;
        rect.offset(0, (int) (this.mPageRect.height() * 0.3f));
        Rect rect2 = new Rect(this.mPageRect);
        this.mTextRect = rect2;
        rect2.inset(20, 20);
        Rect rect3 = new Rect(0, 0, 500, 500);
        this.mImageRect = rect3;
        rect3.offset(this.mPageRect.centerX() - this.mImageRect.centerX(), this.mPageRect.centerY() - this.mImageRect.centerY());
        Typeface typeface = Typeface.DEFAULT_BOLD;
        try {
            typeface = Typeface.createFromAsset(MainActivity.mInstance.getAssets(), "assets/fonts/Mojangles.ttf");
        } catch (Exception e) {
            System.out.println("Failed to load mojangles font: " + e.getMessage());
        }
        TextPaint textPaint = new TextPaint();
        this.mTitleTextPaint = textPaint;
        textPaint.setAntiAlias(true);
        this.mTitleTextPaint.setTextSize(64.0f);
        this.mTitleTextPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mTitleTextPaint.setTypeface(typeface);
        TextPaint textPaint2 = new TextPaint();
        this.mPageTextPaint = textPaint2;
        textPaint2.setAntiAlias(true);
        this.mPageTextPaint.setTextSize(32.0f);
        this.mPageTextPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
    }

    public boolean createDocument(String[] strArr, String str) {
        PdfDocument pdfDocument = this.mOpenDocument;
        if (pdfDocument != null) {
            pdfDocument.close();
        }
        PdfDocument pdfDocument2 = new PdfDocument();
        this.mOpenDocument = pdfDocument2;
        PdfDocument.Page startPage = pdfDocument2.startPage(_getPageInfo(1));
        _drawTextInRect(str, startPage, this.mTitleTextPaint, this.mTitleRect, Layout.Alignment.ALIGN_CENTER);
        this.mOpenDocument.finishPage(startPage);
        for (int i = 0; i < strArr.length; i++) {
            String str2 = strArr[i];
            PdfDocument.Page startPage2 = this.mOpenDocument.startPage(_getPageInfo(i + 2));
            try {
                String _getExtension = _getExtension(str2);
                if (_getExtension.equals("txt")) {
                    _drawTextInRect(_readFileToString(str2), startPage2, this.mPageTextPaint, this.mTextRect, Layout.Alignment.ALIGN_NORMAL);
                } else if (_getExtension.equals("jpeg")) {
                    startPage2.getCanvas().drawBitmap(BitmapFactory.decodeFile(str2), (Rect) null, this.mImageRect, (Paint) null);
                } else {
                    throw new UnsupportedOperationException("Unsupported extension from file: " + str2);
                }
                this.mOpenDocument.finishPage(startPage2);
            } catch (Exception e) {
                System.out.println("Failed to write page: " + e.getMessage());
                closeDocument();
                return false;
            }
        }
        return true;
    }

    public boolean writeDocumentToFile(String str) {
        try {
            this.mOpenDocument.writeTo(new FileOutputStream(str));
            return true;
        } catch (Exception e) {
            System.out.println("Failed to write pdf file: " + e.getMessage());
            return false;
        }
    }

    public void closeDocument() {
        PdfDocument pdfDocument = this.mOpenDocument;
        if (pdfDocument != null) {
            pdfDocument.close();
            this.mOpenDocument = null;
        }
    }

    public String getPicturesDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }

    private PdfDocument.PageInfo _getPageInfo(int i) {
        return new PdfDocument.PageInfo.Builder(this.mPageRect.width(), this.mPageRect.height(), i).create();
    }

    private void _drawTextInRect(String str, PdfDocument.Page page, TextPaint textPaint, Rect rect, Layout.Alignment alignment) {
        StaticLayout staticLayout = new StaticLayout(str, textPaint, rect.width(), alignment, 1.0f, 0.0f, false);
        Canvas canvas = page.getCanvas();
        canvas.translate(rect.left, rect.top);
        staticLayout.draw(canvas);
    }

    private String _readFileToString(String str) throws FileNotFoundException, IOException {
        File file = new File(str);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bArr = new byte[(int) file.length()];
        fileInputStream.read(bArr);
        fileInputStream.close();
        return new String(bArr);
    }

    private String _getExtension(String str) {
        int i;
        int lastIndexOf = str.lastIndexOf(46);
        return (lastIndexOf < 0 || (i = lastIndexOf + 1) >= str.length()) ? "" : str.substring(i).toLowerCase();
    }
}
