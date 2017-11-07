package io.nfls.williamxie.nflser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
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

import static io.nfls.williamxie.nflser.NFLSUtil.*;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar = null;
    private Button loginButton = null;
    private TextView signUpButton = null;
    private TextView resetPasswordButton = null;
    private TextView offlineModeButton = null;

    private String username = null;
    private String password = null;

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            progressBar.setVisibility(View.INVISIBLE);
            loginButton.setEnabled(true);
            signUpButton.setEnabled(true);
            resetPasswordButton.setEnabled(true);
            offlineModeButton.setEnabled(true);
            if (jsonString.equals(REQUEST_FAILED)) {
                Toast.makeText(LoginActivity.this, R.string.request_failed, Toast.LENGTH_SHORT).show();
                clearPreferences(LoginActivity.this);
            } else {
                JSONObject json = null;
                try {
                    json = new JSONObject(jsonString);
                    if (json.getJSONObject("info").getString("status").equals("success")) {
                        storeCookies(json.getJSONObject("info").getString("token"));
                        new Thread(getUsernameTask).start();
                    } else {
                        Toast.makeText(LoginActivity.this, json.getJSONObject("info").getString("message"), Toast.LENGTH_SHORT).show();
                        clearPreferences(LoginActivity.this);
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
            loginButton.setEnabled(true);
            signUpButton.setEnabled(true);
            resetPasswordButton.setEnabled(true);
            offlineModeButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            Bundle data = msg.getData();
            String result = data.getString("result");
            Log.d("result", result);
            if (result.equals(TOKEN_CORRECT)) {
                //Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_LONG).show();
                checkAuth();
            }
        }
    };

    private Handler getAuthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressBar.setVisibility(View.INVISIBLE);
            loginButton.setEnabled(true);
            signUpButton.setEnabled(true);
            resetPasswordButton.setEnabled(true);
            offlineModeButton.setEnabled(true);
            SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
            SharedPreferences.Editor editor = preferences.edit();
            String response = msg.getData().getString("response");
            if (response == REQUEST_FAILED) {
                Toast.makeText(LoginActivity.this, getString(R.string.phone_auth_activity_title) + " " + getString(R.string.request_failed), Toast.LENGTH_SHORT);
            } else {
                try {
                    Log.d("JSON", response);
                    JSONObject json = new JSONObject(response);
                    json = json.getJSONObject("info");
                    editor.putBoolean("hasPhoneAuth", json.getBoolean("phone"));
                    try {
                        editor.putBoolean("hasRealNameAuth", json.getInt("ic") == 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        editor.putBoolean("hasRealNameAuth", json.getBoolean("ic"));
                    }
                    finally {
                        editor.commit();
                    }
                    Log.d("hasRealNameAuth", preferences.getBoolean("hasRealNameAuth", false) + "");
                    if (!preferences.getBoolean("hasPhoneAuth", false)) {
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                                .setTitle(R.string.warning)
                                .setIcon(R.mipmap.nflsio)
                                .setMessage(R.string.phone_auth_go_tip)
                                .setCancelable(false)
                                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        startActivity(new Intent(LoginActivity.this, PhoneAuthActivity.class));
                                    }
                                })
                                .show();
                    } else if (!preferences.getBoolean("hasRealNameAuth", false)) {
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                                .setTitle(R.string.warning)
                                .setIcon(R.mipmap.nflsio)
                                .setMessage(R.string.real_name_auth_tip)
                                .setCancelable(false)
                                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        startActivity(new Intent(LoginActivity.this, RealNameAuthActivity.class));
                                    }
                                })
                                .show();
                    } else {
                        finish();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler postVersionCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressBar.setVisibility(View.INVISIBLE);
            String response = msg.getData().getString("response");
            //Log.d("response", response);
            if (response.equals(NFLSUtil.REQUEST_FAILED)) {
                Toast.makeText(LoginActivity.this, getString(R.string.request_failed), Toast.LENGTH_SHORT);
            } else {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getInt("code") != HttpsURLConnection.HTTP_OK) {
                        final String url = json.getString("info");
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle(R.string.warning)
                                .setIcon(R.mipmap.nflsio)
                                .setMessage(R.string.version_too_old_tip)
                                .setCancelable(false)
                                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Intent intent = new Intent();
                                        intent.setAction("android.intent.action.VIEW");
                                        Uri content_url = Uri.parse(url);
                                        intent.setData(content_url);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else {
                        if (getString(R.string.version_no).contains("-debug")) {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(R.string.warning)
                                    .setIcon(R.mipmap.nflsio)
                                    .setMessage(R.string.version_is_debug_tip)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            progressBar.setVisibility(View.VISIBLE);
                                            new Thread(autoLoginTask).start();
                                        }
                                    })
                                    .show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            new Thread(autoLoginTask).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable loginTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", postLoginRequest(username, password));
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

    private Runnable getAuthTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("response", getAuth());
            msg.setData(data);
            getAuthHandler.sendMessage(msg);
        }
    };

    private Runnable getUsernameTask = new Runnable() {
        @Override
        public void run() {
            getUsername();
            checkAuth();
        }
    };

    private Runnable postVersionCheckTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("response", postVersionCheck());
            msg.setData(data);
            postVersionCheckHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //clearPreferences();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_password = (EditText) findViewById(R.id.input_password);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setEnabled(false);
                signUpButton.setEnabled(false);
                resetPasswordButton.setEnabled(false);
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
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        resetPasswordButton = (TextView) findViewById(R.id.reset_password_button);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        offlineModeButton = (TextView) findViewById(R.id.offline_mode_button);
        offlineModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NFLSUtil.isOfflineMode = true;
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
        });

        if (!NFLSUtil.isInternetRequestAvailable(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, R.string.offline, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            loginButton.setEnabled(false);
            signUpButton.setEnabled(false);
            resetPasswordButton.setEnabled(false);
            //offlineModeButton.setEnabled(false);
            SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
            if (!preferences.getString("password", "fail").equals("fail")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("password");
                editor.putString("username", username);
                editor.commit();
                input_username.setText(username);
                input_password.setText(password);
                new Thread(loginTask).start();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                checkVersion();
            }
        }
    }

    public static String postLoginRequest(String username, String password) {
        String json = REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/center/login?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            //String data = "username=" + java.net.URLEncoder.encode(username, "utf-8") + "&password=" + java.net.URLEncoder.encode(password, "utf-8") + "&session=app";

            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("password", password);
            data.put("session", "app");

            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(data.toString());

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private String autoLogin() {
        String result = REQUEST_FAILED;
        SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
        String token = preferences.getString("token", "No Token");
        URL url;
        try {
            url = new URL("https://api.nfls.io/device/status?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", " token=" + token);
            connection.setRequestProperty("User-Agent", "Nflsers-Android");
            Log.d("Token", token);

            int responseCode = connection.getResponseCode();

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

    private void getUsername() {
        SharedPreferences preferences = getSharedPreferences("user", MODE_APPEND);
        String token = preferences.getString("token", "No Token");
        URL url;
        try {
            url = new URL("https://api.nfls.io/center/username?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", " token=" + token);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                JSONObject json = new JSONObject(NFLSUtil.inputStreamToString(connection.getInputStream()));
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", json.getString("info"));
                editor.commit();
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
    }

    private String getAuth() {
        Log.d("GetAuth", "In");
        String response = REQUEST_FAILED;
        String token = getSharedPreferences("user", MODE_APPEND).getString("token", "No Token");
        try {
            URL url = new URL("https://api.nfls.io/center/auth?token=" + token);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            Log.d("GetAuth Response Code", responseCode + "");

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
        Log.d("GetAuth Response", "Response");
        return response;
    }

    private String postVersionCheck() {
        String response = REQUEST_FAILED;
        try {
            URL url = new URL("https://api.nfls.io/device/android");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Cookie", getSharedPreferences("user", MODE_APPEND).getString("token", "No Token"));

            JSONObject data = new JSONObject();
            String version_no = getString(R.string.version_no);

            if (version_no.contains("-debug")) {
                data.put("version", version_no.substring(0, version_no.lastIndexOf("-debug")));
            } else {
                data.put("version", version_no);
            }

            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            Log.d("data", data.toString());
            out.writeBytes(data.toString());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = NFLSUtil.inputStreamToString(in);
                Log.d("response", response);
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
        return response;
    }

    public void storeCookies(String token) {
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.commit();
    }

    public static void clearPreferences(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("user", Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    private void checkAuth() {
        Log.d("CheckAuth", "In");
        Log.d("CheckAuth", "Thread Start");
        new Thread(getAuthTask).start();
    }

    private void checkVersion() {
        new Thread(postVersionCheckTask).start();
    }

    public static String getRequestHeader(HttpsURLConnection conn) {
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

    @Override
    public void onResume() {
        super.onResume();
        isOnline = isNetworkAvailable(LoginActivity.this);
        if (isOnline) {
            checkVersion();
        }
    }
}