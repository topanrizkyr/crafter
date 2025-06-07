package com.unity3d.myads;

import android.util.Log;

import java.lang.reflect.Array;

public class dek {
    public static String decode(String str) {
        int i;
        Boolean bool = true;
        int i2 = 0;
        while (true) {
            if (i2 >= 8) {
                break;
            } else if ("11111111".charAt(i2) != str.charAt(i2)) {
                bool = false;
                break;
            } else {
                i2++;
            }
        }
        String str2 = "";
        String str3 = str2;
        for (i = 8; i < str.length(); i++) {
            str3 = str3.concat(String.valueOf(str.charAt(i)));
        }
        int[][] iArr = (int[][]) Array.newInstance(int.class, 11101, 8);
        int i3 = -1;
        int i4 = 0;
        for (int i5 = 0; i5 < str3.length(); i5++) {
            if (i5 % 7 == 0) {
                i3++;
                iArr[i3][0] = str3.charAt(i5) - '0';
                i4 = 1;
            } else {
                iArr[i3][i4] = str3.charAt(i5) - '0';
                i4++;
            }
        }
        int[] iArr2 = new int[11111];
        int i6 = 0;
        int i7 = 0;
        while (i6 <= i3) {
            int i8 = 6;
            int i9 = 0;
            int i10 = 0;
            while (i8 >= 0) {
                i9 += iArr[i6][i8] * ((int) Math.pow(2.0d, i10));
                i10++;
                i8--;
                str2 = str2;
            }
            iArr2[i7] = i9;
            i6++;
            i7++;
        }
        for (int i11 = 0; i11 < i7; i11++) {
            str2 = str2.concat(String.valueOf((char) iArr2[i11]));
        }
        Log.e("dec", "text 11 - " + str2);
        return (str3.length() % 7 == 0 && bool.booleanValue()) ? str2 : "Invalid Code";
    }
}
