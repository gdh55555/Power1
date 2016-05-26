package com.gudh.power1.energy.Analysis;

/**
 * Created by Admin on 2016/5/23.
 */
public class PackageName {
    private static long duringTime = 3600000;
    private long time;
    private String name;    //packageManager name
    private String label;   //ApplicationLabel
    private int versionCode;
    private String versionName;
    private int permission;

    public PackageName(){
        this.time = -1;
        this.name = null;
    }

    public void canUpdate(){
        if(this.time != -1 && this.time + duringTime < System.currentTimeMillis()){
            this.time = -1;
            this.name = null;
            this.versionCode = -1;
            this.versionName = null;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        if(this.time == -1)
            this.time = System.currentTimeMillis();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setversionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setPermission(int permission) {
        this.permission |= permission;
    }

    public void clear() {
        this.time = -1;
        this.name = null;
    }

    public String[] getInfo() {
        if(this.name == null)
            return null;
        return new String[]{this.name, this.label, String.valueOf(this.versionCode), this.versionName, String.valueOf(this.permission)};
    }
}
