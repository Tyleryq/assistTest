package com.example.assisttest;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.app.usage.UsageStats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;

public class InsApp implements CompoundButton.OnCheckedChangeListener,Serializable {

    private String name;
    private String pkgName;
    private int aSwitch;
    private int runTime;

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }



    public  InsApp(){
        this.runTime=0;
    }
    public InsApp(String name) {
        this.name = name;
        this.runTime=0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getaSwitch() {
        return aSwitch;
    }

    public void setaSwitch(int aSwitch) {
        this.aSwitch = aSwitch;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(MyValues.context, name, Toast.LENGTH_LONG).show();
            MyValues.selApps.add(this);
            String sapps_txt = MyValues.context.getExternalFilesDir(null).getAbsolutePath()+ File.separator+"apps.txt";
            FileOutputStream apps_txt= null;
            try {
                apps_txt = new FileOutputStream(sapps_txt,true);
                OutputStreamWriter writer = new OutputStreamWriter(apps_txt);
                writer.append("\r\n");
                writer.append(name);
                writer.close();
                apps_txt.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
