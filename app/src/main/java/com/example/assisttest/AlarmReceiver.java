package com.example.assisttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context,Intent intent) {
        Log.d("reciever","进入接收器");
        Intent i = new Intent(context,CountService.class);
        context.startService(i);

    }
}
