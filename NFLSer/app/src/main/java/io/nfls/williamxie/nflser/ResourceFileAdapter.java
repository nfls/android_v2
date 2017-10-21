package io.nfls.williamxie.nflser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.LinkedList;

public class ResourceFileAdapter extends BaseAdapter {

    private LinkedList<ResourceFile> mData;
    private Context mContext;

    public ResourceFileAdapter(LinkedList<ResourceFile> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public ResourceFile getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResourceFile file = mData.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_resource_file, parent, false);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.name = convertView.findViewById(R.id.name);
            holder.info = convertView.findViewById(R.id.info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (file.isFolder()) {
            holder.icon.setBackgroundResource(R.mipmap.folder_icon);
        } else {
            holder.icon.setBackgroundResource(R.mipmap.file_icon);
        }
        String name = file.getName();
        if (name.length() > 30) {
            name = name.substring(0, 31) + " ...";
        }
        holder.name.setText(name);
        String info = new Timestamp(file.getDate()).toString();
        info += " - ";
        info += file.getSizeWithUnit();
        if (file.isDownloaded()) {
            info += " - ";
            info += mContext.getString(R.string.cached);
        }
        holder.info.setText(info);
        return convertView;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView info;
    }

}
