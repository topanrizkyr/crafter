package com.mojang.minecraftpe.store;

public class Purchase {
    public String mPlatformPurchaseId;
    public String mProductId;
    public boolean mPurchaseActive;
    public String mReceipt;

    public Purchase(String str, String str2, String str3, boolean z) {
        this.mProductId = str;
        this.mPlatformPurchaseId = str2;
        this.mReceipt = str3;
        this.mPurchaseActive = z;
    }
}
