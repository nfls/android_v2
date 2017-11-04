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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private LinkedList<FunctionBlock> mData;
    private FunctionBlockAdapter mAdapter;
    private ListView list_function_block;
    private SharedPreferences preferences;
    private boolean mIsExit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
                            new AlertDialog.Builder(HomeActivity.this)
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
                        } else {
                            //Toast.makeText(HomeActivity.this, block.getName(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(HomeActivity.this, ResourcesActivity.class));
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, block.getName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HomeActivity.this, ResourcesActivity.class));
                    }
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

        final ImageView settings_icon = (ImageView) findViewById(R.id.settings_icon);
        settings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList options = new ArrayList<String>();
                options.add(getString(R.string.settings));
                options.add(getString(R.string.about));
                options.add(getString(R.string.log_out));
                options.add(getString(R.string.tickets));
                View popupView = HomeActivity.this.getLayoutInflater().inflate(R.layout.window_popup_settings, null);
                ListView listView = (ListView) popupView.findViewById(R.id.list_view_popup);
                listView.setAdapter(new ArrayAdapter<String>(HomeActivity.this, R.layout.item_list_settings, options));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0: {
                                Toast.makeText(HomeActivity.this,R.string.close_tip, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            case 1: {
                                new AlertDialog.Builder(HomeActivity.this)
                                        .setIcon(R.mipmap.nflsio)
                                        .setTitle(R.string.about)
                                        .setMessage(getString(R.string.developer) + ": " + getString(R.string.developer_name) + "\n" + getString(R.string.version) + ": " + getString(R.string.version_no))
                                        .setPositiveButton(getString(R.string.say_thanks_to) + " " + getString(R.string.developer_name), null)
                                        .setCancelable(true)
                                        .show();
                                break;
                            }
                            case 2: {
                                Toast.makeText(HomeActivity.this, R.string.operation_succeed, Toast.LENGTH_SHORT).show();
                                LoginActivity.clearPreferences(HomeActivity.this);
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                break;
                            }
                            case 3: {
                                Intent intent = new Intent(HomeActivity.this, PassKitActivity.class);
                                intent.putExtra("url", "https://api.nfls.io/ic/ticket");
                                startActivity(intent);
                                break;
                            }
                            default: {
                                Toast.makeText(HomeActivity.this,"你tm点了什么弹出这个窗口", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });
                PopupWindow popupWindow = new PopupWindow(popupView, 200, 450);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.update();
                popupWindow.showAsDropDown(settings_icon, -40, 5);
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

    @Override
    public void onResume() {
        super.onResume();
        NFLSUtil.isOnline = NFLSUtil.isNetworkAvailable(HomeActivity.this);
    }
}