package io.nfls.williamxie.nflser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import jp.wasabeef.blurry.Blurry;
import jp.wasabeef.blurry.internal.Blur;
import jp.wasabeef.blurry.internal.BlurFactor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PassKitActivity extends AppCompatActivity {

    private static final int BACKGROUND = 0;
    private static final int ICON = 1;
    private static final int LOGO = 2;
    private static final int THUMBNAIL = 3;

    private URL url = null;
    private File passkitFile = null;
    private Bitmap[] bitmaps = new Bitmap[4];
    private JSONObject passContent = null;

    private AlertDialog alertDialog = null;
    private LinearLayout backgroundLayout = null;
    private ImageView logo = null;
    private ImageView thumbnail = null;
    private ImageView barcode = null;
    private TextView label_1 = null;
    private TextView label_2 = null;
    private TextView label_3 = null;
    private TextView label_4 = null;
    private TextView value_1 = null;
    private TextView value_2 = null;
    private TextView value_3 = null;
    private TextView value_4 = null;
    private ScrollView back = null;
    private LinearLayout pad = null;
    private TextView text_1 = null;
    private TextView text_2 = null;
    private TextView text_3 = null;
    private TextView text_4 = null;
    private TextView text_5 = null;

    private Handler downloadPasskitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            alertDialog.dismiss();
            Bundle data = msg.getData();
            String response = data.getString("response");
            if (response.equals(NFLSUtil.REQUEST_FAILED)) {
                Toast.makeText(PassKitActivity.this, R.string.request_failed, Toast.LENGTH_LONG).show();
                finish();
            } else {
                refreshViews();
            }
        }
    };

    private Runnable downloadPasskitTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("response", downloadPasskit());
            msg.setData(data);
            downloadPasskitHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_kit);

        try {
            url = new URL(getIntent().getExtras().getString("url"));
            refresh();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        backgroundLayout = (LinearLayout) findViewById(R.id.background);
        logo = (ImageView) findViewById(R.id.logo);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        barcode = (ImageView) findViewById(R.id.barcode);
        label_1 = (TextView) findViewById(R.id.label_1);
        label_2 = (TextView) findViewById(R.id.label_2);
        label_3 = (TextView) findViewById(R.id.label_3);
        label_4 = (TextView) findViewById(R.id.label_4);
        value_1 = (TextView) findViewById(R.id.value_1);
        value_2 = (TextView) findViewById(R.id.value_2);
        value_3 = (TextView) findViewById(R.id.value_3);
        value_4 = (TextView) findViewById(R.id.value_4);
        back = (ScrollView) findViewById(R.id.back);
        pad = (LinearLayout) findViewById(R.id.pad);
        text_1 = (TextView) findViewById(R.id.text_1);
        text_2 = (TextView) findViewById(R.id.text_2);
        text_3 = (TextView) findViewById(R.id.text_3);
        text_4 = (TextView) findViewById(R.id.text_4);
        text_5 = (TextView) findViewById(R.id.text_5);

        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundLayout.setVisibility(GONE);
                back.setVisibility(VISIBLE);
            }
        });

        pad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundLayout.setVisibility(VISIBLE);
                back.setVisibility(GONE);
            }
        });

        back.setVisibility(GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //refresh();
    }

    @Override
    public void onBackPressed() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        finish();
    }

    private void refresh() {
        NFLSUtil.isOnline = NFLSUtil.isNetworkAvailable(PassKitActivity.this);
        if (NFLSUtil.isOnline) {
            alertDialog = new AlertDialog.Builder(PassKitActivity.this)
                    .setIcon(R.mipmap.nflsio)
                    .setTitle(R.string.downloading)
                    .setCancelable(false)
                    .show();
            new Thread(downloadPasskitTask).start();
        } else {
            new AlertDialog.Builder(PassKitActivity.this)
                    .setIcon(R.mipmap.nflsio)
                    .setTitle(R.string.warning)
                    .setMessage(getString(R.string.offline) + ", " + getString(R.string.offline_tip))
                    .setCancelable(false)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Intent intent =  new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    private void refreshViews() {
        try {
            ZipFile passkitZip = new ZipFile(passkitFile);
            ZipInputStream zin = new ZipInputStream(new FileInputStream(passkitFile));
            ZipEntry ze;
            String zipRootName = passkitZip.getName();
            //zipRootName = zipRootName.substring(0, zipRootName.length() - 4);
            ze = zin.getNextEntry();
            Log.d("Zin", zin.toString());
            while (ze != null) {
                String fileName = ze.getName();
                Log.d("FileName", fileName);
                if (fileName.equals("background@2x.png")) {
                    bitmaps[BACKGROUND] = readImageFile(passkitZip.getInputStream(ze));
                    Log.d("Bitmap", bitmaps[BACKGROUND].toString());
                } else if (fileName.equals("icon@2x.png")) {
                    bitmaps[ICON] = readImageFile(passkitZip.getInputStream(ze));
                    Log.d("Bitmap", bitmaps[ICON].toString());
                } else if (fileName.equals("logo@2x.png")) {
                    bitmaps[LOGO] = readImageFile(passkitZip.getInputStream(ze));
                    Log.d("Bitmap", bitmaps[LOGO].toString());
                } else if (fileName.equals("thumbnail@2x.png")) {
                    bitmaps[THUMBNAIL] = readImageFile(passkitZip.getInputStream(ze));
                    Log.d("Bitmap", bitmaps[THUMBNAIL].toString());
                } else if (fileName.equals("pass.json")) {
                    passContent = readJsonFile(passkitZip.getInputStream(ze));
                    Log.d("PassContent", passContent.toString());
                }
                ze = zin.getNextEntry();
            }
            bitmaps[BACKGROUND] = NFLSUtil.blurBitmap(PassKitActivity.this, bitmaps[BACKGROUND], 5);
            backgroundLayout.setBackground(new BitmapDrawable(getResources(), bitmaps[BACKGROUND]));
            logo.setImageBitmap(bitmaps[LOGO]);
            thumbnail.setImageBitmap(bitmaps[THUMBNAIL]);
            barcode.setImageBitmap(NFLSUtil.createBarcode(passContent.getJSONObject("barcode").getString("message"), barcode.getWidth(), barcode.getHeight()));
            String color = passContent.getString("labelColor");
            color = color.substring(color.lastIndexOf("(") + 1, color.lastIndexOf(")"));
            Log.d("LabelColor", color);
            String[] rgb = color.split(",");
            for (int i = 0; i < rgb.length; i ++) {
                rgb[i] = rgb[i].trim();
                Log.d("RGB[" + i + "]", rgb[i]);
            }
            int labelColor = Color.rgb(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
            color = passContent.getString("foregroundColor");
            color = color.substring(color.lastIndexOf("(") + 1, color.lastIndexOf(")"));
            Log.d("ForegroundColor", color);
            rgb = color.split(",");
            for (int i = 0; i < rgb.length; i ++) {
                rgb[i] = rgb[i].trim();
                Log.d("RGB[" + i + "]", rgb[i]);
            }
            int foregroundColor = Color.rgb(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
            foregroundColor = Color.LTGRAY;

            label_1.setTextColor(labelColor);
            label_2.setTextColor(labelColor);
            label_3.setTextColor(labelColor);
            label_4.setTextColor(labelColor);
            value_1.setTextColor(foregroundColor);
            value_2.setTextColor(foregroundColor);
            value_3.setTextColor(foregroundColor);
            value_4.setTextColor(foregroundColor);

            label_1.setText(passContent.getJSONObject("eventTicket").getJSONArray("primaryFields").getJSONObject(0).getString("label"));
            label_2.setText(passContent.getJSONObject("eventTicket").getJSONArray("secondaryFields").getJSONObject(0).getString("label"));
            label_3.setText(passContent.getJSONObject("eventTicket").getJSONArray("auxiliaryFields").getJSONObject(0).getString("label"));
            label_4.setText(passContent.getJSONObject("eventTicket").getJSONArray("auxiliaryFields").getJSONObject(1).getString("label"));
            value_1.setText(passContent.getJSONObject("eventTicket").getJSONArray("primaryFields").getJSONObject(0).getString("value"));
            value_2.setText(passContent.getJSONObject("eventTicket").getJSONArray("secondaryFields").getJSONObject(0).getString("value"));
            value_3.setText(passContent.getJSONObject("eventTicket").getJSONArray("auxiliaryFields").getJSONObject(0).getString("value"));
            value_4.setText(passContent.getJSONObject("eventTicket").getJSONArray("auxiliaryFields").getJSONObject(1).getString("value"));
            JSONArray backFields = passContent.getJSONObject("eventTicket").getJSONArray("backFields");
            text_1.setText(backFields.getJSONObject(0).getString("label") + "\n" + backFields.getJSONObject(0).getString("value") + "\n" + backFields.getJSONObject(0).getString("attributedValue"));
            text_2.setText(backFields.getJSONObject(1).getString("label") + "\n" + backFields.getJSONObject(1).getString("value") + "\n" + backFields.getJSONObject(1).getString("attributedValue"));
            text_3.setText(backFields.getJSONObject(2).getString("label") + "\n" + backFields.getJSONObject(2).getString("value") + "\n" + backFields.getJSONObject(2).getString("attributedValue"));
            text_4.setText(backFields.getJSONObject(3).getString("label") + "\n" + backFields.getJSONObject(3).getString("value") + "\n" + backFields.getJSONObject(3).getString("attributedValue"));
            text_5.setText(backFields.getJSONObject(4).getString("label") + "\n" + backFields.getJSONObject(4).getString("value") + "\n" + backFields.getJSONObject(4).getString("attributedValue"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String downloadPasskit() {
        String response = NFLSUtil.REQUEST_FAILED;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(NFLSUtil.TIME_OUT);
            connection.setReadTimeout(NFLSUtil.TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", "token=" + getSharedPreferences("user", MODE_APPEND).getString("token", "No Token"));
            connection.setDoInput(true);
            Log.d("Message", connection.getResponseMessage());
            if (connection.getResponseCode() == 200) {
                Log.d("Content-Disposition", connection.getHeaderField("Content-Disposition"));

                InputStream is = connection.getInputStream();
                byte[] arr = new byte[1];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos);
                double n = is.read(arr);
                while (n > 0) {
                    bos.write(arr);
                    n = is.read(arr);
                }
                bos.close();
                String path = NFLSUtil.FILE_PATH_DOWNLOAD_PASSKITS;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fileName = connection.getHeaderField("Content-Disposition");
                File file = new File(path + "/" + fileName.substring(fileName.indexOf("\"") + 1, fileName.lastIndexOf(".")) + ".zip");
                if (!file.exists()) {
                    file.createNewFile();
                }
                passkitFile = file;
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.close();
                response = path;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static JSONObject readJsonFile(InputStream is) {
        JSONObject json = null;
        try {
            json = new JSONObject(readTxtFile(is));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String readTxtFile(InputStream is) {
        StringBuffer content = new StringBuffer();
        try {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                content.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static Bitmap readImageFile(InputStream is) {
        Bitmap content;
        content = BitmapFactory.decodeStream(is);
        return content;
    }

    @Override
    public void finish() {
        passkitFile = null;
        bitmaps = null;
        passContent = null;
        super.finish();
    }
}
