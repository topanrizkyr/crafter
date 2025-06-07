package com.mojang.minecraftpe;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;

public class PlayIntegrity {
    public static Task<IntegrityTokenResponse> requestIntegrityToken(Context context, String str) {
        return IntegrityManagerFactory.create(context).requestIntegrityToken(IntegrityTokenRequest.builder().setNonce(str).build());
    }
}
