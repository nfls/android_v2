package io.nfls.williamxie.nflser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class PhoneAuthActivity extends AppCompatActivity {

    private Button sendButton = null;
    private Button submitButton = null;
    private TextView backButton = null;
    private ProgressBar progressBar = null;

    private String phone_number = null;
    private String verification_code = null;

    private Handler sendForVerificationCodeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            sendButton.setEnabled(true);
            submitButton.setEnabled(true);
            backButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            boolean success = msg.getData().getBoolean("response");
            Log.d("success", success + "");
            if (success) {
                Toast.makeText(PhoneAuthActivity.this, getString(R.string.send_verification_code) + " " + getString(R.string.succeed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PhoneAuthActivity.this, getString(R.string.request_failed) + "\n" + getString(R.string.send_for_verification_code_failed_tip), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Handler sendVerificationCodeAuthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            sendButton.setEnabled(true);
            submitButton.setEnabled(true);
            backButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            boolean success = msg.getData().getBoolean("response");
            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_APPEND).edit();
            if (success) {
                editor.putBoolean("hasPhoneAuth", true);
                editor.commit();
                Toast.makeText(PhoneAuthActivity.this, getString(R.string.phone_auth_activity_title) + " " + getString(R.string.succeed), Toast.LENGTH_SHORT);
                finish();
                startActivity(new Intent(PhoneAuthActivity.this, RealNameAuthActivity.class));
            } else {
                editor.putBoolean("hasPhoneAuth", false);
                editor.commit();
                Toast.makeText(PhoneAuthActivity.this, getString(R.string.request_failed) + "\n" + getString(R.string.send_for_verification_code_failed_tip), Toast.LENGTH_SHORT);
            }
        }
    };

    private Runnable sendForVerificationCodeTask = new Runnable() {
        @Override
        public void run() {
            Bundle data = new Bundle();
            data.putBoolean("response", sendForVerificationCode());
            Message msg = new Message();
            msg.setData(data);
            sendForVerificationCodeHandler.sendMessage(msg);
        }
    };

    private Runnable sendVerificationCodeAuthTask = new Runnable() {
        @Override
        public void run() {
            Bundle data = new Bundle();
            data.putBoolean("response", sendVerificationCodeAuth());
            Message msg = new Message();
            msg.setData(data);
            sendVerificationCodeAuthHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        final EditText input_phone_number = (EditText) findViewById(R.id.input_phone_number);
        final EditText input_verification_code = (EditText) findViewById(R.id.input_verification_code);

        sendButton = (Button) findViewById(R.id.send_button);
        submitButton = (Button) findViewById(R.id.submit_button);
        backButton = (TextView) findViewById(R.id.back_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        sendButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendButton.setEnabled(false);
                submitButton.setEnabled(false);
                backButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                phone_number = input_phone_number.getText().toString();
                new Thread(sendForVerificationCodeTask).start();
            }
        });
        submitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendButton.setEnabled(false);
                submitButton.setEnabled(false);
                backButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                phone_number = input_phone_number.getText().toString();
                verification_code = input_verification_code.getText().toString();
                new Thread(sendVerificationCodeAuthTask).start();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(PhoneAuthActivity.this, HomeActivity.class));
            }
        });
        progressBar.setVisibility(View.INVISIBLE);
    }

    private boolean sendForVerificationCode() {
        boolean success = false;
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone_number);
            json.put("captcha", "app");
            String response = postPhoneRequest(json.toString());
            Log.d("Response", response);
            if (response != NFLSUtil.REQUEST_FAILED) {
                json = new JSONObject(response);
                if (json.getInt("code") == HttpsURLConnection.HTTP_OK) {
                    success = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    private boolean sendVerificationCodeAuth() {
        boolean success = false;
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone_number);
            json.put("code", Integer.valueOf(verification_code));
            json.put("captcha", "app");
            String response = postPhoneRequest(json.toString());
            if (response != NFLSUtil.REQUEST_FAILED) {
                json = new JSONObject(response);
                if (json.getInt("code") == HttpsURLConnection.HTTP_OK) {
                    success = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    private String postPhoneRequest(String jsonString) {
        Log.d("jsonString", jsonString);
        String response = NFLSUtil.REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/center/phone");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Cookie", "token=" + getSharedPreferences("user", MODE_APPEND).getString("token", "No Token"));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(jsonString);

            int responseCode = connection.getResponseCode();
            Log.d("PhoneAuthResponseCode", responseCode + "");
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = NFLSUtil.inputStreamToString(in);
                Log.d("Json", response);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
