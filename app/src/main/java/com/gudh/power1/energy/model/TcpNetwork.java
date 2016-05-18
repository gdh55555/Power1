package com.gudh.power1.energy.model;

import android.net.TrafficStats;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.gudh.power1.energy.Analysis.SystemProcess;

/**
 * Created by Admin on 2016/5/11.
 */

/**
 * read /proc/[uid]/tcp_rcv
 * read /proc/[uid]/tcp_snd
 */
public class TcpNetwork implements Parcelable{

    public static int uid;
    public static long tcp_rcv;
    public static long tcp_snd;

    /**
     * 判断/proc/uid_stat是否存在
     */
    public static void  FileIsExist(){
        SystemProcess.uid_stat_isExist =  new File("/proc/uid_stat/").exists();
    }
    public static TcpNetwork get(int uid){
        try {
            return new TcpNetwork(String.format("/proc/uid_stat/%s", uid), uid);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public TcpNetwork(String path, int uid) throws IOException{
        this.uid = uid;
        if(SystemProcess.uid_stat_isExist) {
            this.tcp_rcv = Long.valueOf(ProcFile.readFile(String.format(path + "/%s", "tcp_rcv")));
            this.tcp_snd = Long.valueOf(ProcFile.readFile(String.format(path + "/%s", "tcp_snd")));
        }
        else{
            this.tcp_rcv = TrafficStats.getUidRxBytes(this.uid);
            this.tcp_snd = TrafficStats.getUidTxBytes(this.uid);

        }
    }

    public long getTcp_rcv() {

        if(SystemProcess.uid_stat_isExist)
            try {
                tcp_rcv = Long.valueOf(ProcFile.readFile(String.format("/proc/uid_stat/%s/tcp_rcv",uid)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            tcp_rcv = TrafficStats.getUidRxBytes(uid);
        return tcp_rcv;
    }

    public long getTcp_snd(){
        if(SystemProcess.uid_stat_isExist)
            try {
                tcp_rcv = Long.valueOf(ProcFile.readFile(String.format("/proc/uid_stat/%s/tcp_snd", uid)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            tcp_snd = TrafficStats.getUidTxBytes(uid);
        return tcp_snd;
    }

    public void update(){
        getTcp_rcv();
        getTcp_snd();
    }

    public static ArrayList<Integer> getFileList(){
        if (!new File("/proc/uid_stat/").exists()) {
            return new ArrayList<>();
        }
        String[] list = new File("/proc/uid_stat/").list();
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (list != null) {
            for (String parseInt : list) {
                arrayList.add(Integer.parseInt(parseInt));
            }
        }
        return arrayList;
    }

    public long getRcv() {
        return this.tcp_rcv;
    }

    public long getSnd() {
        return this.tcp_snd;
    }

    protected TcpNetwork(Parcel in) {
        this.tcp_rcv = in.readLong();
        this.tcp_snd = in.readLong();
    }

    public static final Creator<TcpNetwork> CREATOR = new Creator<TcpNetwork>() {
        @Override
        public TcpNetwork createFromParcel(Parcel in) {
            return new TcpNetwork(in);
        }

        @Override
        public TcpNetwork[] newArray(int size) {
            return new TcpNetwork[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.tcp_rcv);
        parcel.writeLong(this.tcp_snd);
    }
}
