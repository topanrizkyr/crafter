package com.unity3d.myads;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import androidx.multidex.MultiDex;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;



public class MyApplication extends Application implements InvocationHandler {
    private static final int GET_SIGNATURES = 64;
    private Object base;
    private String appPkgName = "";
    private String appPkgInstaller = "com.android.vending";
    private String sign = "3082036c30820254a00302010202044e3ee013300d06092a864886f70d01010505003078310b30090603550406130253453119301706035504080c1053746f636b686f6c6d73204ce2809e6e311230100603550407130953746f636b686f6c6d31123010060355040a13094d6f6a616e6720414231123010060355040b13094d6f6a616e6720414231123010060355040313094d6f6a616e672041";

    public MyApplication() {
        MultiDex.install(this);
    }

    private void gQevHycPLQLEhYsxJAglMKqtxOJwmOPzaRbmk(Context context) {
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Object invoke = cls.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("sPackageManager");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            Class<?> cls2 = Class.forName("android.content.pm.IPackageManager");
            this.base = obj;
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
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        if ("getPackageInfo".equals(method.getName())) {
            String str = (String) objArr[0];
            if ((((Integer) objArr[1]).intValue() & GET_SIGNATURES) != 0 && this.appPkgName.equals(str)) {
                Signature signature = new Signature(this.sign);
                PackageInfo packageInfo = (PackageInfo) method.invoke(this.base, objArr);
                packageInfo.signatures[0] = signature;
                return packageInfo;
            }
        }
        if ("getInstallerPackageName".equals(method.getName())) {
            if (this.appPkgName.equals((String) objArr[0])) {
                return this.appPkgInstaller;
            }
        }
        return method.invoke(this.base, objArr);
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }


    @Override
    protected void attachBaseContext(Context context) {
        gQevHycPLQLEhYsxJAglMKqtxOJwmOPzaRbmk(context);
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
