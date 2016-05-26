package com.gudh.power1.energy.Service;

import android.os.Binder;

/**
 * Created by Admin on 2016/5/25.
 */
public class PowerBinder extends Binder {
    public PowerService powerService;
    public PowerBinder(PowerService p){
        this.powerService = p;
    }
}
