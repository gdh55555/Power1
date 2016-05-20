package com.gudh.power1.energy.Analysis;

import android.util.SparseArray;
import android.webkit.DownloadListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

/**
 * Created by Admin on 2016/5/11.
 */
public class PowerCalculator {
    private static String powerCalculator = "PowerCalculator";

    public static void calPower(long preTime, long curTime, boolean isUpdate,SparseArray<ProcessKeeper> pkArray, ArrayList<Integer> copy, CpuCaculator cpuCaculator, HashMap<String, ArrayList> event, ArrayList<long[]> d) {
        SparseArray<HashMap<String, Long>> cpu = calCpu(cpuCaculator.clone());
        SparseArray<SparseArray<Double[]>> cpuUsage = calCpuDetail(cpu);
        HashMap eve = event;    //处理event, 暂时未统计
        for(int i = 0; i < pkArray.size(); i++){
            int keyAt = pkArray.keyAt(i); //uid
            ProcessKeeper pkTmp = pkArray.get(keyAt);
            long sysTime = pkTmp.getSysUserTime()[0] * 10;
            long userTime = pkTmp.getSysUserTime()[1] * 10;
            if(sysTime + userTime > 0){
                int impor = pkTmp.getImprotance();
                if(isUpdate){

                }
                double cpuPower = calCpuPower(sysTime, userTime, impor, preTime, curTime, cpu, cpuUsage);

            }

        }

    }

    private static double calCpuPower(long sysTime, long userTime, int impor, long preTime, long curTime, SparseArray<HashMap<String, Long>> cpu, SparseArray<SparseArray<Double[]>> cpuUsage) {
        if(cpuUsage.size() == 1){
            return calCpuPower1(sysTime, userTime, impor, preTime, curTime, cpuUsage.keyAt(0), cpuUsage);
        }
        else if(cpuUsage.size() == 2){
            return  calCpuPower2(sysTime, userTime, impor, preTime, curTime, cpuUsage.keyAt(0), cpuUsage.keyAt(2), cpuUsage);
        }
        else{
            long max1 = -1;
            long max2 = -1;
            int index1 = 0, index2 = 0;
            long tmp;
            for(int i = 0; i < cpu.size(); i++){
                int key = cpu.keyAt(1);
                tmp = cpu.get(key).get("s") + cpu.get(key).get("u");
                if(max1 < tmp){
                    max1 = tmp;
                    index1 = key;
                }
            }
            for(int i = 0; i < cpu.size(); i++){
                int key = cpu.keyAt(1);
                if(key != index1) {
                    tmp = cpu.get(key).get("s") + cpu.get(key).get("u");
                    if (max2 < tmp) {
                        max2 = tmp;
                        index2 = key;
                    }
                }
            }
            return calCpuPower2(sysTime, userTime, impor, preTime, curTime, index1, index2, cpuUsage);
        }
    }

    private static double calCpuPower2(long sysTime, long userTime, int impor, long preTime, long curTime, int keyat1, int keyat2, SparseArray<SparseArray<Double[]>> cpuUsage) {
        long total = sysTime + userTime;
        long time = impor == 0 ? curTime - preTime : total;
        double ratio1 = 0.0d;
        double ratio2 = 0.0d;
        int i, j;
        i = j = SysemInfo.freqNum;
        double power = 0.0d;
        //寻找较小的ratio
        while(true){
            if(ratio1 <= 0.0d){
                i--;
                if(i < 0)
                    break;
                ratio1 = cpuUsage.get(keyat1).get(SysemInfo.freq[i]) != null ?  cpuUsage.get(keyat1).get(SysemInfo.freq[i])[0] : 0.0d;
            }
            if(ratio2 <= 0.0d){
                j--;
                if(j < 0)
                    break;
                ratio2 = cpuUsage.get(keyat2).get(SysemInfo.freq[j]) != null ?  cpuUsage.get(keyat2).get(SysemInfo.freq[j])[0] : 0.0d;
            }
            double min = Math.min(ratio1, ratio2);
            if(min > 0.0d){
                ratio1 -= min;
                ratio2 -= min;
                power += total / 1000.0d * min * SysemInfo.d[i][j][3];
                time -= total * min;
            }
        }
        while(i >= 0){
            ratio1 = cpuUsage.get(keyat1).get(SysemInfo.freq[i]) != null ?  cpuUsage.get(keyat1).get(SysemInfo.freq[i])[0] : 0.0d;
            power += total / 1000.0d * ratio1 * SysemInfo.freqPower[i];
            time -= total * ratio1;
        }
        while(j >= 0){
            ratio2 = cpuUsage.get(keyat2).get(SysemInfo.freq[j]) != null ?  cpuUsage.get(keyat2).get(SysemInfo.freq[j])[0] : 0.0d;
            power += total / 1000.0d * ratio2 * SysemInfo.freqPower[i];
            time -= total * ratio2;
        }
        return power + time / 1000.0d * SysemInfo.f;
    }

    private static double calCpuPower1(long sysTime, long userTime, int impor, long preTime, long curTime, int key, SparseArray<SparseArray<Double[]>> cpuUsage) {
        long total = sysTime + userTime;
        long time = impor == 0 ? curTime - preTime : total;
        int i = 0;
        double power = 0.0d;
        while(i < SysemInfo.freqNum){
            int freq = SysemInfo.freq[i];
            if(cpuUsage.get(key).get(freq) != null){
                power += total / 1000.0d * cpuUsage.get(key).get(freq)[0] * SysemInfo.freqPower[i];
                time -= total * cpuUsage.get(key).get(freq)[0];
            }
        }
        return time > 0 ? power + time / 1000.0d * SysemInfo.f : power;
    }

    private static SparseArray<SparseArray<Double[]>> calCpuDetail(SparseArray<HashMap<String, Long>> cpu) {
        int i = 0;
        SparseArray<SparseArray<Double>> count= new SparseArray<>();
        SparseArray<SparseArray<Double[]>> result= new SparseArray<>();
        int totalJeffies = 0;
        while(i < cpu.size()){
            int keyAt = cpu.keyAt(i);
            SparseArray<Double> sp = new SparseArray<>();
            count.append(keyAt, sp);
            HashMap<String, Long> hashMap = cpu.get(keyAt);
            long jeffies = hashMap.get("s") + hashMap.get("u");
            totalJeffies += jeffies;
            //暂时不考虑误差问题
            if(hashMap.size() == 3){
                int maxFreq = SysemInfo.freq[SysemInfo.freqNum-1];
                hashMap.put(String.valueOf(maxFreq), jeffies);
                sp.put(maxFreq, 1.0d);
            }
            else {
                /*for (Map.Entry<String, Long> entry : hashMap.entrySet()) {
                    String key = entry.getKey();
                    if (!(key.equals("s") || key.equals("u") || key.equals("n"))) {
                        sp.put(Integer.parseInt(key), 1.0d);
                    }
                }*/
                long tmp = 0;
                for(int j = 0; j < SysemInfo.freqNum; j++){
                    int freq = SysemInfo.freq[j];
                    if(hashMap.containsKey(String.valueOf(freq)) && hashMap.get(String.valueOf(freq)) > 0){
                        tmp += hashMap.get(String.valueOf(freq));   //不同频率的总时长
                    }
                }
                if(tmp < jeffies){
                    int freq = SysemInfo.freq[SysemInfo.freqNum/2];
                    if(hashMap.containsKey(String.valueOf(freq))){
                        hashMap.put(String.valueOf(freq), hashMap.get(String.valueOf(freq)) + jeffies - tmp);
                    }
                    else
                        hashMap.put(String.valueOf(freq),  jeffies - tmp);
                }
                tmp = jeffies;
                for(int j = SysemInfo.freqNum-1; j >= 0; j--){
                    int freq = SysemInfo.freq[j];
                    if(hashMap.containsKey(String.valueOf(freq))){
                        if(tmp < hashMap.get(String.valueOf(freq))){
                            sp.put(freq, tmp * 1.0d / hashMap.get(String.valueOf(freq)));
                        }
                        tmp -= hashMap.get(String.valueOf(freq));
                        sp.put(freq, 1.0d);
                    }
                }

            }
            i++;
        }
        for(i = 0; i < cpu.size(); i++){
            int keyAt = cpu.keyAt(i);
            SparseArray<Double[]> sp = new SparseArray<>();
            result.append(keyAt, sp);
            for(int j = 0; j < count.get(keyAt).size(); j++){
                int key = count.get(keyAt).keyAt(j);
                double value = count.get(keyAt).get(key);
                if(totalJeffies > 0 && value > 0.0d && cpu.get(keyAt).containsKey(key) && cpu.get(keyAt).get(key) > 0){
                    Double[] v = new Double[2];
                    v[0] = cpu.get(keyAt).get(key) *value / totalJeffies;
                    v[1] = cpu.get(keyAt).get(key) *(1.0d * value) / totalJeffies;
                    sp.append(key, v);
                }
            }
        }
        return result;
    }

    //不同cpu 对应不同频率的时长 已经cpu使用情况进行保留
    private static SparseArray<HashMap<String, Long>> calCpu(HashMap<String, Long> clone) {
        ArrayList<Integer> tmp = new ArrayList<>();
        SparseArray<HashMap<String, Long>> sa = new SparseArray<>();
        String sb;
        for(Map.Entry<String, Long> entry : clone.entrySet()){
            String[] split= entry.getKey().split("-");
            HashMap<String, Long> point;
            String str = split[1];
            int freq;
            if (str.equals("u") || str.equals("s") || str.equals("n")){
                sb = str;
            }
            else{
                freq = Integer.parseInt(str);
                //处理如果freq不存在的情况，需要折中处理，同时也需要中间变量保存cpu 频率。
                //暂时未采取措施
                sb = new StringBuilder(freq).toString();
            }
            int cpuNum = Integer.parseInt(split[0]);
            if(sa.get(cpuNum) == null){
                HashMap<String, Long> t = new HashMap<>();
                sa.put(cpuNum, t);
                point = t;
            }
            else{
                point = sa.get(cpuNum);
            }
            if(point.containsKey(sb)){
                point.put(sb, entry.getValue() * 10 + point.get(sb));
            }
            else{
                point.put(sb, entry.getValue() * 10);
            }
        }
        for(int i = 0; i < sa.size(); i++){
            HashMap<String, Long> t = sa.get(sa.keyAt(i));
            if(!t.containsKey("s"))
                t.put("s", 0L);
            if(!t.containsKey("u"))
                t.put("u", 0L);
            if(!t.containsKey("n"))
                t.put("n", 0L);
        }
        return sa;
    }
}
