package com.mojang.minecraftpe.store.amazonappstore;

import android.content.Context;
import android.util.Log;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;
import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Purchase;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class AmazonAppStore implements Store {
    private boolean mForFireTV;
    private StoreListener mListener;
    private PurchasingListener mPurchasingListener;
    private Map<RequestId, String> mProductIdRequestMapping = new HashMap();
    private final String subscriptionKey = ".subscription";
    private Locale userLocale = null;
    private Currency userCurrency = null;
    private final String mPlatformPurchaseId = "";

    @Override
    public void destructor() {
    }

    @Override
    public String getStoreId() {
        return "android.amazonappstore";
    }

    @Override
    public boolean hasVerifiedLicense() {
        return false;
    }

    @Override
    public void purchaseGame() {
    }

    @Override
    public boolean receivedLicenseResponse() {
        return false;
    }

    public AmazonAppStore(Context context, StoreListener storeListener, boolean z) {
        PurchasingListener purchasingListener = new PurchasingListener() {
            @Override
            public void onUserDataResponse(UserDataResponse userDataResponse) {
                if (userDataResponse == null || userDataResponse.getUserData() == null) {
                    return;
                }
                UserData userData = userDataResponse.getUserData();
                AmazonAppStore.this.userLocale = new Locale(Locale.getDefault().getLanguage(), userData.getMarketplace());
                AmazonAppStore amazonAppStore = AmazonAppStore.this;
                amazonAppStore.userCurrency = Currency.getInstance(amazonAppStore.userLocale);
            }

            @Override
            public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
                if (purchaseUpdatesResponse.getRequestStatus() != PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
                    AmazonAppStore.this.mListener.onQueryPurchasesFail();
                    return;
                }
                ArrayList arrayList = new ArrayList();
                String userId = purchaseUpdatesResponse.getUserData().getUserId();
                for (Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                    arrayList.add(new Purchase(receipt.getSku(), "", AmazonAppStore.this.createReceipt(userId, receipt.getReceiptId()), !receipt.isCanceled()));
                }
                AmazonAppStore.this.mListener.onQueryPurchasesSuccess((Purchase[]) arrayList.toArray(new Purchase[0]));
            }

            @Override
            public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
                String str = (String) AmazonAppStore.this.mProductIdRequestMapping.remove(purchaseResponse.getRequestId());
                if (purchaseResponse.getRequestStatus() == PurchaseResponse.RequestStatus.SUCCESSFUL) {
                    AmazonAppStore.this.mListener.onPurchaseSuccessful(str, "", AmazonAppStore.this.createReceipt(purchaseResponse));
                } else {
                    AmazonAppStore.this.mListener.onPurchaseFailed(str);
                }
            }

            @Override
            public void onProductDataResponse(ProductDataResponse productDataResponse) {
                if (productDataResponse.getRequestStatus() != ProductDataResponse.RequestStatus.SUCCESSFUL) {
                    AmazonAppStore.this.mListener.onQueryProductsFail();
                    return;
                }
                ArrayList arrayList = new ArrayList();
                Set<String> unavailableSkus = productDataResponse.getUnavailableSkus();
                Map<String, Product> productData = productDataResponse.getProductData();
                for (String str : productData.keySet()) {
                    if (!unavailableSkus.contains(str)) {
                        Product product = productData.get(str);
                        String str2 = "";
                        String replace = product.getSku() != null ? product.getSku().replace(".child", "") : "";
                        String price = product.getPrice() != null ? product.getPrice() : "";
                        String str3 = "0";
                        if (AmazonAppStore.this.userLocale != null && AmazonAppStore.this.userCurrency != null) {
                            try {
                                str2 = AmazonAppStore.this.userCurrency.getCurrencyCode();
                                str3 = NumberFormat.getCurrencyInstance(AmazonAppStore.this.userLocale).parse(price).toString();
                            } catch (Exception e) {
                                Log.i("AmazonAppStore", e.getMessage());
                            }
                        }
                        Log.i("AmazonAppStore", "--queryProductsResponse add sku[" + replace + "] price[" + price + "] currencyCode[" + str2 + "] unformatted[" + str3 + "]");
                        arrayList.add(new com.mojang.minecraftpe.store.Product(replace, price, str2, str3));
                    }
                }
                AmazonAppStore.this.mListener.onQueryProductsSuccess((com.mojang.minecraftpe.store.Product[]) arrayList.toArray(new com.mojang.minecraftpe.store.Product[0]));
            }
        };
        this.mPurchasingListener = purchasingListener;
        this.mListener = storeListener;
        this.mForFireTV = z;
        PurchasingService.registerListener(context, purchasingListener);
        storeListener.onStoreInitialized(true);
    }

    @Override
    public String getProductSkuPrefix() {
        return this.mForFireTV ? "firetv." : "";
    }

    @Override
    public String getRealmsSkuPrefix() {
        return this.mForFireTV ? "firetv." : "";
    }

    @Override
    public ExtraLicenseResponseData getExtraLicenseData() {
        return new ExtraLicenseResponseData(0L, 0L, 0L);
    }

    @Override
    public void queryProducts(String[] strArr) {
        String[] strArr2 = new String[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].indexOf(".subscription") != -1) {
                strArr2[i] = strArr[i] + ".child";
            } else {
                strArr2[i] = strArr[i];
            }
        }
        PurchasingService.getUserData();
        PurchasingService.getProductData(new HashSet(Arrays.asList(strArr2)));
    }

    @Override
    public void purchase(String str, boolean z, String str2) {
        this.mProductIdRequestMapping.put(PurchasingService.purchase(str), str);
    }

    @Override
    public void acknowledgePurchase(String str, String str2) {
        try {
            PurchasingService.notifyFulfillment(new JSONObject(str).getString("receiptId"), FulfillmentResult.FULFILLED);
        } catch (JSONException unused) {
        }
    }

    @Override
    public void queryPurchases() {
        PurchasingService.getPurchaseUpdates(true);
    }

    private String createReceipt(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("userId", str);
            jSONObject.put("receiptId", str2);
            return jSONObject.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String createReceipt(PurchaseResponse purchaseResponse) {
        return createReceipt(purchaseResponse.getUserData().getUserId(), purchaseResponse.getReceipt().getReceiptId());
    }
}
