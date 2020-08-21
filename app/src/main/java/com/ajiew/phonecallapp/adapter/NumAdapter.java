package com.ajiew.phonecallapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ajiew.phonecallapp.R;

import java.util.ArrayList;

public class NumAdapter extends BaseAdapter {

    private ArrayList arrayList;

    private Context context;


    public NumAdapter(ArrayList arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    // 当前适配器中加载数据的总条目
    @Override
    public int getCount() {
        return arrayList.size();
    }

    // 根据指定下标获取对应item 的view
    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    // 根据指定下标获取当前item的id
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder mHolder;
        // 表示第一次运行创建，否则复用view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.num_item, null);
            mHolder = new Holder();
            mHolder.tv = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        String num = arrayList.get(position).toString();
        mHolder.tv.setText(num);
        return convertView;
    }
}

