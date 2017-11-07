package io.nfls.williamxie.nflser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Vector;

public class NewsAdapter extends BaseAdapter {

    private Vector<News> mData;
    private Context mContext;

    public NewsAdapter(Vector<News> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public News getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        News news = mData.get(position);
        NewsAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new NewsAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_news, parent, false);
            holder.img = convertView.findViewById(R.id.img);
            holder.title = convertView.findViewById(R.id.title);
            holder.info = convertView.findViewById(R.id.info);
            holder.detail = convertView.findViewById(R.id.detail);
            convertView.setTag(holder);
        } else {
            holder = (NewsAdapter.ViewHolder) convertView.getTag();
        }
        holder.img.setImageBitmap(news.getImage());
        holder.title.setText(news.getTitle());
        String info = news.getType() + " " + news.getTime();
        holder.info.setText(info);
        holder.detail.setText(news.getDetail());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;
        public TextView detail;
    }

}
