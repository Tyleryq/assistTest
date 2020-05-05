package com.example.assisttest;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CountService extends IntentService {
    private NotificationManager mNManager;
    private WindowManager windowManager=null;
    private MyImageView[] imageView=new MyImageView[7];


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BackService","进入服务");

        //写文件
//        String logTxtNa = getExternalCacheDir().getAbsolutePath()+ File.separator+"log.txt";
//        FileOutputStream logTxt= null;
//        try {
//            logTxt = new FileOutputStream(logTxtNa,true);
//            OutputStreamWriter writer = new OutputStreamWriter(logTxt);
//            writer.append(MyValues.ft.format(new Date())+"\t暂停时:"+MyValues.totalPuaseMili/(60000));
//            writer.append("\r\n");
//            writer.close();
//            logTxt.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //5.0以后无用
//        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo t:runningAppProcessInfos) {
//            Log.d("运行的应用", t.processName);
//        }


        if (!MyValues.isPause) {
            //获取当天个应用使用情况
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());
            today.set(Calendar.HOUR_OF_DAY,0);
            today.set(Calendar.MINUTE,0);
            today.set(Calendar.SECOND,0);
            long zero=today.getTimeInMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, zero, System.currentTimeMillis());
            MyValues.selRunTime = 0;
            //如果被释放,读取文件缓存
            if (MyValues.selApps==null){
                FileInputStream in = null;
                try {
                    in = new FileInputStream(getExternalCacheDir().getAbsoluteFile()+File.separator+"data");
                    @SuppressWarnings("resource")
                    ObjectInputStream objectInputStream = new ObjectInputStream(in);
                    MyValues.selApps = (List<InsApp>) objectInputStream.readObject();
                    objectInputStream.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Log.d("所选应用个数",MyValues.selApps.size()+"");
            Log.d("获取使用情况应用个数",stats.size()+"");
            for (UsageStats t1 : stats) {
                long totT = t1.getTotalTimeInForeground() / (1000 * 60);
                Log.d("过去用", t1.getPackageName() + "前台时长" + totT + "分");
                for (InsApp sel : MyValues.selApps) {
                    if (sel.getPkgName().equals(t1.getPackageName())) {
                        int newTime = (int) t1.getTotalTimeInForeground();
                        MyValues.selRunTime += newTime;
                        //totaltime.setText("改变,z"+MyValues.selRunTime);
                        sel.setRunTime(newTime);
                    }
                }
            }

            Log.d("总运行时",MyValues.selRunTime/(60000)+"");
            Log.d("暂停时间",MyValues.totalPuaseMili/(60000)+"");

            MyValues.selRunTime-=MyValues.totalPuaseMili;

            Log.d("运行时",MyValues.selRunTime/(60000)+"");

            if(MyValues.selRunTime>=MyValues.totalMili){
                NotificationChannel channel = new NotificationChannel("1","tz",NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{200,200});
                mNManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNManager.createNotificationChannel(channel);
                Notification.Builder mBuilder = new Notification.Builder(this,"1");
                mBuilder.setContentTitle("Time Over");
                mBuilder.setContentText("Time Over");
                mBuilder.setWhen(System.currentTimeMillis());
                mBuilder.setSmallIcon(R.drawable.warning);
                mNManager.notify(1,mBuilder.build());

                if (!MyValues.removeflag) {
                    //悬浮窗
                    showBlockScreen(-200, -250, 0);
                    showBlockScreen(-200, -700, 1);
                    showBlockScreen(-200, 200, 2);
                    showBlockScreen(-200, 700, 3);
                    showBlockScreen(270, 200, 4);
                    showBlockScreen(270, -700, 5);
                    showBlockScreen(270, 700, 6);
                }

                //序列化保存ImageView
                FileOutputStream out;
                try {
                    out = new FileOutputStream(getExternalCacheDir().getAbsoluteFile()+File.separator+"imageview data");
                    ObjectOutputStream objectOutputStream = null;
                    objectOutputStream = new ObjectOutputStream(out);
                    objectOutputStream.writeObject(imageView);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            Intent intent1 = new Intent("updateviewdatereceiver");
            sendBroadcast(intent1);
        } else if (MyValues.removeflag){
            removeBlockScreen();
        }


        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        //这里是定时的,
        int anHour =  10*60*1000;//单位毫秒
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AlarmReceiver.class);
        i.setAction("myinterval");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public CountService() {
        super("CountService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private void showBlockScreen(int x,int y,int count) {

        MyValues.removeflag=true;
        // 新建悬浮窗控件
        imageView[count] = new MyImageView(getApplicationContext());
        imageView[count].setImageResource(R.drawable.warning);

        if (windowManager==null) {
            // 获取WindowManager服务
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        // 设置LayoutParam
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//注意该属性的设置很重要，FLAG_NOT_FOCUSABLE使浮动窗口不获取焦点,若不设置该属性，屏幕的其它位置点击无效，应为它们无法获取焦点
        layoutParams.width = 500;
        layoutParams.height = 500;
        layoutParams.x = x;
        layoutParams.y = y;

        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(imageView[count], layoutParams);

    }

    private void removeBlockScreen(){
        if (windowManager==null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            //Log.d("windowManager", "windowManager空");
        }
        if (imageView[0]==null){
            FileInputStream in = null;
            try {
                in = new FileInputStream(getExternalCacheDir().getAbsoluteFile()+File.separator+"imageview data");
                @SuppressWarnings("resource")
                ObjectInputStream objectInputStream = new ObjectInputStream(in);
                imageView = (MyImageView[]) objectInputStream.readObject();
                objectInputStream.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (ImageView anImageView : imageView)
            windowManager.removeViewImmediate(anImageView);
        MyValues.removeflag = false;
    }

}
