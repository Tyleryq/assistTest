package com.example.assisttest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {
    private List<InsApp> apps;
    private Context mcontext;

    MyAdapter(List<InsApp> apps,Context context){
        this.apps=apps;
        this.mcontext=context;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mcontext).inflate(R.layout.item_list,parent,false);
        TextView appname = convertView.findViewById(R.id.appname);
        Switch appswitch = convertView.findViewById(R.id.switch1);
        appswitch.setOnCheckedChangeListener(apps.get(position));
        appname.setText(apps.get(position).getName());//+"  "+apps.get(position).getPkgName()
//        TextView runTime = convertView.findViewById(R.id.runtime);
//        runTime.setText(apps.get(position).getRunTime());
        return convertView;
    }

}
