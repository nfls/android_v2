package io.nfls.williamxie.nflser;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class LaunchActivity extends AppCompatActivity {

    private ImageView launch_image_view;
    private TextView caption_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        launch_image_view = (ImageView) findViewById(R.id.launch_image_view);
        caption_view = (TextView) findViewById(R.id.caption_view);

        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
            }
        };

        Calendar calendar = Calendar.getInstance();

        if (NFLSUtil.isOnDate(2017, 12, 25)) {
            loadChristmasImage();
            timer.schedule(timerTask, 4500);
        } else {
            loadDefaultImage();
            timer.schedule(timerTask, 3000);
        }

        System.out.println(caption_view.getVisibility() == View.VISIBLE);
        System.out.println(caption_view.getX());
        System.out.println(caption_view.getY());
    }

    public void loadDefaultImage () {

        launch_image_view.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.lauch_image_default));
        caption_view.setText("忙碌的ALevel\n© 2017 李道辰");
    }

    public void loadChristmasImage() {

        launch_image_view.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.female_suit));
        caption_view.setText("期待hqy的圣诞版女装福利吧 !!!\n© 2017 季晨晨");
    }

    @Override
    public void onBackPressed() {
        return;
    }
}