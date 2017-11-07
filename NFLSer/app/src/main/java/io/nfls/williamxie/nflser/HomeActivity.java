package io.nfls.williamxie.nflser;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import static io.nfls.williamxie.nflser.NFLSUtil.*;

public class HomeActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private Vector<FunctionBlock> mFunctionBlockData;
    private FunctionBlockAdapter mFunctionBlockAdapter;
    private ListView list_function_block;
    private Vector<News> mNewsData;
    private Vector<News> mNewsDataBuffer;
    private NewsAdapter mNewsAdapter;
    private ListView list_news;
    private SharedPreferences preferences;
    private boolean mIsExit;

    private Handler getNewsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String response = msg.getData().getString("response");
            if (response.equals(REQUEST_FAILED)) {
                Toast.makeText(HomeActivity.this, R.string.request_failed, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            } else {
                try {
                    mNewsData.clear();
                    JSONArray jsons = new JSONObject(response).getJSONArray("info");
                    for (int i = 0; i < jsons.length(); i ++) {
                        JSONObject json = jsons.getJSONObject(i);
                        String time = json.getString("time");
                        String title = json.getString("title");
                        String type = json.getString("type");
                        String detail = json.getString("detail");
                        String conf = json.getString("conf");
                        String imageUrl = json.getString("img");
                        News news = new News(time, title, type, detail, conf, imageUrl);
                        mNewsData.add(news);
                    }

                    mNewsAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    for (int i = 0; i < mNewsData.size(); i ++) {
                        new Thread(new getNewsImageTask(mNewsData.get(i).getImageUrl(), i)).start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler getNewsImageHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle data = msg.getData();
            News news = mNewsData.get(data.getInt("index"));
            Bitmap image = data.getParcelable("image");
            if (image == null) {
                image = BitmapFactory.decodeResource(getResources(), R.mipmap.help_icon);
            }
            news.setImage(image);
            /*
            for (News n : mNewsData) {
                if (n.getImage() == null) {
                    Log.d("Null", "Return");
                    return;
                }
            }
            */
            mNewsAdapter.notifyDataSetChanged();
        }
    };

    class getNewsImageTask implements Runnable {
        String imageUrl;
        int i;
        public getNewsImageTask(String imageUrl, int i) {
            this.imageUrl = imageUrl;
            this.i = i;
        }
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putParcelable("image", getNewsImageRequest(imageUrl));
            data.putInt("index", i);
            msg.setData(data);
            getNewsImageHandler.sendMessage(msg);
        }
    }

    private Runnable getNewsTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("response", getNewsRequest());
            msg.setData(data);
            getNewsHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        list_function_block = (ListView) findViewById(R.id.list_function_block);
        list_news = (ListView) findViewById(R.id.list_news);

        mFunctionBlockData = new Vector<FunctionBlock>();
        mFunctionBlockData.add(new FunctionBlock(R.string.resources, R.mipmap.resources_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.gc, R.mipmap.gc_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.forum, R.mipmap.forum_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.ic, R.mipmap.ic_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.alumni, R.mipmap.alumni_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.wiki, R.mipmap.wiki_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.weather, R.mipmap.weather_icon));
        mFunctionBlockData.add(new FunctionBlock(R.string.media, R.mipmap.media_icon));

        mFunctionBlockAdapter = new FunctionBlockAdapter(mFunctionBlockData, HomeActivity.this);

        View header = LayoutInflater.from(this).inflate(R.layout.header_list_function_block, null, false);
        View footer = LayoutInflater.from(this).inflate(R.layout.footer_list_function_block, null, false);
        list_function_block.addHeaderView(header, null, false);
        list_function_block.addFooterView(footer, null, true);
        list_function_block.setAdapter(mFunctionBlockAdapter);
        list_function_block.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == mFunctionBlockData.size() + 1) {
                    return;
                }
                FunctionBlock block = mFunctionBlockData.get(i - 1);
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

        mNewsData = new Vector<News>();
        mNewsDataBuffer = new Vector<News>();
        mNewsAdapter = new NewsAdapter(mNewsData, HomeActivity.this);
        list_news.setAdapter(mNewsAdapter);
        list_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NFLSUtil.isInternetRequestAvailable(HomeActivity.this)) {
                    refreshNews();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.GREEN);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setProgressViewOffset(true, 50, 250);

        if (NFLSUtil.isInternetRequestAvailable(HomeActivity.this)) {
            refreshNews();
        }

        final ImageView settings_icon = (ImageView) findViewById(R.id.settings_icon);
        settings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList options = new ArrayList<String>();
                options.add(getString(R.string.settings));
                options.add(getString(R.string.about));
                options.add(getString(R.string.log_out));
                options.add(getString(R.string.tickets));
                options.add(getString(R.string.vibrate));
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
                                NFLSUtil.isOfflineMode = false;
                                finish();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                break;
                            }
                            case 3: {
                                Intent intent = new Intent(HomeActivity.this, PassKitActivity.class);
                                intent.putExtra("url", "https://api.nfls.io/ic/ticket");
                                startActivity(intent);
                                break;
                            }
                            case 4: {
                                NFLSUtil.vibrate(HomeActivity.this);
                                break;
                            }
                            default: {
                                Toast.makeText(HomeActivity.this,"你tm点了什么弹出这个窗口", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });
                PopupWindow popupWindow = new PopupWindow(popupView, 200, options.size() * 110);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.background_list_settings_popup)));
                popupWindow.update();
                popupWindow.showAsDropDown(settings_icon, -40, 5);
            }
        });
        preferences = getSharedPreferences("user", MODE_APPEND);
        Toast.makeText(HomeActivity.this, getString(R.string.welcome) + " " + preferences.getString("username", "Unknown") + " !", Toast.LENGTH_SHORT).show();
    }

    private void refreshNews() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(getNewsTask).start();
    }

    public String getNewsRequest() {
        String response = NFLSUtil.REQUEST_FAILED;
        String token = getSharedPreferences("user", MODE_APPEND).getString("token", "No Token");
        try {
            URL url = new URL("https://api.nfls.io/center/news?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIME_OUT);
            connection.setReadTimeout(TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", " token=" + token);
            Log.d("Token", token);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                response = NFLSUtil.inputStreamToString(connection.getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public Bitmap getNewsImageRequest(String imageUrl) {
        Bitmap image = null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(TIME_OUT);
            connection.setReadTimeout(TIME_OUT);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                image = BitmapFactory.decodeStream(connection.getInputStream());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onBackPressed() {
        if (mIsExit) {
            finish();
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