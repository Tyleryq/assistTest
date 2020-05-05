package com.example.assisttest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity  implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private List<PackageInfo> packageInfos;
    private TextView mytxt;
    private TextView total;
    private List<InsApp> apps;
    AlarmReceiver alarmReceiver;
    UpdateViewDataReceiver updateViewDateReceiver;
    IntentFilter intentFilter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyValues.context=MainActivity.this;
        MyValues.ft = new SimpleDateFormat("yyyy MM dd HH:mm");
        MyValues.selApps= new ArrayList<>();

        List<String> appnames = new ArrayList<>();
        File apps_txt = new File(getExternalFilesDir(null).getAbsolutePath()+ File.separator+"apps.txt");
        Log.d("文件位置:",apps_txt.toString());
        try {
            if(!apps_txt.exists())  apps_txt.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(apps_txt));
            String name = br.readLine();
            while (name!=null){
                appnames.add(name);
                name = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        PackageManager packageManager = getPackageManager();
        packageInfos = packageManager.getInstalledPackages(0);
        mytxt = (TextView) findViewById(R.id.totaltime);
        mytxt.setText("0");
        total = findViewById(R.id.totoal);
        ToggleButton countbtn = findViewById(R.id.countbtn);
        countbtn.setChecked(true);
        countbtn.setOnCheckedChangeListener(this);

        Button help = findViewById(R.id.help);
        help.setOnClickListener(this);


        // 查询所有已经安装的应用程序
        apps = new ArrayList<>();
        for(PackageInfo packageInfo:packageInfos){
            InsApp tapp = new InsApp(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());//获取应用程序名
            tapp.setPkgName( packageInfo.packageName);
            apps.add(tapp);
        }

        for (InsApp app:apps){
            for (String appname:appnames){
                if (appname.equals(app.getName()))  MyValues.selApps.add(app);
            }
        }

        //序列化保存
        FileOutputStream out;
        try {
            out = new FileOutputStream(getExternalCacheDir().getAbsoluteFile()+File.separator+"data");
            ObjectOutputStream objectOutputStream = null;
            objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(MyValues.selApps);
            objectOutputStream.flush();
            objectOutputStream.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        MyAdapter myAdapter = new MyAdapter(apps,MainActivity.this);
        ListView mlist=findViewById(R.id.mlist);
        mlist.setAdapter(myAdapter);

        final EditText inTRT = findViewById(R.id.inTRT);
        inTRT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //Toast.makeText(MainActivity.this,inTRT.getText(),Toast.LENGTH_SHORT).show();
                    String ti = inTRT.getText().toString();
                    Toast.makeText(MainActivity.this,ti,Toast.LENGTH_SHORT).show();
                    float it = Float.parseFloat(ti);
                    if(it <= 4.0){
                        MyValues.totalMili = (long)(it*60*60*1000);
                    }
                }
            }
        });

        //打开获取访问情况权限设置界面
        //startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        //打开悬浮窗权限设置界面
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }


        //动态注册无用
//        alarmReceiver=new AlarmReceiver();
//        IntentFilter itf = new IntentFilter();
//        itf.addAction("myinterval");
//        registerReceiver(alarmReceiver,itf);

        updateViewDateReceiver = new UpdateViewDataReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("updateviewdatereceiver");
        registerReceiver(updateViewDateReceiver,intentFilter);

        //启动服务
        Intent intent = new Intent(MainActivity.this,CountService.class);
        startService(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.countbtn:
                if (buttonView.isChecked()){
                    MyValues.totalPuaseMili+=(System.currentTimeMillis()-MyValues.puaseTime);
                    MyValues.isPause=false;
                    //registerReceiver(updateViewDateReceiver,intentFilter);
                    Toast.makeText(this,"统计",Toast.LENGTH_SHORT).show();
                    //Log.d("Main","打开开关");

                } else {
                    MyValues.puaseTime=System.currentTimeMillis();
                    MyValues.isPause=true;
                    //unregisterReceiver(updateViewDateReceiver);
                    //Toast.makeText(this,"暂停",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.help:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog alertDialog = builder.setTitle(R.string.help)
                        .setMessage(R.string.tip)
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                alertDialog.show();
                break;
        }
    }

    public class UpdateViewDataReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            mytxt.setText(MyValues.selRunTime/(60*1000)+"分("+String.format("%.2f",MyValues.selRunTime/(60*1000*60.0))+"小时)");
            total.setText(String.format("%.2f",MyValues.totalMili/(60*1000*60.0))+"小时");
//            Toast.makeText(MainActivity.this,"进入接收器",Toast.LENGTH_LONG).show();
        }
    }

}
