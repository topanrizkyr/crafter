package com.unity3d.myads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Application extends android.app.Application implements InvocationHandler, android.app.Application.ActivityLifecycleCallbacks {
    private static final int GET_SIGNATURES = 64;
    private String appPkgName = "";
    private Object base;
    private byte[][] sign;

    private void hook(Context context) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(Base64.decode("AQAAA3AwggNsMIICVKADAgECAgROPuATMA0GCSqGSIb3DQEBBQUAMHgxCzAJBgNVBAYTAlNFMRkw\nFwYDVQQIDBBTdG9ja2hvbG1zIEzigJ5uMRIwEAYDVQQHEwlTdG9ja2hvbG0xEjAQBgNVBAoTCU1v\namFuZyBBQjESMBAGA1UECxMJTW9qYW5nIEFCMRIwEAYDVQQDEwlNb2phbmcgQUIwHhcNMTEwODA3\nMTg1NzIzWhcNMzgxMjIzMTg1NzIzWjB4MQswCQYDVQQGEwJTRTEZMBcGA1UECAwQU3RvY2tob2xt\ncyBM4oCebjESMBAGA1UEBxMJU3RvY2tob2xtMRIwEAYDVQQKEwlNb2phbmcgQUIxEjAQBgNVBAsT\nCU1vamFuZyBBQjESMBAGA1UEAxMJTW9qYW5nIEFCMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\nCgKCAQEAhD5zm6F0AVIE/N3rl8MQ1v/HAbQI59Oy/EME8RQp99hORYmnWPx2HufFQ4K4sZQYrqGq\n5pxFXvxGqpWnKLUvCtxQbTFbs3KHaoMuDYtLSjfy1XtAPB6O4nshh1OsNoTEy3rlU7TNsMFHK2Nu\nZZuZKqIWvEEIcif8A83n+i20yHcU4D7ZN1/610Uu92ccT0Ls3K4SH8oDb7LKMd6O/ahllbngdNx0\nbKv0wLQfj4vyKhzX7HACDKNuigEOeuoOFa2o33YbxqHCbnSp+kXxdgwlynR0uz/udFgm5BgCtufp\niRrPJShz7JtYIy0rk3CzWy7s4oGZZlxQVtPJx7zwDnZg9wIDAQABMA0GCSqGSIb3DQEBBQUAA4IB\nAQAbeYC/oCRcgawXwI3hV9Cr0EcIPDOX4t9+9f320t2U1LZEJuf+o3O7hwehRx4jk0FOUPTjPlld\nTSZ0Ysm2KW6FFSQw14O98ksP64m0xpvRuBF6hjCXBk+OdqUM547WkdRRqNiyh81vwXgmleUyNuPg\nUyRcylMLhBmWgO0AG2PD2RJg010ZIvJQ8h3mk9VEukO+hWOIbvR62b6wcPRcmsRxe+AGF6dzQajz\nMBtT+F1Nbep4sThFmvMipQWTuUx30ElO0ejRPmtumztSDPgXi9TEbbv6XQwe8hJJWg1+u7XEF/Jw\nSfn2NW4NLAV5a5jWr9N/qJl/2sanTavjo4fddiXG\n", 0)));
            byte[][] bArr = new byte[dataInputStream.read() & 255][];
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = new byte[dataInputStream.readInt()];
                dataInputStream.readFully(bArr[i]);
            }
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Object invoke = cls.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("sPackageManager");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            Class<?> cls2 = Class.forName("android.content.pm.IPackageManager");
            this.base = obj;
            this.sign = bArr;
            this.appPkgName = context.getPackageName();
            Object newProxyInstance = Proxy.newProxyInstance(cls2.getClassLoader(), new Class[]{cls2}, this);
            declaredField.set(invoke, newProxyInstance);
            PackageManager packageManager = context.getPackageManager();
            Field declaredField2 = packageManager.getClass().getDeclaredField("mPM");
            declaredField2.setAccessible(true);
            declaredField2.set(packageManager, newProxyInstance);
            System.out.println("PmsHook success.");
        } catch (Exception e) {
            System.err.println("PmsHook failed.");
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();




    }

    @Override
    protected void attachBaseContext(Context context) {
        hook(context);
        super.attachBaseContext(context);




    }

    @Override
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        if ("getPackageInfo".equals(method.getName())) {
            String str = (String) objArr[0];
            if ((((Number) objArr[1]).intValue() & GET_SIGNATURES) != 0 && this.appPkgName.equals(str)) {
                PackageInfo packageInfo = (PackageInfo) method.invoke(this.base, objArr);
                packageInfo.signatures = new Signature[this.sign.length];
                for (int i = 0; i < packageInfo.signatures.length; i++) {
                    packageInfo.signatures[i] = new Signature(this.sign[i]);
                }
                return packageInfo;
            }
        }
        return method.invoke(this.base, objArr);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
