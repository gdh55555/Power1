package com.gudh.power1.energy.Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;

/**
 * Created by Admin on 2016/5/20.
 */
public class PowermodelReader {
    private String powermodleReader = "PowermodelReader";

    public static void modelReader(String path, InputStream is) {
        BufferedReader br = null;
        try {
            br = !new File(path).exists() ? new BufferedReader(new InputStreamReader(is, "UTF-8")) : new BufferedReader(new FileReader(path));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        if (null != br){
            String readLine;
            try {
                readLine = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                readLine = null;
            }
            if(null == readLine){
                try {
                    br.close();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String[] split = readLine.split(" ");
            SysemInfo.freqNum = Integer.parseInt(split[0]);
            int i = SysemInfo.freqNum;
            SysemInfo.freq = new int[i];
            SysemInfo.freqPower = new float[i];
            SysemInfo.d = (float[][][]) Array.newInstance(Float.TYPE, i, i, 4);
            SysemInfo.e = new float[i];
            int j = 0;
            int k = 1;
            while(j < SysemInfo.freqNum){
                SysemInfo.freq [j++] = Integer.parseInt(split[k++]);
            }
            j = 0;
            while(j < SysemInfo.freqNum){
                SysemInfo.freqPower[j++] = Integer.parseInt(split[k++]);
            }

            for(j = 0; j < SysemInfo.freqNum; j++){
                for(int m = 0; m < SysemInfo.freqNum; m++){
                    int count = 0;
                    while(count < 4){
                        SysemInfo.d[j][m][count++] = Float.parseFloat(split[k++]);
                    }
                }
            }
            j = 0;
            while(j < SysemInfo.freqNum){
                SysemInfo.e[j++] = Float.parseFloat(split[k++]);
            }
            SysemInfo.f = Float.parseFloat(split[k++]);

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
