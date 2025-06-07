package com.mojang.minecraftpe.store.googleplay;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.mojang.minecraftpe.store.Product;
import com.mojang.minecraftpe.store.StoreListener;
import handheld.project.android.src.com.mojang.minecraftpe.store.googleplay.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GooglePlayBillingImpl implements PurchasesUpdatedListener, PurchasesResponseListener, AcknowledgePurchaseResponseListener, ConsumeResponseListener, ProductDetailsResponseListener {
    private static final String mNewSubscriptionTag = "NEW_SUB";
    private final Activity mActivity;
    private BillingClient mBillingClient;
    private final StoreListener mListener;
    private final String mSignatureBase64;
    private String mSkuInProgress;
    private String mWorldName;
    private boolean mHasTelemetryBeenSentThisSession = false;
    private final Map<String, ProductDetails> mProductDetailsMap = new HashMap();

    public GooglePlayBillingImpl(Activity activity, StoreListener storeListener, String str) {
        this.mActivity = activity;
        this.mListener = storeListener;
        this.mSignatureBase64 = str;
        initialize();
    }

    private void initialize() {
        BillingClient build = BillingClient.newBuilder(this.mActivity).setListener(this).enablePendingPurchases().build();
        this.mBillingClient = build;
        build.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                GooglePlayBillingImpl.this.mListener.onStoreInitialized(billingResult.getResponseCode() == 0);
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d("GooglePlayBillingImpl", "Billing service disconnected.");
            }
        });
    }

    public void launchInAppPurchaseFlow(Activity activity, String str, String str2) {
        if (!this.mBillingClient.isReady()) {
            Log.v("GooglePlayBillingImpl", "Billing client is not ready when launching purchase flow");
            return;
        }
        Map<String, ProductDetails> map = this.mProductDetailsMap;
        if (map == null) {
            Log.v("GooglePlayBillingImpl", "mProductDetails map is null");
            return;
        }
        ProductDetails productDetails = map.get(str);
        if (productDetails == null) {
            Log.v("GooglePlayBillingImpl", "Unable to find SKU");
            return;
        }
        this.mSkuInProgress = str;
        String encodeToString = Base64.encodeToString(str2.getBytes(), 0);
        BillingFlowParams.ProductDetailsParams build = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build();
        ArrayList arrayList = new ArrayList();
        arrayList.add(build);
        BillingResult launchBillingFlow = this.mBillingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(arrayList).setObfuscatedAccountId(encodeToString).build());
        Log.v("GooglePlayBillingImpl", "launchInAppBillingFlow: BillingResponse " + launchBillingFlow.getResponseCode() + " " + launchBillingFlow.getDebugMessage());
    }

    public void launchSubscriptionPurchaseFlow(Activity activity, String str, String str2) {
        String str3;
        if (!this.mBillingClient.isReady()) {
            Log.v("GooglePlayBillingImpl", "Billing client is not ready when launching purchase flow");
            return;
        }
        Map<String, ProductDetails> map = this.mProductDetailsMap;
        if (map == null) {
            Log.v("GooglePlayBillingImpl", "mProductDetails map is null");
            return;
        }
        ProductDetails productDetails = map.get(str);
        if (productDetails == null) {
            Log.v("GooglePlayBillingImpl", "Unable to find SKU");
            return;
        }
        this.mSkuInProgress = str;
        try {
            JSONObject jSONObject = (JSONObject) new JSONTokener(str2).nextValue();
            if (jSONObject.has("subscription_id")) {
                str3 = jSONObject.getString("subscription_id");
                if (str3.isEmpty()) {
                    str3 = mNewSubscriptionTag;
                }
            } else {
                str3 = "";
            }
            if (jSONObject.has("world_name")) {
                this.mWorldName = jSONObject.getString("world_name");
            }
            String encodeToString = Base64.encodeToString(new JSONObject(jSONObject, new String[]{"xuid"}).toString().getBytes(), 0);
            String encodeToString2 = str3.isEmpty() ? "" : Base64.encodeToString(str3.getBytes(), 0);
            List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
            if (subscriptionOfferDetails != null && !subscriptionOfferDetails.isEmpty()) {
                BillingFlowParams.Builder obfuscatedAccountId = BillingFlowParams.newBuilder().setProductDetailsParamsList(ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(subscriptionOfferDetails.get(0).getOfferToken()).build())).setObfuscatedAccountId(encodeToString);
                if (!str3.isEmpty()) {
                    obfuscatedAccountId.setObfuscatedProfileId(encodeToString2);
                }
                BillingResult launchBillingFlow = this.mBillingClient.launchBillingFlow(activity, obfuscatedAccountId.build());
                Log.v("GooglePlayBillingImpl", "launchSubscriptionPurchaseFlow: BillingResponse " + launchBillingFlow.getResponseCode() + " " + launchBillingFlow.getDebugMessage());
                return;
            }
            Log.v("GooglePlayBillingImpl", "Subscription Offer Details not found");
        } catch (JSONException e) {
            Log.e("GooglePlayBillingImpl", e.getLocalizedMessage());
        }
    }

    public void queryPurchases() {
        if (this.mBillingClient.isReady()) {
            this.mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp").build(), this);
            if (this.mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() == 0) {
                this.mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("subs").build(), this);
                return;
            }
            return;
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when querying purchases");
    }

    public void querySubscriptions(String[] strArr) {
        if (this.mBillingClient.isReady()) {
            ArrayList arrayList = new ArrayList();
            for (String str : strArr) {
                arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(str).setProductType("subs").build());
            }
            QueryProductDetailsParams build = QueryProductDetailsParams.newBuilder().setProductList(arrayList).build();
            if (this.mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() == 0) {
                this.mBillingClient.queryProductDetailsAsync(build, this);
                return;
            }
            return;
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when querying Subscriptions");
    }

    public void queryConsumables(String[] strArr) {
        if (this.mBillingClient.isReady()) {
            ArrayList arrayList = new ArrayList();
            for (String str : strArr) {
                if (!str.equals("marketplacepass") && !str.equals("marketplacepass.trial")) {
                    arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(str).setProductType("inapp").build());
                }
            }
            this.mBillingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(arrayList).build(), this);
            return;
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when querying Subscriptions");
    }

    public void queryProducts(String[] strArr) {
        if (this.mBillingClient.isReady()) {
            queryConsumables(strArr);
            if (this.mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() == 0) {
                ArrayList arrayList = new ArrayList();
                arrayList.add("marketplacepass");
                arrayList.add("marketplacepass.trial");
                arrayList.add("realms.subscription.monthly.10player.01");
                arrayList.add("realms.subscription.monthly.10player.02");
                arrayList.add("realms.subscription.monthly.10player.03");
                arrayList.add("realms.subscription.monthly.10player.trial");
                arrayList.add("realms.subscription.monthly.2player.01");
                arrayList.add("realms.subscription.monthly.2player.02");
                arrayList.add("realms.subscription.monthly.2player.03");
                querySubscriptions((String[]) arrayList.toArray(new String[arrayList.size()]));
                return;
            }
            return;
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when querying products");
    }

    public void consumeOrAckPurchase(String str) {
        Purchase parseReceipt = parseReceipt(str);
        if (parseReceipt != null) {
            List<String> products = parseReceipt.getProducts();
            if (!products.isEmpty()) {
                String str2 = products.get(0);
                if (this.mProductDetailsMap.containsKey(str2)) {
                    if (this.mProductDetailsMap.get(str2).getProductType().equals("inapp")) {
                        consumePurchase(parseReceipt);
                        return;
                    } else {
                        if (parseReceipt.isAcknowledged()) {
                            return;
                        }
                        acknowledgePurchase(parseReceipt.getPurchaseToken());
                        return;
                    }
                }
                Log.v("GooglePlayBillingImpl", "consumeOrAckPurchase could not find product in product map");
                return;
            }
            Log.v("GooglePlayBillingImpl", "consumeOrAckPurchase has purchase with no products");
            return;
        }
        Log.v("GooglePlayBillingImpl", "consumeOrAckPurchase has null purchase");
    }

    public void acknowledgePurchase(String str) {
        if (this.mBillingClient.isReady()) {
            Log.v("GooglePlayBillingImpl", "Acknowledging purchase");
            this.mBillingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(str).build(), this);
            return;
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when acknowledging purchase");
    }

    public void consumePurchase(Purchase purchase) {
        if (this.mBillingClient.isReady()) {
            if (purchase.getPurchaseState() == 1) {
                this.mBillingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), this);
                return;
            } else {
                Log.v("GooglePlayBillingImpl", "Purchase is not in PurchasedState");
                return;
            }
        }
        Log.v("GooglePlayBillingImpl", "Billing client is not ready when consuming purchase");
    }

    @Override
    public void onConsumeResponse(BillingResult billingResult, String str) {
        Log.v("GooglePlayBillingImpl", "onConsumeResponse: BillingResponse " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        if (billingResult.getResponseCode() == 0) {
            Log.v("GooglePlayBillingImpl", "ConsumeSuccess");
        } else {
            Log.v("GooglePlayBillingImpl", "ConsumeFail");
        }
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.v("GooglePlayBillingImpl", "onAcknowledgePurchaseResponse: BillingResponse " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        if (billingResult.getResponseCode() == 0) {
            Log.v("GooglePlayBillingImpl", "AckSuccess");
        } else {
            Log.v("GooglePlayBillingImpl", "AckFail");
        }
    }

    @Override
    public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> list) {
        Log.v("GooglePlayBillingImpl", "onQueryPurchasesResponse: BillingResponse " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        if (billingResult.getResponseCode() == 0) {
            ArrayList arrayList = new ArrayList();
            Iterator<Purchase> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Purchase next = it.next();
                List<String> products = next.getProducts();
                if (!products.isEmpty()) {
                    String str = products.get(0);
                    arrayList.add(new com.mojang.minecraftpe.store.Purchase(str, (str.equals("marketplacepass") || str.equals("marketplacepass.trial")) ? str : "", createReceipt(next), next.getPurchaseState() == 1));
                }
            }
            Log.v("GooglePlayBillingImpl", "onQueryPurchasesResponse: num of purchases sent to c++ " + arrayList.size());
            if (this.mHasTelemetryBeenSentThisSession && arrayList.isEmpty()) {
                return;
            }
            this.mListener.onQueryPurchasesSuccess((com.mojang.minecraftpe.store.Purchase[]) arrayList.toArray(new com.mojang.minecraftpe.store.Purchase[0]));
            this.mHasTelemetryBeenSentThisSession = true;
            return;
        }
        this.mListener.onQueryPurchasesFail();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
        int responseCode = billingResult.getResponseCode();
        Log.v("GooglePlayBillingImpl", "onPurchasesUpdated: BillingResponse " + responseCode + " " + billingResult.getDebugMessage());
        if (responseCode != 0) {
            if (responseCode == 1) {
                if (list != null) {
                    Iterator<Purchase> it = list.iterator();
                    while (it.hasNext()) {
                        this.mListener.onPurchaseCanceled(it.next().getProducts().get(0));
                    }
                    return;
                }
                this.mListener.onPurchaseCanceled(this.mSkuInProgress);
                return;
            }
            if (list != null) {
                Iterator<Purchase> it2 = list.iterator();
                while (it2.hasNext()) {
                    this.mListener.onPurchaseFailed(it2.next().getProducts().get(0));
                }
                return;
            }
            this.mListener.onPurchaseFailed(this.mSkuInProgress);
            return;
        }
        for (Purchase purchase : list) {
            int purchaseState = purchase.getPurchaseState();
            List<String> products = purchase.getProducts();
            if (purchaseState == 1 && !products.isEmpty()) {
                if (Security.verifyPurchase(this.mSignatureBase64, purchase.getOriginalJson(), purchase.getSignature())) {
                    String str = products.get(0);
                    if (!str.equals("marketplacepass") && !str.equals("marketplacepass.trial")) {
                        str = "";
                    }
                    this.mListener.onPurchaseSuccessful(purchase.getProducts().get(0), str, createReceipt(purchase));
                } else {
                    this.mListener.onPurchaseFailed(purchase.getProducts().get(0));
                }
            } else {
                Log.v("GooglePlayBillingImpl", "onPurchasesUpdated: PurchaseState " + purchaseState);
                this.mListener.onPurchasePending(purchase.getProducts().get(0));
            }
        }
    }

    @Override
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
        String str;
        String str2;
        int responseCode = billingResult.getResponseCode();
        Log.v("GooglePlayBillingImpl", "onProductDetailsResponse: BillingResponse " + responseCode + " " + billingResult.getDebugMessage());
//        if (responseCode == 0) {
//            ArrayList arrayList = new ArrayList();
//            for (ProductDetails productDetails : list) {
//                List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
//                ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
//                if (oneTimePurchaseOfferDetails != null) {
//                    str = oneTimePurchaseOfferDetails.getFormattedPrice();
//                    str2 = oneTimePurchaseOfferDetails.getPriceCurrencyCode();
//                } else if (subscriptionOfferDetails == null || subscriptionOfferDetails.isEmpty()) {
//                    str = "";
//                    str2 = "";
//                } else {
//                    List<ProductDetails.PricingPhase> pricingPhaseList = subscriptionOfferDetails.get(0).getPricingPhases().getPricingPhaseList();
//                    if (!pricingPhaseList.isEmpty()) {
//                        ProductDetails.PricingPhase pricingPhase = pricingPhaseList.get(0);
//                        String formattedPrice = pricingPhase.getFormattedPrice();
//                        str2 = pricingPhase.getPriceCurrencyCode();
//                        str = formattedPrice;
//                    }
//                }
//                arrayList.add(new Product(productDetails.getProductId(), str, str2, str));
//                this.mProductDetailsMap.put(productDetails.getProductId(), productDetails);
//                Log.v("GooglePlayBillingImpl", "onProductDetailsResponse: PRODUCT: " + productDetails.getName());
//            }
//            this.mListener.onQueryProductsSuccess((Product[]) arrayList.toArray(new Product[0]));
//            return;
//        }
//        this.mListener.onQueryProductsFail();
    }

    private String createReceipt(Purchase purchase) {
        JSONObject jSONObject = new JSONObject();
        try {
            if (purchase != null) {
                if (this.mProductDetailsMap != null) {
                    List<String> products = purchase.getProducts();
                    if (!products.isEmpty()) {
                        String str = products.get(0);
                        if (this.mProductDetailsMap.containsKey(str)) {
                            String productType = this.mProductDetailsMap.get(str).getProductType();
                            JSONObject jSONObject2 = new JSONObject(purchase.getOriginalJson());
                            if (jSONObject2.has("obfuscatedAccountId")) {
                                String str2 = new String(Base64.decode(jSONObject2.getString("obfuscatedAccountId"), 0));
                                if (productType.equals("subs") && jSONObject2.has("obfuscatedProfileId")) {
                                    String str3 = new String(Base64.decode(jSONObject2.getString("obfuscatedProfileId"), 0));
                                    if (str3.equals(mNewSubscriptionTag)) {
                                        str3 = "";
                                    }
                                    JSONObject jSONObject3 = new JSONObject(str2);
                                    jSONObject3.put("subscription_id", str3);
                                    jSONObject3.put("world_name", this.mWorldName);
                                    str2 = jSONObject3.toString();
                                }
                                jSONObject2.put("developerPayload", str2);
                                Iterator<String> keys = jSONObject2.keys();
                                while (keys.hasNext()) {
                                    String next = keys.next();
                                    if (next.equals("obfuscatedAccountId") || next.equals("obfuscatedProfileId")) {
                                        keys.remove();
                                    }
                                }
                            }
                            jSONObject.put("itemtype", productType);
                            jSONObject.put("originaljson", jSONObject2.toString());
                            jSONObject.put("signature", purchase.getSignature());
                            Log.v("VERBOSE", jSONObject.toString());
                        } else {
                            Log.v("GooglePlayBillingImpl", "createReceipt could not find product in product map");
                        }
                    } else {
                        Log.v("GooglePlayBillingImpl", "createReceipt has purchase with no products");
                    }
                } else {
                    Log.v("GooglePlayBillingImpl", "skuDetails map was null");
                }
            } else {
                Log.v("GooglePlayBillingImpl", "Null purchase in createReceipt");
            }
            return jSONObject.toString();
        } catch (JSONException e) {
            String str4 = (String) Objects.requireNonNull(e.getLocalizedMessage());
            Log.e("GooglePlayBillingImpl", str4);
            return str4;
        }
    }

    private Purchase parseReceipt(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            return new Purchase(jSONObject.getString("originaljson"), jSONObject.getString("signature"));
        } catch (JSONException e) {
            Log.e("GooglePlayBillingImpl", e.getLocalizedMessage());
            return null;
        }
    }
}
