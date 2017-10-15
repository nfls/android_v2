package com.nflsic.williamxie.nflser;

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

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar = null;
    private String username = null;
    private String password = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String value = data.getString("value");
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, value, Toast.LENGTH_SHORT).show();
        }
    };

    Runnable loginTask = new Runnable() {

        @Override
        public void run() {

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", postLoginRequest());
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLoginState();
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

        TextView login = (TextView) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = input_username.getText().toString();
                password = input_password.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                new Thread(loginTask).start();
            }
        });

        TextView signUp = (TextView) findViewById(R.id.sign_up_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        TextView resetPassword = (TextView) findViewById(R.id.reset_password_button);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void checkLoginState() {

    }

    private String postLoginRequest() {
        String result = null;
        try {
            URL url = new URL("https://api.nfls.io/center/login?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");

            String data = "username=" + username + "&password=" + password + "&session=app";

            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", data.length()+"");

            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream in = connection.getInputStream();
                result = inputStreamToString(in);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String inputStreamToString(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = null;
        try {
            result = out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
