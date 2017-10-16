package com.nflsic.williamxie.nflser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.nflsic.williamxie.nflser.RuntimeInfo.isOnline;

public class LoginActivity extends AppCompatActivity {

    private static final String TOKEN_CORRECT = "token_correct";
    private static final String TOKEN_WRONG = "token_wrong";
    private static final String REQUEST_FAILED = "request_failed";

    private ProgressBar progressBar = null;
    private TextView loginButton = null;
    private TextView signUpButton = null;
    private TextView resetPasswordButton = null;

    private String username = null;
    private String password = null;

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            progressBar.setVisibility(View.INVISIBLE);
            if (jsonString.equals(REQUEST_FAILED)) {
                Toast.makeText(LoginActivity.this, R.string.request_time_out, Toast.LENGTH_LONG).show();
            } else {
                JSONObject json = null;
                try {
                    json = new JSONObject(jsonString);
                    if (json.getJSONObject("info").getString("status").equals("success")) {
                        Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_LONG).show();
                        storeCookies(json.getJSONObject("info").getString("token"));
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, json.getJSONObject("info").getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler autoLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");
            if (result.equals(TOKEN_CORRECT)) {
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_LONG).show();
                Intent intent =new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            loginButton.setEnabled(true);
            signUpButton.setEnabled(true);
            resetPasswordButton.setEnabled(true);
        }
    };

    private Runnable loginTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", postLoginRequest());
            msg.setData(data);
            loginHandler.sendMessage(msg);
        }
    };

    private Runnable autoLoginTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("result", autoLogin());
            msg.setData(data);
            autoLoginHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        */

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_password = (EditText) findViewById(R.id.input_password);

        loginButton = (TextView) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = input_username.getText().toString();
                password = input_password.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                new Thread(loginTask).start();
            }
        });

        signUpButton = (TextView) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        resetPasswordButton = (TextView) findViewById(R.id.reset_password_button);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        loginButton.setEnabled(false);
        signUpButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);

        isOnline = NFLSUtil.isNetworkAvailable(LoginActivity.this);

        if (!isOnline) {
            Intent intent =new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            new Thread(autoLoginTask).start();
        }
    }

    private String postLoginRequest() {
        String json = null;
        try {
            URL url = new URL("https://api.nfls.io/center/login?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");

            String data = "username=" + username + "&password=" + password + "&session=app";

            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", data.length()+"");

            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                json = NFLSUtil.inputStreamToString(in);
            } else {
                json = REQUEST_FAILED;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private String autoLogin() {
        String result = null;
        SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
        String token = preferences.getString("token", "No Token");
        URL url;
        try {
            url = new URL("https://api.nfls.io/device/status?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", " token=" + token);

            Log.d("Token", token);
            Log.d("Header", getReqeustHeader(connection));

            int responseCode = connection.getResponseCode();

            Log.d("ResponseCode", responseCode + "");

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                JSONObject json = new JSONObject(NFLSUtil.inputStreamToString(connection.getInputStream()));
                result = TOKEN_CORRECT;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("id", json.getInt("id"));
                editor.commit();
            } else {
                result = TOKEN_WRONG;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void storeCookies(String token) {
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.commit();
    }

    private void clearPreferences() {
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    private String getReqeustHeader(HttpsURLConnection conn) {
        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
            sbRequestHeader.append(requestHeaderKey);
            sbRequestHeader.append(":");
            sbRequestHeader.append(requestHeaderValue);
            sbRequestHeader.append("\n");
        }
        return sbRequestHeader.toString();
    }
}
