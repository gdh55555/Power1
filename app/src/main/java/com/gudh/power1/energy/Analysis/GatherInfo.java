package com.gudh.power1.energy.Analysis;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.util.SparseArray;

/**
 * Created by Admin on 2016/5/16.
 */

import com.gudh.power1.energy.Service.PowerService;
import com.gudh.power1.energy.model.TcpNetwork;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class GatherInfo implements Runnable{

    private long curTime = -1;      //当前时间
    private long preTime = -1;
    private String timeFormat;      //时间格式
    private ActivityManager activity;
    private SparseArray<ProcessKeeper> pkArray; //对应uid 和 pk 变量
    private SparseArray<ArrayList<long[]>> rssArray; //貌似是rss
    private HashMap<String, ArrayList> event = new HashMap<>();
    private ArrayList<long[]> D;
    private ArrayList<Integer> H;
    private int N;
    private String path;
    private boolean isLoop = true;  //重复多次统计
    private boolean isFirst = true;
    private boolean isCompute = false;


    private LogWrite logWrite;
    private CpuCaculator cpuCaculator;


    public GatherInfo(PowerService powerService){
        this.activity = (ActivityManager) powerService.getSystemService(Context.ACTIVITY_SERVICE);
        this.pkArray = new SparseArray<>();
        this.rssArray = new SparseArray<>();
        path = "";  //目前为空path
        this.N = -1;
        this.H = new ArrayList<>();
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

        {

        }
        while(this.isLoop) {
            if(this.isFirst){
                if(!this.isCompute){
                    if (this.preTime < this.curTime) {
                        cpuCal(this.curTime);
                        tcpNetwork(this.curTime);
                        rssTotal(this.curTime);
                        calPower(this.preTime, this.curTime, false);
                    }
                }
            }
        }

    }

    private void calPower(long preTime, long curTime, boolean isUpdate) {
        ArrayList<Integer> copy = (ArrayList<Integer>) this.H.clone();
        if(isUpdate){

        }
        PowerCalculator.calPower(this.preTime, this.curTime, isUpdate, this.pkArray, copy, this.cpuCaculator, this.event, this.D);

    }

    private void rssTotal(long curTime) {
        ArrayList<Integer> pids = new ArrayList<>();
        ArrayList<Integer> pkIndex = new ArrayList<>();
        for(int i = 0; i < this.pkArray.size(); i++) {
            int index = this.pkArray.keyAt(i);
            ProcessKeeper pkTmp = this.pkArray.get(index);
            if (pkTmp != null) {
                Iterator<Integer> it = pkTmp.getPids().iterator();
                while (it.hasNext()) {
                    pids.add(it.next());
                    pkIndex.add(index);
                }
            }
        }
        if(!pids.isEmpty()){
            long[] tmp = RunningProcess.getRunningProcess(pids);
            if(tmp.length == pkIndex.size()){
                long[] obj = new long[2];
                obj[0] = curTime;
                int i = 0;
                while(i < pkIndex.size()){
                    obj[1] += tmp[i];
                    if(i == pkIndex.size() - 1 || !pkIndex.get(i).equals(pkIndex.get(i+1))){ //对应不同uid
                        int index = pkIndex.get(i);
                        ArrayList<long[]> rssTmp_t;
                        ArrayList<long[]> rssTmp = this.rssArray.get(index);
                        if(rssTmp == null){
                            rssTmp = new ArrayList<>();
                            rssArray.put(index, rssTmp);
                        }
                        rssTmp_t = rssTmp;
                        rssTmp_t.add(obj.clone());
                        obj[1] = 0;
                    }
                    i++;
                }
            }
        }

    }

    private void cpuCal(long t){
        cpuCaculator.cpuCal(t);
    }

    private int getImportance(int i){
        switch (i){
            case 100:
                return 1;
            /*case TransportMediator.KEYCODE_MEDIA_RECORD:
                return 2;
            */
            case 200:
                return 4;
            case 300:
                return 8;
            case 400:
                return 16;
            case 500:
                return 32;
            default:
                return 64;
        }
    }

    private ArrayList getUid(long t, boolean isUpdate){
        ArrayList<Integer>[] uidInfo = RunningProcess.getRunningProcess(this.activity);
        ArrayList<Integer> uid = uidInfo[0];
        ArrayList<Integer> pid = uidInfo[1];
        ArrayList<Integer> importance = uidInfo[2];
        ArrayList<Integer> prio = uidInfo[3];
        for(int i = 0; i < uid.size(); i++) {
            ProcessKeeper pk = this.pkArray.get(uid.get(i));//当前没有
            if (pk == null) {
                ProcessKeeper tmp = new ProcessKeeper(uid.get(i), getImportance(importance.get(i)), prio.get(i), this.path);
                tmp.addPid(pid.get(i));
                this.pkArray.put(uid.get(i), tmp);
                pk = tmp;
            } else if (!pk.getPids().contains(pid.get(i))) {    //不包含此pid
                if (pk.getCurTime() < t) {    //时间较早，说明信息已经过期。全部clear
                    pk.clearPids();
                    pk.resetImportance();
                    pk.clearPrio();
                }
                pk.addPid(pid.get(i));
                pk.updateImportance(getImportance(importance.get(i)));
                pk.addPrio(prio.get(i));

            }
            pk.updateTime(t);
        }
        if(isUpdate) {
            for (int size = this.pkArray.size() - 1; size >= 0; size--) {
                if (!this.pkArray.valueAt(size).isCurrentTime(t))
                    this.pkArray.removeAt(size);
            }
        }
        return uid;
    }

    private void tcpNetwork(long curTime) {
        ArrayList<Integer> uid = getUid(curTime, true);
        ArrayList<Integer> fileList = TcpNetwork.getFileList();
        int i = 0;
        while(i < uid.size()){
            ProcessKeeper pkTemp = this.pkArray.get(uid.get(i));
            if(pkTemp != null){
                pkTemp.getTotalTime(curTime);
                if(fileList.contains(uid.get(i))){
                    pkTemp.updateNetUsage(curTime);
                }
            }
            i++;
        }
    }
}
