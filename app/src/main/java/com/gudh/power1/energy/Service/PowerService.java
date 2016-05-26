package com.gudh.power1.energy.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.gudh.power1.R;
import com.gudh.power1.energy.Analysis.GatherInfo;
import com.gudh.power1.energy.Analysis.PowermodelReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Admin on 2016/5/17.
 */
public class PowerService extends Service {
    private GatherInfo gatherInfo;
    private Thread thread;

    private Binder binder = new PowerBinder(this);
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void onCreate(){
        this.gatherInfo = new GatherInfo(this);
        String stringBuilder = String.valueOf(getFileStreamPath("").getAbsolutePath()) + "/" + "m.txt";
        InputStream openRawResource = getResources().openRawResource(R.raw.m);
        if (!new File(stringBuilder).exists()) {
            createSource(stringBuilder);
        }
        PowermodelReader.modelReader(stringBuilder, openRawResource);
        try {
            openRawResource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public int onStartCommand(Intent intent, int flag, int startId){
        super.onStartCommand(intent, flag, startId);
        if(this.thread == null){
            this.thread = new Thread(this.gatherInfo);
            this.thread.setPriority(10);
            this.thread.start();
        }
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        if(this.thread != null) {
            this.gatherInfo.isLoop = false;
            this.thread.interrupt();
            while(this.thread.isAlive()) {
                try {
                    this.thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void createSource(String str) {
        InputStream openRawResource = getResources().openRawResource(R.raw.m);
        try {
            new File(str).createNewFile();
            OutputStream fileOutputStream = new FileOutputStream(str);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read == -1) {
                    openRawResource.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    return;
                }
                fileOutputStream.write(bArr, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
