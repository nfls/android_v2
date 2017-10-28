package io.nfls.williamxie.nflser;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.util.LinkedList;

public class HomeActivity extends AppCompatActivity {

    private LinkedList<FunctionBlock> mData;
    private FunctionBlockAdapter mAdapter;
    private ListView list_function_block;
    private SharedPreferences preferences;
    private boolean mIsExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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

        mAdapter = new FunctionBlockAdapter(mData, HomeActivity.this);

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
                FunctionBlock block = mData.get(i - 1);
                if (i == 1) {
                    Log.d("Choice", preferences.getBoolean("hasRealNameAuth", false) + "");
                    if (NFLSUtil.isOnline) {
                        if (!preferences.getBoolean("hasRealNameAuth", false)) {
                            Toast.makeText(HomeActivity.this, R.string.resources_closed_tip, Toast.LENGTH_SHORT).show();
                            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle(R.string.warning)
                                    .setIcon(R.mipmap.nflsio)
                                    .setMessage(R.string.real_name_auth_tip)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (preferences.getBoolean("hasPhoneAuth", false)) {
                                                startActivity(new Intent(HomeActivity.this, RealNameAuthActivity.class));
                                            } else {
                                                startActivity(new Intent(HomeActivity.this, PhoneAuthActivity.class));
                                            }
                                        }
                                    })
                                    .show();
                        }
                    }
                    Toast.makeText(HomeActivity.this, block.getName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, ResourcesActivity.class));
                } else {
                    Toast.makeText(HomeActivity.this, R.string.close_tip, Toast.LENGTH_SHORT).show();
                }
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
        preferences = getSharedPreferences("user", MODE_APPEND);
        Toast.makeText(HomeActivity.this, getString(R.string.welcome) + " " + preferences.getString("username", "Unknown") + " !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mIsExit) {
            this.finish();
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
        } else {
            Toast.makeText(HomeActivity.this, getString(R.string.exit_tip), Toast.LENGTH_SHORT).show();
            mIsExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsExit = false;
                }
            }, 2000);
        }
    }
}