package com.gudh.power1.energy.Analysis;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 2016/5/11.
 */
public class ProcessKeeper {
    private static String processKeeper = "ProcessKeeper";
    private int tidnum; //tid的数量
    private ArrayList<Integer> pids; //一个uid对应多个pid
    private SparseArray<Integer> sysTimeSpar = new SparseArray<>();
    private SparseArray<Integer> userTimeSpar = new SparseArray<>();
    private ArrayList<Long> newWork = new ArrayList<>();
    private HashMap tids = new HashMap(); // 0 0 -1 -1 -1线程的一些统计
    private long systemTime = 0;
    private long userTime = 0;
    private long curTime;



}
