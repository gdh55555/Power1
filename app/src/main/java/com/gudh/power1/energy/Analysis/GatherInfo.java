package com.gudh.power1.energy.Analysis;

import android.os.Process;

/**
 * Created by Admin on 2016/5/16.
 */

import com.gudh.power1.energy.model.TcpNetwork;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GatherInfo implements Runnable{

    private long curTime = -1;      //当前时间
    private long preTime = -1;
    private String timeFormat;      //时间格式

    private LogWrite logWrite;
    private CpuCaculator cpuCaculator;

    private void cpuCal(long i){
        cpuCaculator.cpuCal(i);
    }

    @Override
    public void run() {
        Process.setThreadPriority(-2);
        RunningProcess.getSystemProcess(); //获得系统进程
        TcpNetwork.FileIsExist();          //判断/proc/uid_stat是否存在
        this.curTime = System.currentTimeMillis();  //当前时间
        this.preTime = this.curTime - 1000;
        //未实现  等待实现
        Date date = new java.sql.Date(this.curTime);  //mysql的data
        this.logWrite.init(this.timeFormat, this.curTime, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date)); //时间

        if(this.preTime < this.curTime){
           cpuCal(this.curTime);
        }


    }
}
