package io.nfls.williamxie.nflser;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageViewerActivity extends AppCompatActivity {

    ImageView imageView = null;
    String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        findViewById(R.id.back_icon).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.help_icon).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        filePath = getIntent().getExtras().getString("filePath");
        Log.d("FilePath", filePath);
        File file = new File(filePath);
        String title = file.getName();
        if (title.length() > 10) {
            title = title.substring(0, 11) + " ...";
        }
        ((TextView) findViewById(R.id.image_title)).setText(title);
        imageView = (ImageView) findViewById(R.id.image_view);
        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
