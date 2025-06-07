package com.unity3d.myads;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchaseActivity extends AppCompatActivity {

    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBillingClient();  // Inicializar cliente de facturación

        checkNoAdsPurchased(isPurchased -> {
            if (isPurchased) {
                logMessage("No ads purchased!");
            } else {
                logMessage("Ads are still enabled");
            }
        });

    }

    private void initBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener((billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            handlePurchase(purchase);
                        }
                    } else {
                        logMessage("Error en la actualización de la compra: " + billingResult.getDebugMessage());
                    }
                })
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    logMessage("BillingClient initialized successfully");
                    queryProductDetails();
                } else {
                    logMessage("Error en la configuración: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                logMessage("Billing service disconnected");
            }
        });
    }




    private void queryProductDetails() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId("noads")
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !productDetailsList.isEmpty()) {
                for (ProductDetails productDetails : productDetailsList) {
                    if (productDetails.getProductId().equals("noads")) {
                        logMessage("Product Details loaded successfully: " + productDetails.getTitle());
                        launchBillingFlow(productDetails);
                    }
                }
            } else {
                logMessage("Failed to load product details. Response code: "
                        + billingResult.getResponseCode() + ", Debug message: " + billingResult.getDebugMessage());
            }
        });
    }

    private void launchBillingFlow(ProductDetails productDetails) {
        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                .build();

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productDetailsParams))
                .build();

        billingClient.launchBillingFlow(this, flowParams);
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        removeAds();
                    }
                });
            } else {
                removeAds();
            }
        }
    }

    private void removeAds() {
        SharedPreferences preferences = getSharedPreferences("isNoads", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isNoads", true);
        editor.apply();

        // Realizar alguna acción adicional si es necesario, como cerrar anuncios o desactivar la publicidad.
        Toast.makeText(this, "Anuncios desactivados. Disfruta del juego sin interrupciones.", Toast.LENGTH_SHORT).show();
        finish(); // Si deseas cerrar la actividad después de la compra
    }


    private void logMessage(String message) {

        Log.d("PurchaseActivity", message);
    }




    //close the console and open it again okey wait tihis work wait
    public void checkNoAdsPurchased(final PurchaseCheckCallback callback) {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS) // Cambiado a SUBS
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            boolean noAdsPurchased = false;
            for (Purchase purchase : purchases) {
                if (purchase.getProducts().contains("noads") &&
                        purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    noAdsPurchased = true;
                    SharedPreferences preferences = getSharedPreferences("isNoads", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isNoads", true);
                    editor.apply();
                    logMessage("Purchase done. Enjoy the game now.");
                    break;
                }
            }
            callback.onPurchaseChecked(noAdsPurchased);
        });
    }

    public interface PurchaseCheckCallback {
        void onPurchaseChecked(boolean isPurchased);
    }
}
