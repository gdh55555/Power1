package com.gudh.power1.energy.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Admin on 2016/5/11.
 */

/**
 * 遍历/proc/[pid]/task下的目录获取值
 */
public class Task {
    public static HashMap<String, long[]> getTask(int i){
        HashMap<String, long[]> hashMap = new HashMap();
        if (new File("/proc/" + i + "/task").exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(
                        new String[]{"sh", "-c", "cat "  + "/proc/" + i + "/task/*/stat"}).getInputStream()));
                String str = "";
                while (true) {
                    str = bufferedReader.readLine();
                    if (str == null) {
                        break;
                    }
                    int indexOf = str.indexOf("(");
                    int lastIndexOf = str.lastIndexOf(")");
                    if (indexOf >= 1 && indexOf + 1 <= lastIndexOf && lastIndexOf + 2 <= str.length()) {
                        String substring = str.substring(0, indexOf - 1);
                        substring = new StringBuilder(String.valueOf(substring)).append("-").append(str.substring(indexOf + 1, lastIndexOf)).toString();
                        String[] split = str.substring(lastIndexOf + 2).split(" ");
                        if (split.length >= 13) {
                            long prio = -1;
                            int stat = -1;
                            try {
                                long utime = Long.parseLong(split[11]); //utime
                                long stime = Long.parseLong(split[12]); //stime
                                if (split[0].length() == 1) {
                                    stat = split[0].charAt(0);
                                }
                                if (split.length >= 37) {
                                    prio = Long.parseLong(split[36]); //prio
                                }
                                /*pid-cmd -> {stime utime, prio, stat}*/
                                hashMap.put(substring, new long[]{stime, utime, prio, (long) stat});
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hashMap;
    }
}
