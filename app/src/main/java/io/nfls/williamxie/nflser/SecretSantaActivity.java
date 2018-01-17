package io.nfls.williamxie.nflser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class SecretSantaActivity extends AppCompatActivity {

    private ImageView textArea;
    private ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_santa);

        textArea = (ImageView) findViewById(R.id.text_area);
        button = (ImageButton) findViewById(R.id.button);

        textArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SecretSantaActivity.this, "试试点点其他地方", Toast.LENGTH_SHORT).show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("SantaChecked", true);
                editor.commit();
                if (NFLSUtil.isApplicationInstalled("com.williamxie.carol", SecretSantaActivity.this)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.williamxie.carol");
                    startActivity(intent);
                } else {
                    Toast.makeText(SecretSantaActivity.this, "下载圣诞游戏吧 !!!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse("https://nflsio.oss-cn-shanghai.aliyuncs.com/resources/Christmas%20Carol-release.apk");
                    intent.setData(uri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        ComponentName componentName = intent.resolveActivity(getPackageManager());
                        startActivity(Intent.createChooser(intent, "请选择浏览器"));
                    } else {
                        Toast.makeText(getApplicationContext(), "没有匹配的程序", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isSantaChecked()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(SecretSantaActivity.this, "不点点看屏幕吗？", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSantaChecked() {
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
        return preferences.getBoolean("SantaChecked", false);
    }
}
