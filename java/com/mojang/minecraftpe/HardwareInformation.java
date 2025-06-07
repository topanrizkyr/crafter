package com.mojang.minecraftpe;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.provider.Settings;
import androidx.core.os.EnvironmentCompat;
import com.mojang.minecraftpe.hardwareinfo.CPUCluster;
import com.mojang.minecraftpe.hardwareinfo.CPUTopologyInfo;
import com.mojang.minecraftpe.platforms.Platform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardwareInformation {
    private static final CPUInfo cpuInfo = getCPUInfo();
    private final ApplicationInfo appInfo;
    private final Context context;
    private final PackageManager packageManager;

    public HardwareInformation(Context context) {
        this.packageManager = context.getPackageManager();
        this.appInfo = context.getApplicationInfo();
        this.context = context;
    }

    public static class CPUInfo {
        private final Map<String, String> cpuLines;
        private final int numberCPUCores;

        public CPUInfo(Map<String, String> map, int i) {
            this.cpuLines = map;
            this.numberCPUCores = i;
        }

        String getCPULine(String str) {
            return this.cpuLines.containsKey(str) ? this.cpuLines.get(str) : "";
        }

        int getNumberCPUCores() {
            return this.numberCPUCores;
        }
    }

    public static String getDeviceModelName() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        if (str2.startsWith(str)) {
            return str2.toUpperCase(Locale.ENGLISH);
        }
        return str.toUpperCase(Locale.ENGLISH) + " " + str2;
    }

    public String getAndroidVersion() {
        if (((MainActivity) this.context).isChromebook()) {
            return "ChromeOS " + Build.VERSION.RELEASE;
        }
        return "Android " + Build.VERSION.RELEASE;
    }

    public static String getLocale() {
        return Locale.getDefault().toString();
    }

    public static String getCPUType() {
        return Platform.createPlatform(false).getABIS();
    }

    public static String getCPUName() {
        CPUInfo cPUInfo = cpuInfo;
        String cPULine = cPUInfo.getCPULine("model name");
        return !cPULine.isEmpty() ? cPULine : cPUInfo.getCPULine("Hardware");
    }

    public static String getSoCName() {
        String cPULine;
        if (Build.VERSION.SDK_INT >= 31) {
            cPULine = Build.SOC_MODEL.equals(EnvironmentCompat.MEDIA_UNKNOWN) ? "" : Build.SOC_MODEL;
        } else {
            cPULine = cpuInfo.getCPULine("Hardware");
        }
        return cPULine.isEmpty() ? cpuInfo.getCPULine("model name") : cPULine;
    }

    public static String getCPUFeatures() {
        return cpuInfo.getCPULine("Features");
    }

    public static int getNumCores() {
        int cPUCount = CPUTopologyInfo.getInstance().getCPUCount();
        return cPUCount == 0 ? cpuInfo.getNumberCPUCores() : cPUCount;
    }

    public static int getNumClusters() {
        return CPUTopologyInfo.getInstance().getCPUClusterCount();
    }

    public static int getPerformanceCoreCount() {
        CPUCluster[] clusterArray = CPUTopologyInfo.getInstance().getClusterArray();
        int numCores = getNumCores();
        if (clusterArray.length == 0) {
            if (numCores > 2) {
                return numCores >> 1;
            }
            return 1;
        }
        if (clusterArray.length == 1) {
            return numCores;
        }
        CPUCluster cPUCluster = clusterArray[0];
        long maxFreq = cPUCluster.getMaxFreq();
        for (int i = 1; i < clusterArray.length; i++) {
            if (clusterArray[i].getMaxFreq() < maxFreq) {
                cPUCluster = clusterArray[i];
            }
        }
        return numCores - cPUCluster.getClusterCoreCount();
    }

    public static CPUInfo getCPUInfo() {
        HashMap hashMap = new HashMap();
        int i = 0;
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                Pattern compile = Pattern.compile("([\\w\\ ]*)\\s*:\\s([^\\n]*)");
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    Matcher matcher = compile.matcher(readLine);
                    if (matcher.find() && matcher.groupCount() == 2) {
                        if (!hashMap.containsKey(matcher.group(1))) {
                            hashMap.put(matcher.group(1), matcher.group(2));
                        }
                        if (matcher.group(1).contentEquals("processor")) {
                            i++;
                        }
                    }
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new CPUInfo(hashMap, i);
    }

    public String getSecureId() {
        return Settings.Secure.getString(this.context.getContentResolver(), "android_id");
    }

    public static String getSerialNumber() {
        return Build.SERIAL;
    }

    public static String getBoard() {
        return Build.BOARD;
    }

    public String getInstallerPackageName() {
        PackageManager packageManager = this.packageManager;
        return (packageManager == null || this.appInfo == null) ? "" : packageManager.getInstallerPackageName(this.context.getPackageName());
    }

    public int getSignaturesHashCode() {
        int i;
        Exception e;
        try {
            i = 0;
            for (Signature signature : this.packageManager.getPackageInfo(this.context.getPackageName(), 64).signatures) {
                try {
                    i ^= signature.hashCode();
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    return i;
                }
            }
        } catch (Exception e3) {
            i = 0;
            e = e3;
        }
        return i;
    }

    public boolean getIsRooted() {
        return checkRootA() || checkRootB() || checkRootC();
    }

    private boolean checkRootA() {
        String str = Build.TAGS;
        return str != null && str.contains("test-keys");
    }

    private boolean checkRootB() {
        String[] strArr = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/app/Superuser.apk", "/data/local/su", "/su/bin/su"};
        for (int i = 0; i < 10; i++) {
            if (new File(strArr[i]).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRootC() {
        String[] strArr = {"eu.chainfire.supersu", "eu.chainfire.supersu.pro"};
        for (int i = 0; i < 2; i++) {
            if (appInstalled(strArr[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean appInstalled(String str) {
        try {
            this.packageManager.getPackageInfo(str, 0);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
}
