package com.gudh.power1.energy.model;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Admin on 2016/5/11.
 */

/**
 * read /sys/devices/system/cpu/cpu" + i + "/cpufreq/stats/time_in_state  i为cpu个数
 * 不同频率下使用的时长
 */
public final class CpuFreq extends ProcFile {

    private int cpuNum;
    public static CpuFreq get(int i){
        try {
            return new CpuFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/stats/time_in_state", i);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    protected CpuFreq(String path, int i) throws IOException {
        super(path);
        this.cpuNum = i;
    }

    public HashMap<String, Long> getCpuFreq(){
        HashMap<String, Long> freq = new HashMap<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String[] split = line.split(" ");
            if(split.length >= 2){
                freq.put(String.valueOf(this.cpuNum) + "-" + split[0], Long.valueOf(split[1]));
            }
        }
        return freq;
    }
}
