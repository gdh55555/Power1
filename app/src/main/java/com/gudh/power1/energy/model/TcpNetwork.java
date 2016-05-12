package com.gudh.power1.energy.model;

import android.net.TrafficStats;
import android.os.Parcel;
import android.os.Parcelable;

import com.gudh.power1.energy.Status.FileStatus;

import java.io.File;
import java.io.IOException;

import com.gudh.power1.energy.Status.FileStatus;

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

    public void FileIsExist(){
        FileStatus.uid_stat_isExist =  new File("/proc/uid_stat/").exists();
    }
    public static TcpNetwork get(int uid) throws IOException{
        return new TcpNetwork(String.format("/proc/uid_stat/%s", uid), uid);
    }

    public TcpNetwork(String path, int uid) throws IOException{
        this.uid = uid;
        if(FileStatus.uid_stat_isExist) {
            this.tcp_rcv = Long.valueOf(ProcFile.readFile(String.format(path + "/%s", "tcp_rcv")));
            this.tcp_snd = Long.valueOf(ProcFile.readFile(String.format(path + "/%s", "tcp_snd")));
        }
        else{
            this.tcp_rcv = TrafficStats.getUidRxBytes(this.uid);
            this.tcp_snd = TrafficStats.getUidTxBytes(this.uid);

        }
    }

    public long getRcv() throws IOException{
        if(FileStatus.uid_stat_isExist)
            tcp_rcv = Long.valueOf(ProcFile.readFile(String.format("/proc/uid_stat/%s/tcp_rcv",uid)));
        else
            tcp_rcv = TrafficStats.getUidRxBytes(uid);
        return tcp_rcv;
    }

    public long getSnd() throws IOException{
        if(FileStatus.uid_stat_isExist)
            tcp_rcv = Long.valueOf(ProcFile.readFile(String.format("/proc/uid_stat/%s/tcp_snd", uid)));
        else
            tcp_snd = TrafficStats.getUidTxBytes(uid);
        return tcp_snd;
    }

    public void update() throws IOException{
        getRcv();
        getSnd();
    }
    public long getTcp_rcv() {
        return tcp_rcv;
    }

    public long getTcp_snd() {
        return tcp_snd;
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
