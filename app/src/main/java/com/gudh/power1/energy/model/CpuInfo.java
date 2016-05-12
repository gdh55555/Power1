package com.gudh.power1.energy.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Admin on 2016/5/11.
 */

/**
 * read /proc/cpuinfo get cpu nums
 */
public class CpuInfo {
    public static int getCpuNum() throws IOException {
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
        bufferedReader.close();
        return i+1;
    }
}
