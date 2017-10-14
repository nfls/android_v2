package com.nflsic.williamxie.nflser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class FunctionBlockAdapter extends BaseAdapter {

    private LinkedList<FunctionBlock> mData;
    private Context mContext;

    public FunctionBlockAdapter(LinkedList<FunctionBlock> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public FunctionBlock getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_function_block, parent, false);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.name = convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.icon.setBackgroundResource(mData.get(position).getIcon());
        holder.name.setText(mData.get(position).getName());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView description;
    }

}
