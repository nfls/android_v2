package com.nflsic.williamxie.nflser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ResetPasswordActivity extends AppCompatActivity {

    private ProgressBar progressBar = null;
    private TextView loginButton = null;
    private Button resetPasswordButton = null;

    private String username = null;
    private String email = null;

    private Handler resetPasswordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            progressBar.setVisibility(View.INVISIBLE);
            resetPasswordButton.setEnabled(true);
            loginButton.setEnabled(true);
            if (jsonString.equals(REQUEST_FAILED)) {
                Toast.makeText(ResetPasswordActivity.this, R.string.request_failed, Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject json = new JSONObject(jsonString).getJSONObject("info");
                    if (json.getString("status").equals("success")) {
                        Toast.makeText(ResetPasswordActivity.this, R.string.send_email_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable resetPasswordTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", postResetPasswordRequest());
            msg.setData(data);
            resetPasswordHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        loginButton = (TextView) findViewById(R.id.login_button);
        resetPasswordButton = (Button) findViewById(R.id.reset_password_button);

        final EditText input_email = (EditText) findViewById(R.id.input_email);
        final TextView input_email_tip = (TextView) findViewById(R.id.input_email_tip);

        input_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
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
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
            }
        });

        resetPasswordButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = input_email.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                resetPasswordButton.setEnabled(false);
                loginButton.setEnabled(false);
                new Thread(resetPasswordTask).start();
            }
        });

        progressBar.setVisibility(View.INVISIBLE);

        if (!NFLSUtil.isNetworkAvailable(ResetPasswordActivity.this)) {
            Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private String postResetPasswordRequest() {
        String json = NFLSUtil.REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/center/recover?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");

            String data = "email=" + email + "&session=app";

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
