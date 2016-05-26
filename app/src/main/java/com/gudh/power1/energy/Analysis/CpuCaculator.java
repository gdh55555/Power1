package com.gudh.power1.energy.Analysis;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Admin on 2016/5/16.
 */
public class CpuCaculator {
    long preTime = -1;
    private HashMap<String, Long> pre = new HashMap<>();
    private HashMap<String, Long> total = new HashMap<>();

    /**
     * 根据最新的/proc/stat的情况统计
     * @param curTime  time
     */
    public void cpuCal(long curTime)  {
        if(curTime != this.preTime){
            HashMap<String, Long> cpuTime = RunningProcess.getCpuInfo();
            if (isFirst()) {
                for (Map.Entry<String, Long> entry : cpuTime.entrySet()) {
                    String str = entry.getKey();
                    if(this.pre.containsKey(str) && ((entry.getValue() - this.pre.get(str)) > 0)){
                        this.total.put(str, (this.total.containsValue(str) ? this.total.get(str): 0) + entry.getValue() - this.pre.get(str));
                    }
                }
            }
            this.pre = (HashMap<String, Long>) cpuTime.clone();     //上个时间的hashmap
            this.preTime = curTime;                                 //上个时间
            }
    }

    private boolean isFirst() {
        return this.preTime != -1;
    }

    public HashMap<String, Long> clone(){
        return (HashMap<String, Long>) this.total.clone();
    }

    public void clear(){
        this.total.clear();
    }


}
