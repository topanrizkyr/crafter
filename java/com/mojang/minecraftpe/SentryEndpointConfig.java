package com.mojang.minecraftpe;

public class SentryEndpointConfig {
    public String projectId;
    public String publicKey;
    public String url;

    public SentryEndpointConfig(String str, String str2, String str3) {
        this.url = str;
        this.projectId = str2;
        this.publicKey = str3;
    }
}
