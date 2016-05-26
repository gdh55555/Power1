package com.gudh.power1.energy.Analysis;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
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
    private long networkTime = -1;
    private long networkTimeScreenOff = -1;
    private long screenOffTime = 5000;
    private String timeFormat;      //时间格式
    private ActivityManager activity;
    private SparseArray<ProcessKeeper> pkArray; //对应uid 和 pk 变量
    private SparseArray<ArrayList<long[]>> rssArray; //貌似是rss
    private HashMap<String, ArrayList> event = new HashMap<>();
    private ArrayList<long[]> D;
    private ArrayList<Integer> H;
    private int N;
    private String path;
    public boolean isLoop = true;  //重复多次统计
    public boolean hasCompute = true;
    public boolean isFirst = true;
    private PackageManager packageManager;


    private LogWrite logWrite;
    private CpuCaculator cpuCaculator;


    public GatherInfo(PowerService powerService){
        this.activity = (ActivityManager) powerService.getSystemService(Context.ACTIVITY_SERVICE);
        this.packageManager = powerService.getPackageManager();
        this.pkArray = new SparseArray<>();
        this.rssArray = new SparseArray<>();
        path = "";  //目前为空path
        this.N = -1;
        this.H = new ArrayList<>();
        this.cpuCaculator = new CpuCaculator();
    }
    @Override
    public void run() {
        Process.setThreadPriority(-2);
        RunningProcess.getSystemProcess(); //获得系统进程
        TcpNetwork.FileIsExist();          //判断/proc/uid_stat是否存在
        this.curTime = System.currentTimeMillis();  //当前时间
        this.preTime = this.curTime - 1000;         //一秒前的时间
        this.networkTime = this.curTime - 1000;
        this.networkTimeScreenOff = this.curTime - 1000;
        //未实现  等待实现
        Date date = new java.sql.Date(this.curTime);  //mysql的data
        this.logWrite.init(this.timeFormat, this.curTime, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date)); //时间

        while(this.isLoop) {
            if(this.hasCompute){       //暂定名字

                if(this.isFirst) {
//                    if (this.preTime < this.curTime) {
                        cpuCal(this.curTime);       //统计cpu以及各个时间频率
//                        tcpNetwork(this.curTime);   //pid时间以及网络情况
//                        this.networkTime = this.curTime; //网络时间变动
                        rssTotal(this.curTime);         //每个uid对应的rss使用情况
//                        calPower(this.preTime, this.curTime, false);    //统计电量
                        this.preTime = this.curTime;
                        this.isFirst = false;
//                    }
                }
                Thread.currentThread();
                try {
                    Thread.sleep(Math.max(0, 1000 - (System.currentTimeMillis() - this.curTime)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.curTime = System.currentTimeMillis();
                if(this.hasCompute){
                    // get running process
                }
//                if(this.curTime - this.networkTime >= 1000){
//                    tcpNetwork(this.curTime, true);
//                    this.networkTime = this.curTime;
//                }
//                if(this.curTime - this.networkTimeScreenOff >= this.screenOffTime){
//                    tcpNetwork(this.curTime, true);
//                    this.networkTime = this.curTime;
//                    this.networkTimeScreenOff = this.curTime;
//                }
                if(this.curTime - this.preTime > 30000){
                    cpuCal(this.curTime);
//                    tcpNetwork(this.curTime);
//                    this.networkTime = this.curTime;
//                    this.networkTimeScreenOff = this.curTime;
                    rssTotal(this.curTime);
                    calPower(this.preTime, this.curTime, true);
                    this.preTime = this.curTime;
                }
            }
        }

    }

    private void tcpNetwork(long curTime, boolean isUpdate) {
        ProcessKeeper pk;
        if(isUpdate){
            Iterator<Integer> it = this.H.iterator();
            while(it.hasNext()){
                pk = this.pkArray.get(it.next().intValue());
                if(pk != null){
                    pk.updateNetUsage(this.curTime);
                }
            }
            return;
        }
        ArrayList<Integer> fileList = TcpNetwork.getFileList();
        int i = 0;
        while(i < fileList.size()){
            ProcessKeeper pkTemp = this.pkArray.get(fileList.get(i));
            if(pkTemp != null){
                pkTemp.updateNetUsage(curTime);
            }
            i++;
        }
    }

    private void calPower(long preTime, long curTime, boolean isUpdate) {
        ArrayList<Integer> copy = (ArrayList<Integer>) this.H.clone();
        if(isUpdate){

        }
        PowerCalculator.calPower(this.preTime, this.curTime, isUpdate, this.pkArray, copy, this.cpuCaculator, this.event, this.D);
        //写文件
        writeSQL(preTime, curTime, this.pkArray);
        int i = 0;
        while(i < this.pkArray.size()){
            ProcessKeeper pk = this.pkArray.valueAt(i);
            if(pk == null){
                i++;
            }
            else{
                pk.clearNetworks();
                //gpu
                //pk.clearGpu();
                pk.clearPower();
                i++;
            }
        }
    }

    private void writeSQL(long preTime, long curTime, SparseArray<ProcessKeeper> pkArray) {
        ArrayList<ContentValues> a = new ArrayList<>();
        ArrayList<ContentValues> a1 = new ArrayList<>();
        for(int i= 0; i < pkArray.size(); i++){
            ProcessKeeper pkTmp = pkArray.valueAt(i);
            String[] s = RunningProcess.getPackage(pkArray.keyAt(i), this.packageManager);
            if(s != null && s.length >= 5){
                String pkg_name = s[0];
                String app_name = s[1];
                if(!pkg_name.contains(":")){
                    int fgTime = (int)Math.round(pkTmp.getFgTime() / 1000.0d);
                    int bgTime = (int)Math.round(pkTmp.getBgTime() / 1000.0d);
                    int fgEnergy = (int)Math.round(pkTmp.getFgEnergy() / 10.0d);
                    int bgEnergy = (int)Math.round(pkTmp.getBgEnergy() / 10.0d);
                    if(fgEnergy > 0 || bgEnergy > 0){
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("pkg_name", pkg_name);
                        contentValues.put("app_name", app_name);
                        contentValues.put("fg_time", fgTime);
                        contentValues.put("fg_energy", fgEnergy);
                        contentValues.put("bg_time", bgTime);
                        contentValues.put("bg_energy", bgEnergy);
                        contentValues.put("start_time", preTime);
                        contentValues.put("end_time", curTime);
                        a.add(contentValues);
                        ContentValues contentValues1 = new ContentValues();
                        contentValues1.put("pkg_name", pkg_name);
                        contentValues1.put("fg", pkTmp.getFg());
                        contentValues1.put("cpu", pkTmp.getCpuPower());
                        contentValues1.put("network", pkTmp.getNetWorkPower());
                        contentValues1.put("screen", pkTmp.getScreenPower());
                        contentValues1.put("gps", 0);
                        contentValues1.put("gpu", pkTmp.getGpuPowr());
                        contentValues1.put("start_time", preTime);
                        contentValues1.put("end_time", curTime);
                        a1.add(contentValues1);
                    }
                }
            }
        }
        //写入SQL
    }

    /**
     * 统计uid 对应的所有 RSS
     * @param curTime 当前时间
     */
    private void rssTotal(long curTime) {
        ArrayList<Integer> pids = new ArrayList<>();
        ArrayList<Integer> pkIndex = new ArrayList<>();
        for(int i = 0; i < this.pkArray.size(); i++) {
            int index = this.pkArray.keyAt(i);
            ProcessKeeper pkTmp = this.pkArray.get(index);
            if (pkTmp != null) {
                for (Integer integer : pkTmp.getPids()) {
                    pids.add(integer);
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

    /**
     * 统计cpu 使用时间以及频率状况
     * @param t 当前时间
     */
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

    /**
     * 获得uid信息
     * 将网络情况保存在pk中
     * 并统计每个pid 以及对于tid的时间
     * @param curTime
     */
    private void tcpNetwork(long curTime) {
        ArrayList<Integer> uid = getUid(curTime, true);
        ArrayList<Integer> fileList = TcpNetwork.getFileList(); //uid_stat下面的文件list
        int i = 0;
        while(i < uid.size()){
            ProcessKeeper pkTemp = this.pkArray.get(uid.get(i));
            if(pkTemp != null){
                pkTemp.getTotalTime(curTime);   //uid tid time
                if(fileList.contains(uid.get(i))){
                    pkTemp.updateNetUsage(curTime); //获取网络情况
                }
            }
            i++;
        }
    }
}
