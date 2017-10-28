package io.nfls.williamxie.nflser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public class PdfViewerActivity extends AppCompatActivity {

    PDFView pdfView = null;
    String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

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
        pdfView = (PDFView) findViewById(R.id.pdf_view);
        Log.d("FilePath", filePath);
        File file = new File(filePath);
        pdfView.fromFile(file)
                .defaultPage(1)
                .enableSwipe(true)
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .spacing(0)
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        Log.d("nbPages", nbPages + "");
                    }
                })
                .load();
        String title = null;
        try {
            title = URLDecoder.decode(file.getName(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (title.length() > 10) {
            title = title.substring(0, 11) + " ...";
        }
        ((TextView) findViewById(R.id.pdf_title)).setText(title);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
