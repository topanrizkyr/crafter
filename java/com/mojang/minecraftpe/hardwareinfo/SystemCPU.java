package com.mojang.minecraftpe.hardwareinfo;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class SystemCPU implements Comparable<SystemCPU> {
    protected static final String SYSTEM_CPU_PATH = "/sys/devices/system/cpu";
    private final BitSet CPU_BIT_MASK;
    private final int CPU_ID;
    private final String PATH;
    private BitSet siblingCoresMask;
    private long cpuMinFreq = 0;
    private long cpuMaxFreq = 0;

    SystemCPU(int i) {
        this.CPU_ID = i;
        BitSet bitSet = new BitSet();
        this.CPU_BIT_MASK = bitSet;
        bitSet.set(i);
        this.PATH = "/sys/devices/system/cpu/cpu" + i;
    }

    public int getCPUId() {
        return this.CPU_ID;
    }

    public BitSet getCPUMask() {
        return (BitSet) this.CPU_BIT_MASK.clone();
    }

    public long getMinFrequencyHz() {
        return this.cpuMinFreq;
    }

    public long getMaxFrequencyHz() {
        return this.cpuMaxFreq;
    }

    void updateCPUFreq() {
        long tryReadFreq = tryReadFreq("cpuinfo", "min");
        this.cpuMinFreq = tryReadFreq;
        if (tryReadFreq == 0) {
            this.cpuMinFreq = tryReadFreq("scaling", "min");
        }
        long tryReadFreq2 = tryReadFreq("cpuinfo", "max");
        this.cpuMaxFreq = tryReadFreq2;
        if (tryReadFreq2 == 0) {
            this.cpuMaxFreq = tryReadFreq("scaling", "max");
        }
    }

    private long tryReadFreq(String str, String str2) {
        File file = new File(this.PATH + "/cpufreq/" + str + "_" + str2 + "_freq");
        if (!file.exists() || !file.canRead()) {
            return 0L;
        }
        try {
            Scanner scanner = new Scanner(file);
            try {
                long nextInt = scanner.nextInt();
                scanner.close();
                return nextInt;
            } finally {
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    boolean exists() {
        return getSystemCPUFile() != null;
    }

    private File getSystemCPUFile() {
        File file = new File(this.PATH);
        if (!file.exists()) {
            Log.v("MCPE", "cpu" + this.CPU_ID + " directory doesn't exist: " + this.PATH);
            return null;
        }
        if (file.canRead()) {
            return file;
        }
        Log.v("MCPE", "Cannot read directory: " + this.PATH);
        return null;
    }

    public String getSiblingString() {
        String str = this.PATH + "/topology";
        File file = new File(str + "/core_siblings_list");
        if (!file.exists() || !file.canRead()) {
            Log.v("MCPE", "Cannot read file: " + file.getAbsolutePath());
            file = new File(str + "/package_cpus_list");
        }
        if (!file.exists() || !file.canRead()) {
            Log.v("MCPE", "Cannot read file: " + file.getAbsolutePath());
            return null;
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            try {
                String readLine = bufferedReader.readLine();
                bufferedReader.close();
                return readLine;
            } finally {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BitSet getSiblingsMask() {
        if (this.siblingCoresMask == null) {
            this.siblingCoresMask = retrieveSiblingsMask();
        }
        return this.siblingCoresMask;
    }

    private BitSet retrieveSiblingsMask() {
        File file;
        String[] strArr = {"/core_siblings", "/package_cpus"};
        String str = this.PATH + "/topology";
        int i = 0;
        while (true) {
            if (i >= 2) {
                file = null;
                break;
            }
            file = new File(str + strArr[i]);
            if (file.exists() && file.canRead()) {
                break;
            }
            i++;
        }
        if (file == null) {
            Log.v("MCPE", "Cannot read file: " + file.getAbsolutePath());
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            try {
                String[] split = bufferedReader.readLine().split(",");
                BitSet bitSet = new BitSet(split.length * 32);
                for (int i2 = 0; i2 < split.length; i2 += 2) {
                    int i3 = i2 + 1;
                    bitSet.or(BitSet.valueOf(new long[]{((i3 < split.length ? Long.parseLong(split[i3].trim().toUpperCase(), 16) : 0L) << 32) | Long.parseLong(split[i2].trim().toUpperCase(), 16)}));
                }
                bufferedReader.close();
                return bitSet;
            } finally {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<SystemCPU> getSiblingCPUs() {
        List<SystemCPU> cpus = CPUTopologyInfo.getInstance().getCPUS();
        TreeSet treeSet = new TreeSet();
        BitSet siblingsMask = getSiblingsMask();
        if (siblingsMask != null && siblingsMask.length() != 0) {
            int i = 0;
            while (true) {
                int nextSetBit = siblingsMask.nextSetBit(i);
                if (nextSetBit < 0) {
                    break;
                }
                treeSet.add(cpus.get(nextSetBit));
                i = nextSetBit + 1;
            }
        }
        return treeSet;
    }

    public int hashCode() {
        return this.CPU_ID;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && obj.getClass() == getClass() && ((SystemCPU) obj).CPU_ID == this.CPU_ID;
    }

    public String toString() {
        return this.PATH;
    }

    @Override
    public int compareTo(SystemCPU systemCPU) {
        return Integer.compare(this.CPU_ID, systemCPU.CPU_ID);
    }
}
