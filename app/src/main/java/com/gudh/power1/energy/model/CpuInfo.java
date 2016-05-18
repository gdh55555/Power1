package com.gudh.power1.energy.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Admin on 2016/5/11.
 */

/**
 * read /proc/cpuinfo get cpu nums
 * get cpu info
 */
public final class CpuInfo extends ProcFile{

    public static CpuInfo get() {
        try {
            return new CpuInfo("/proc/stat");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    protected CpuInfo(String path) throws IOException{
        super(path);
    }

    public HashMap<String, Long> getCpuTime(){
        HashMap<String, Long> total = new HashMap<>();
        for(String split : content.split("\n")){
            String[] split2 = split.split(" ");
            if (split2[0].contains("cpu") && !split2[0].equals("cpu")) {
                int cpuId = Integer.parseInt(split2[0].substring(3));
                total.put(String.valueOf(cpuId) + "-u", Long.parseLong(split2[1]));
                total.put(String.valueOf(cpuId) + "-n", Long.parseLong(split2[2]));
                total.put(String.valueOf(cpuId) + "-s", Long.parseLong(split2[3]));
            }
            else if(!split2[0].contains("cpu"))
                break;
        }
        return total;
    }

    public static int getCpuNum() {
        int i = 0;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/cpuinfo"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            String readLine = null;
            try {
                readLine = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (readLine == null) {
                break;
            }
            String[] split = readLine.split("[:\t ]+");
            if (split.length > 1 && split[0].contains("processor")) {
                int parseInt = Integer.parseInt(split[1]);
                if (parseInt > i) {
                    i = parseInt;
                }
            }
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i+1;
    }


}
