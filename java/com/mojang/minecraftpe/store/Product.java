package com.mojang.minecraftpe.store;

public class Product {
    public String mCurrencyCode;
    public String mId;
    public String mPrice;
    public String mUnformattedPrice;

    public Product(String str, String str2, String str3, String str4) {
        this.mId = str;
        this.mPrice = str2;
        this.mCurrencyCode = str3;
        this.mUnformattedPrice = str4;
    }
}
