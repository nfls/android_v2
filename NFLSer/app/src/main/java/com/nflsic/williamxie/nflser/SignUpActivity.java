package com.nflsic.williamxie.nflser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static com.nflsic.williamxie.nflser.NFLSUtil.REQUEST_FAILED;

public class SignUpActivity extends AppCompatActivity {

    private ProgressBar progressBar = null;
    private TextView loginButton = null;
    private Button signUpButton = null;

    private String username = null;
    private String password = null;
    private String email = null;

    private Handler signUpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            if (jsonString.equals(REQUEST_FAILED)) {
                Toast.makeText(SignUpActivity.this, R.string.request_failed, Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject json = new JSONObject(jsonString).getJSONObject("info");
                    if (json.getString("status").equals("success")) {
                        SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.commit();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUpActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            progressBar.setVisibility(View.INVISIBLE);
            signUpButton.setEnabled(true);
            loginButton.setEnabled(true);
        }
    };

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            progressBar.setVisibility(View.INVISIBLE);
            loginButton.setEnabled(true);
            signUpButton.setEnabled(true);
            Log.d("JsonString", jsonString);
            if (jsonString.equals(REQUEST_FAILED)) {
                Toast.makeText(SignUpActivity.this, R.string.request_failed, Toast.LENGTH_LONG).show();
            } else {
                JSONObject json = null;
                try {
                    json = new JSONObject(jsonString);
                    Log.d("JsonInfoSuccess", json.getJSONObject("info").getString("status").equals("success") + "");
                    if (json.getJSONObject("info").getString("status").equals("success")) {
                        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", json.getJSONObject("info").getString("token"));
                        editor.putString("username", username);
                        editor.commit();
                        Toast.makeText(SignUpActivity.this, SignUpActivity.this.getString(R.string.welcome) + " " + username + " !", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                    } else {
                        Toast.makeText(SignUpActivity.this, json.getJSONObject("info").getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        }
    };

    private Runnable signUpTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", postSignUpRequest());
            msg.setData(data);
            signUpHandler.sendMessage(msg);
            new Thread(loginTask).start();
        }
    };

    private Runnable loginTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", LoginActivity.postLoginRequest(username, password));
            msg.setData(data);
            loginHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        loginButton = (TextView) findViewById(R.id.login_button);

        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_password = (EditText) findViewById(R.id.input_password);
        final EditText input_email = (EditText) findViewById(R.id.input_email);
        final TextView input_email_tip = (TextView) findViewById(R.id.input_email_tip);

        input_email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String prefix = null;
                if (input_email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+") && s.length() > 0) {
                    prefix = getString(R.string.valid);
                }
                else {
                    prefix = getString(R.string.invalid);
                }
                input_email_tip.setText(prefix + " " + getString(R.string.email_address));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                signUpButton.setEnabled(false);
                loginButton.setEnabled(false);
                username = input_username.getText().toString();
                password = input_password.getText().toString();
                email = input_email.getText().toString();
                new Thread(signUpTask).start();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        progressBar.setVisibility(View.INVISIBLE);

        if (!NFLSUtil.isNetworkAvailable(SignUpActivity.this)) {
            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private String postSignUpRequest() {
        String json = NFLSUtil.REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/center/register?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");

            String data = "username=" + username + "&password=" + password + "&email=" + email + "&session=app";

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", data.length()+"");

            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                json = NFLSUtil.inputStreamToString(in);
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
}
