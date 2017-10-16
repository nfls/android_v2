package com.nflsic.williamxie.nflser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class HomeActivity extends AppCompatActivity {

    private LinkedList<FunctionBlock> mData;
    private Context mContext;
    private FunctionBlockAdapter mAdapter;
    private ListView list_function_block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = HomeActivity.this;
        list_function_block = (ListView) findViewById(R.id.list_function_block);

        mData = new LinkedList<FunctionBlock>();
        mData.add(new FunctionBlock(R.string.resources, R.mipmap.resources_icon));
        mData.add(new FunctionBlock(R.string.gc, R.mipmap.gc_icon));
        mData.add(new FunctionBlock(R.string.forum, R.mipmap.forum_icon));
        mData.add(new FunctionBlock(R.string.ic, R.mipmap.ic_icon));
        mData.add(new FunctionBlock(R.string.alumni, R.mipmap.alumni_icon));
        mData.add(new FunctionBlock(R.string.wiki, R.mipmap.wiki_icon));
        mData.add(new FunctionBlock(R.string.weather, R.mipmap.weather_icon));
        mData.add(new FunctionBlock(R.string.media, R.mipmap.media_icon));

        mAdapter = new FunctionBlockAdapter(mData, mContext);

        View header = LayoutInflater.from(this).inflate(R.layout.header_list_function_block, null, false);
        View footer = LayoutInflater.from(this).inflate(R.layout.footer_list_function_block, null, false);
        list_function_block.addHeaderView(header, null, false);
        list_function_block.addFooterView(footer, null, true);
        list_function_block.setAdapter(mAdapter);
        list_function_block.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == mData.size() + 1) {
                    return;
                }
                Toast.makeText(HomeActivity.this, mData.get(i - 1).getIcon(), Toast.LENGTH_SHORT).show();
            }
        });

        ImageView list_icon = (ImageView) findViewById(R.id.list_icon);
        list_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) findViewById(R.id.layout_home)).openDrawer(Gravity.START);
            }
        });

        ImageView settings_icon = (ImageView) findViewById(R.id.settings_icon);
        settings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}