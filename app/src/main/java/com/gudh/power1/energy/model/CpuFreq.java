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

    public static HashMap<Long, Long> freq;

    public static CpuFreq get(int i) throws IOException{
        return new CpuFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/stats/time_in_state");
    }

    protected CpuFreq(String path) throws IOException {
        super(path);
        freq = new HashMap<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String[] split = line.split(" ");
            if(split.length >= 2){
                freq.put(Long.valueOf(split[0]), Long.valueOf(split[1]));
            }
        }
    }
}
