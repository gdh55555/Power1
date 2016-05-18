package com.gudh.power1.energy.Analysis;

import android.app.ActivityManager;

import com.gudh.power1.energy.model.CpuFreq;
import com.gudh.power1.energy.model.CpuInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 2016/5/12.
 */
public class RunningProcess {

    private static int cpuNum = -1;
    private static long cpuNumCheckTime = 1000; //检测两次cpuNum的时间间隔， 原app是3600000
    private static long cpuNumPreTime = -1;

    public static long[] getRunningProcess(ArrayList arrayList)  {
        if(arrayList.isEmpty())
            return null;
        long[] resultProcess = new long[arrayList.size()];
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"ps"}).getInputStream()));
            bufferedReader.readLine();
            while (true){
                String line = bufferedReader.readLine();
                if(line == null)
                    return resultProcess;
                String[] split = line.split("[\\t]+]");
                if(split.length >= 5){
                    int pid = Integer.parseInt(split[1]);
                    if(arrayList.contains(pid)){
                        resultProcess[arrayList.indexOf(pid)] = Long.parseLong(split[4]); //rss
                    }
                }
            }
        }catch (IOException e){
            return resultProcess;
        }catch (NumberFormatException e) {
            return resultProcess;
        }
    }

    public static ArrayList<Integer>[] getRunningProcess(ActivityManager activityManager){
        ArrayList<Integer>[] arrayLists = new ArrayList[4];
        List runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses.size() > 1) {
            for (int j = 0; j < runningAppProcesses.size(); j++) {
                if (((ActivityManager.RunningAppProcessInfo) runningAppProcesses.get(j)).uid >= 10000) {
                    arrayLists[0].add(Integer.valueOf(((ActivityManager.RunningAppProcessInfo) runningAppProcesses.get(j)).uid));
                    arrayLists[1].add(Integer.valueOf(((ActivityManager.RunningAppProcessInfo) runningAppProcesses.get(j)).pid));
                    arrayLists[2].add(Integer.valueOf(((ActivityManager.RunningAppProcessInfo) runningAppProcesses.get(j)).importance));
                    arrayLists[3].add(Integer.valueOf(-9999));
                }
            }
        }else{
            try{
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps -p | grep ^u0_"}).getInputStream()));
                String str = "";
                while (true) {
                    str = bufferedReader.readLine();
                    if (str == null) {
                        break;
                    } else if (str.startsWith("u0_")) {
                        String[] split = str.split("[ \t]+");
                        if (split.length <= 6) {
                            String uidname = split[0];
                            if (uidname.length() >= 5) {
                                int uid = Integer.parseInt(uidname.substring(4)) + 10000;
                                int pid = Integer.parseInt(split[1]);
                                int prio = Integer.parseInt(split[5]);
                                arrayLists[0].add(uid);
                                arrayLists[1].add(pid);
                                arrayLists[2].add(500);
                                arrayLists[3].add(prio);
                            }
                        }
                    }
                }
                bufferedReader.close();;
            }catch (FileNotFoundException e){

            }catch (IOException e1){

            }catch (NumberFormatException e2){

            }
            arrayLists[0].add(Integer.valueOf(1000));
            arrayLists[1].add(Integer.valueOf(SystemProcess.system_server_pid));
            arrayLists[2].add(Integer.valueOf(500));
            arrayLists[3].add(Integer.valueOf(-9999));
            arrayLists[0].add(Integer.valueOf(1001));
            arrayLists[1].add(Integer.valueOf(SystemProcess.mediaserver_pid));
            arrayLists[2].add(Integer.valueOf(500));
            arrayLists[3].add(Integer.valueOf(-9999));
            arrayLists[0].add(Integer.valueOf(1013));
            arrayLists[1].add(Integer.valueOf(SystemProcess.rild_pid));
            arrayLists[2].add(Integer.valueOf(500));
            arrayLists[3].add(Integer.valueOf(-9999));
        }
        return arrayLists;
    }

    public static void getSystemProcess() {
        String str = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ps").getInputStream()));
            while (true) {
                str = bufferedReader.readLine();
                if (str != null) {
                    String[] split = str.split("[ \t]+");
                    if (split.length >= 9) {
                        if (split[8].equals("system_server")) {
                            SystemProcess.system_server_pid = Integer.parseInt(split[1]);
                        } else if (split[8].equals("/system/bin/mediaserver")) {
                            SystemProcess.mediaserver_pid = Integer.parseInt(split[1]);
                        } else if (split[8].equals("/system/bin/rild")) {
                            SystemProcess.rild_pid = Integer.parseInt(split[1]);
                        }
                    }
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getCpuNum(){
        if(cpuNum <= 0 || System.currentTimeMillis() - cpuNumPreTime > cpuNumCheckTime){
            cpuNum = CpuInfo.getCpuNum();
            cpuNumPreTime = System.currentTimeMillis();
        }
        return cpuNum;
    }

    public static HashMap<String, Long> getCpuInfo(){
        HashMap<String, Long> cpuTime;
        CpuInfo cpuInfo = CpuInfo.get();
        cpuTime = cpuInfo.getCpuTime();
        for(int i = 0; i < getCpuNum(); i++){
            cpuTime.putAll(CpuFreq.get(i).getCpuFreq());
        }
        return cpuTime;
    }
}
