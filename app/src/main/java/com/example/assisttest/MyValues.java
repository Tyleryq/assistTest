package com.example.assisttest;

import android.content.Context;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public class MyValues {
    public static long totalMili = 4*60*60*1000;    //总时间
    public static long selRunTime = 0;
    public static long puaseTime = 0;
    public static long totalPuaseMili = 0;
    public static boolean isPause=false;
    public static List<InsApp> selApps;
    public static Context context;
    public static SimpleDateFormat ft;
    public static boolean removeflag = false;//清屏标志
}
