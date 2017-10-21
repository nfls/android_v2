
package io.nfls.williamxie.nflser;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;

public class ResourcesActivity extends AppCompatActivity {

    private String lastDirectoryPath = "";
    private String currentDirectoryPath = "/";

    private LinkedList<ResourceFile> mData;
    private ResourceFileAdapter mAdapter;
    private ListView list_resource_files;
    private AlertDialog alertDialog = null;

    private ResourceFile targetFile = null;
    private Thread thread = null;

    private Handler getDirectoryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            alertDialog.dismiss();
            Bundle data = msg.getData();
            String jsonString = data.getString("json");
            if (!jsonString.equals(NFLSUtil.REQUEST_FAILED)) {
                Log.d("Json", jsonString);
                try {
                    mData.clear();
                    mData.addAll(unpackJson(new JSONObject(jsonString)));
                    refreshList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ResourcesActivity.this, getString(R.string.request_failed), Toast.LENGTH_LONG).show();
            }
        }
    };

    private Handler downloadFileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            alertDialog.dismiss();
            Bundle data = msg.getData();
            String response = data.getString("response");
            if (!response.equals(NFLSUtil.REQUEST_FAILED)) {
                Log.d("response", response);
                targetFile.setDownloaded(true);
                refreshList();
                Log.d("Response", response);
                viewFile(targetFile);
            } else {
                Toast.makeText(ResourcesActivity.this, getString(R.string.request_failed), Toast.LENGTH_LONG).show();
            }
        }
    };

    private Handler refreshDialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            alertDialog.setMessage(getString(R.string.downloading ) + " " + msg.getData().getString("double") + "%");
        }
    };

    private Runnable getDirectoryTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", getDirectory());
            msg.setData(data);
            getDirectoryHandler.sendMessage(msg);
        }
    };

    private Runnable downloadFileTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("response", downloadFile(targetFile));
            msg.setData(data);
            downloadFileHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        list_resource_files = (ListView) findViewById(R.id.list_resource_files);
        mData = new LinkedList<ResourceFile>();
        mAdapter = new ResourceFileAdapter(mData, ResourcesActivity.this);
        list_resource_files.setAdapter(mAdapter);

        list_resource_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ResourceFile file = mData.get(i);
                if (NFLSUtil.isNetworkAvailable(ResourcesActivity.this)) {
                    if (file.isFolder()) {
                        lastDirectoryPath = currentDirectoryPath;
                        currentDirectoryPath = file.getHref();
                        goToDirectory(currentDirectoryPath);
                    } else {
                        if (!file.isDownloaded()) {
                            targetFile = file;
                            alertDialog = new AlertDialog.Builder(ResourcesActivity.this)
                                    .setTitle(R.string.downloading)
                                    .setMessage(targetFile.getName())
                                    .setIcon(R.mipmap.nflsio)
                                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            thread.interrupt();
                                        }
                                    })
                                    .show();
                            thread = new Thread(downloadFileTask);
                            thread.start();
                        } else {
                            viewFile(file);
                        }
                    }
                } else {

                }
            }
        });

        list_resource_files.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ResourceFile file = mData.get(i);
                if (file.isDownloaded()) {
                    alertDialog = new AlertDialog.Builder(ResourcesActivity.this)
                            .setTitle(R.string.warning)
                            .setMessage(R.string.delete_tip)
                            .setIcon(R.mipmap.nflsio)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File localFile = new File(Environment.getExternalStorageDirectory() + "/download" + file.getHref());
                                    localFile.delete();
                                    file.setDownloaded(false);
                                    refreshList();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            })
                            .show();
                }
                return true;
            }
        });

        findViewById(R.id.back_icon).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!lastDirectoryPath.equals("")) {
                    currentDirectoryPath = lastDirectoryPath;
                    lastDirectoryPath = lastDirectoryPath.substring(0, lastDirectoryPath.lastIndexOf("/"));
                    if (!lastDirectoryPath.equals("")) {
                        lastDirectoryPath = lastDirectoryPath.substring(0, lastDirectoryPath.lastIndexOf("/") + 1);
                    }
                    goToDirectory(currentDirectoryPath);
                } else {
                    exit();
                    //startActivity(new Intent(ResourcesActivity.this, HomeActivity.class));
                }
            }
        });

        findViewById(R.id.help_icon).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        NFLSUtil.verifyStoragePermissions(ResourcesActivity.this);

        if (NFLSUtil.isNetworkAvailable(ResourcesActivity.this)) {
            goToDirectory(currentDirectoryPath);
        }
    }

    private void exit() {
        if (!alertDialog.equals(null)) {
            alertDialog.dismiss();
        }
        finish();
    }

    private void goToDirectory(String directoryPath) {
        alertDialog = new AlertDialog.Builder(ResourcesActivity.this).setMessage(R.string.loading).show();
        new Thread(getDirectoryTask).start();
    }

    private void refreshList() {
        mAdapter.notifyDataSetChanged();
    }

    private JSONObject packJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("action", "get");
            JSONObject subJson = new JSONObject();
            subJson.put("href", currentDirectoryPath);
            subJson.put("what", 1);
            json.put("items", subJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private LinkedList<ResourceFile> unpackJson(JSONObject json) {
        LinkedList<ResourceFile> files = new LinkedList<ResourceFile>();
        try {
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.length(); i ++) {
                JSONObject item = items.getJSONObject(i);

                boolean isFolder = false;
                if (item.has("managed")) {
                    isFolder = true;
                }
                String href = item.getString("href").replaceAll("\\\\", "");
                String name;
                if (isFolder) {
                    name = href.substring(0, href.lastIndexOf("/"));
                } else {
                    name = href;
                }
                name = name.substring(name.lastIndexOf("/") + 1);
                name = java.net.URLDecoder.decode(name, "utf-8");

                if(currentDirectoryPath.equals(href) || !href.contains(currentDirectoryPath)) continue;

                boolean isDownloaded = false;
                String path = Environment.getExternalStorageDirectory() + "/download" + href;
                File file = new File(path);
                if (file.exists()) {
                    isDownloaded = true;
                }
                if (isFolder) {
                    isDownloaded = false;
                }

                long date = item.getLong("time");
                long size = item.getLong("size");

                files.add(new ResourceFile(name, date, size, href, isFolder, isDownloaded));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return files;
    }

    private String getDirectory() {
        String json = NFLSUtil.REQUEST_FAILED;
        String token = getSharedPreferences("user", MODE_APPEND).getString("token", "No Token");
        try {
            URL url = new URL("https://dl.nfls.io/?");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", " token=" + token);
            connection.setDoOutput(true);

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(packJson().toString());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                json = new JSONObject(NFLSUtil.inputStreamToString(connection.getInputStream())).toString();
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

    private String downloadFile(ResourceFile targetFile) {
        String response = NFLSUtil.REQUEST_FAILED;
        String token = getSharedPreferences("user", MODE_APPEND).getString("token", "No Token");
        HttpsURLConnection connection = null;
        try {
            URL url = new URL("https://dl.nfls.io" + targetFile.getHref());
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", " token=" + token);
            Log.d("URL", url.toString());
            Log.d("Code", connection.getResponseCode() + "");
            Log.d("Method", connection.getRequestMethod());
            Log.d("Message", connection.getResponseMessage());
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                byte[] arr = new byte[1];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos);
                double size = connection.getContentLength();
                double n = is.read(arr);
                while (n > 0) {
                    bos.write(arr);
                    n = is.read(arr);
                    if (n % (1024 * 1024) == 0) {
                        Bundle data = new Bundle();
                        data.putString("progress", ResourceFile.df.format(n / size));
                        Message msg = new Message();
                        msg.setData(data);
                        refreshDialogHandler.sendMessage(msg);
                    }
                }
                bos.close();
                String path = Environment.getExternalStorageDirectory() + "/download";
                String href = targetFile.getHref();
                File dir = new File(path + href.substring(0, href.lastIndexOf("/")));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                path += href;
                File file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.close();
                response = path;
                return response;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!connection.equals(null)) {
                connection.disconnect();
            }
        }
        return response;
    }

    private void viewFile(ResourceFile file) {
        String href = file.getHref();
        viewFile(href);
    }

    private void viewFile(String href) {
        if (href.endsWith(".pdf")) {
            Intent intent = new Intent(ResourcesActivity.this, PdfViewerActivity.class);
            intent.putExtra("filePath", Environment.getExternalStorageDirectory() + "/download" + href);
            startActivity(intent);
        } else if (href.endsWith(".mp4")) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            Uri data = Uri.parse(Environment.getExternalStorageDirectory() + "/download" + href);
            intent.setDataAndType(data, "video/mp4");
            startActivity(intent);
        } else if (href.endsWith(".jpg") || href.endsWith(".JPG") || href.endsWith(".png") || href.endsWith(".PNG")) {
            Intent intent = new Intent(ResourcesActivity.this, ImageViewerActivity.class);
            intent.putExtra("filePath", Environment.getExternalStorageDirectory() + "/download" + href);
            startActivity(intent);
        } else {
            String suffix = "";
            if (href.contains(".")) {
                suffix = href.substring(href.lastIndexOf("."));
            }
            Toast.makeText(ResourcesActivity.this, getString(R.string.unsupported_tip) + " \"" + suffix + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (!alertDialog.equals(null)) {
            alertDialog.dismiss();
        }
        finish();
    }
}