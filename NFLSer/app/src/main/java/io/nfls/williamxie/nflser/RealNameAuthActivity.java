package io.nfls.williamxie.nflser;

import android.app.Activity;
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
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class RealNameAuthActivity extends AppCompatActivity {

    private static String[] classIDs = {"PRE IB 1", "PRE IB 2", "PAL 1", "PAL 2", "DP 1-1", "DP 1-2", "AS 1-1", "AS 1-2", "DP 2-1", "DP 2-2", "A2 2-1", "A2 2-2", "Teacher", "Main Campus"};
    private String chinese_name = null;
    private String english_name = null;
    private String classID = classIDs[0];

    private EditText input_name_chinese = null;
    private EditText input_name_english = null;
    private Button submitButton = null;
    private TextView backButton = null;
    private ProgressBar progressBar = null;

    private Handler submitRealNameAuthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            submitButton.setEnabled(true);
            backButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            Bundle data = msg.getData();
            int response = data.getInt("response");
            if (response != HttpsURLConnection.HTTP_OK) {
                Toast.makeText(RealNameAuthActivity.this, R.string.request_failed, Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("hasRealNameAuth", false);
                editor.commit();
                finish();
                startActivity(new Intent(RealNameAuthActivity.this, HomeActivity.class));
            } else {
                Toast.makeText(RealNameAuthActivity.this, R.string.operation_succeed, Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("hasRealNameAuth", true);
                editor.commit();
            }
        }
    };

    private Runnable submitRealNameAuthTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt("response", postSubmitRealNameAuthRequest());
            msg.setData(data);
            submitRealNameAuthHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_name_auth);

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < classIDs.length; i ++) {
            list.add(classIDs[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RealNameAuthActivity.this, R.layout.support_simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.class_id_spinner);
        spinner.setAdapter(adapter);
        /*
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                classID = list.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        */

        input_name_chinese = (EditText) findViewById(R.id.input_name_chinese);
        input_name_english = (EditText) findViewById(R.id.input_name_english);

        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButton.setEnabled(false);
                backButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                chinese_name = input_name_chinese.getText().toString();
                english_name = input_name_english.getText().toString();
                classID = (String) spinner.getSelectedItem();
                new Thread(submitRealNameAuthTask).start();
            }
        });

        backButton = (TextView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private int postSubmitRealNameAuthRequest() {
        int responseCode = HttpsURLConnection.HTTP_NOT_FOUND;
        String token = getSharedPreferences("user", MODE_APPEND).getString("token", "No Token");
        try {
            URL url = new URL("https://api.nfls.io/center/realname?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookies", " token" + token);

            JSONObject data = new JSONObject();
            data.put("chnName", chinese_name);
            data.put("engName", english_name);
            data.put("tmpClass", classID);

            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(data.toString());
            out.flush();
            out.close();

            responseCode = connection.getResponseCode();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
    }

    public static boolean getRealNameAuthRequest(String token) {
        boolean hasRealNameAuth = false;
        String jsonString = getAuthRequest(token);
        if (!jsonString.equals(NFLSUtil.REQUEST_FAILED)) {
            try {
                JSONObject json = new JSONObject(jsonString);
                if (json.getJSONObject("info").getInt("ic") == 1) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hasRealNameAuth;
    }

    public static boolean getPhoneAuthRequest(String token) {
        boolean hasPhoneAuth = false;
        String jsonString = getAuthRequest(token);
        if (!jsonString.equals(NFLSUtil.REQUEST_FAILED)) {
            try {
                JSONObject json = new JSONObject(jsonString);
                if (json.getJSONObject("info").getBoolean("phone")) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hasPhoneAuth;
    }

    private static String getAuthRequest(String token) {
        String json = NFLSUtil.REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/center/auth?token=" + token);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                json = NFLSUtil.inputStreamToString(in);
                Log.d("Json", json);
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
