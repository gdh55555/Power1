package com.gudh.power1.energy.Analysis;

import android.util.SparseArray;

import com.gudh.power1.energy.model.Stat;
import com.gudh.power1.energy.model.Task;
import com.gudh.power1.energy.model.TcpNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Admin on 2016/5/11.
 */
public class ProcessKeeper {
    private static String processKeeper = "ProcessKeeper";
    private int tidnum; //tid的数量
    private int uid;
    private int improtance ;
    private String path;
    private ArrayList<Integer> pids; //一个uid对应多个pid
    private ArrayList<Integer> prio;
    private SparseArray<Long> sysTimeSpar = new SparseArray<>();
    private SparseArray<Long> userTimeSpar = new SparseArray<>();
    private ArrayList<Long> newWork = new ArrayList<>();
    private HashMap<String, long[]> tidUsage = new HashMap<>(); // 0 0 -1 -1 -1线程的一些统计
    private HashMap<String, long[]> preTidUsage = new HashMap<>();
    private long systemTime = 0;    //系统使用时间
    private long userTime = 0;      //用户态时间
    private long curTime = -1;      //对应cpu时间
    private long networkTime = -1;  //网络时间更新
    private long pidTime = -1;      //pid时间更新
    private long tcpSnd;            //网络使用情况
    private long tcpRcv;            //网络使用情况
    private ArrayList<long[]> networkUsage = new ArrayList<>(); //存储网络使用情况 uid time snd rcv pre_snd pre_rcv

    public ProcessKeeper(int uid, int importance, int priority, String str){
        this.uid = uid;
        this.pids = new ArrayList<>();
        this.prio = new ArrayList<>();
        this.prio.add(priority);
        this.improtance = importance;
        this.path = str;

    }


    public void addPid(int  pid) {
        this.pids.add(pid);
    }

    public ArrayList<Integer> getPids(){
        return this.pids;
    }

    public void updateTime(long t) {
        this.curTime = t;
    }

    public long getCurTime() {
        return this.curTime;
    }

    public void clearPids() {
        this.pids.clear();
    }

    public void resetImportance() {
        this.improtance = 0;
    }

    public void clearPrio() {
        this.prio.clear();
    }

    public void updateImportance(int importance) {
        this.improtance |= importance;
    }

    public void addPrio(Integer prio) {
        if(!this.prio.contains(prio))
            this.prio.add(prio);
    }

    public boolean isCurrentTime(long t) {
        return this.curTime == t;
    }

    public void getTotalTime(long curTime) {
        if(this.pidTime != curTime){
            this.userTime = 0;
            this.systemTime = 0;
            this.tidUsage.clear();
            this.tidnum = 0;
            Iterator<Integer> it = this.pids.iterator();
            while(it.hasNext()){
                int pid = it.next();
                Stat stat = Stat.get(pid);
                long userTmp = stat.utime();
                long systemTmp = stat.stime();
                if(userTmp > 0 && systemTmp > 0){
                    if(isFisrtPid() && this.sysTimeSpar.get(pid) != null){
                        this.userTime += userTmp - this.userTimeSpar.get(pid);
                        this.systemTime += systemTmp - this.sysTimeSpar.get(pid);
                    }
                    this.sysTimeSpar.put(pid, userTmp);
                    this.userTimeSpar.put(pid, systemTmp);
                    HashMap<String, long[]> tid = Task.getTask(pid);
                    this.tidnum += tid.size();
                    for(Map.Entry<String, long[]> entry : tid.entrySet()){
                        long[] tidUsage = entry.getValue();
                        String tidCmd = entry.getKey();
                        if(tidUsage[0] > 0 && tidUsage[1] > 0){
                            long[] tmp;
                            if(isFisrtPid() && this.preTidUsage.containsKey(tidCmd)){
                                if(!this.tidUsage.containsKey(tidCmd)){
                                    this.tidUsage.put(tidCmd, new long[]{Long.valueOf(0), Long.valueOf(0), Long.valueOf(-1), Long.valueOf(-1), Long.valueOf(1)});
                                }
                                tmp = this.tidUsage.get(tidCmd);
                                tmp[0] = (tmp[0] + tidUsage[0]) - this.preTidUsage.get(tidCmd)[0];
                                tmp[1] = (tmp[1] + tidUsage[1]) - this.preTidUsage.get(tidCmd)[1];
                            }
                            else{
                                if(!this.tidUsage.containsKey(tidCmd)){
                                    this.tidUsage.put(tidCmd, new long[]{Long.valueOf(0), Long.valueOf(0), Long.valueOf(-1), Long.valueOf(-1), Long.valueOf(1)});
                                }
                                tmp = this.tidUsage.get(tidCmd);
                                tmp[0] += tidUsage[0];
                                tmp[1] += tidUsage[1];
                            }
                            this.tidUsage.get(tidCmd)[2] = tidUsage[2];
                            this.tidUsage.get(tidCmd)[3] = tidUsage[3];
                            this.preTidUsage.put(tidCmd, new long[]{tidUsage[0], tidUsage[1]});
                        }
                    }
                }

            }
        }
        this.pidTime = curTime;
    }

    public void updateNetUsage(long curTime) {
        if(this.networkTime != curTime){
            TcpNetwork tcpNetwork = TcpNetwork.get(this.uid);
            long[] usage = new long[]{tcpNetwork.getSnd(), tcpNetwork.getRcv()};
            if(usage[0] >= 0 && usage[1] >= 0){
                long snd = isFirstNet() ? usage[0] - this.tcpSnd : 0;
                long rcv = isFirstNet() ? usage[1] - this.tcpRcv : 0;
                this.tcpSnd = usage[0];
                this.tcpRcv = usage[1];
                if(snd > 0 && rcv > 0){
                    this.networkUsage.add(new long[]{Long.valueOf(this.networkTime), Long.valueOf(curTime),
                            Long.valueOf(snd), Long.valueOf(rcv), Long.valueOf(this.tcpSnd), Long.valueOf(this.tcpRcv)});
                }
            }
        }
    }

    public boolean isFirstNet() {
        return this.networkTime != -1;
    }

    public boolean isFisrtPid() {
        return this.pidTime != -1;
    }
}
