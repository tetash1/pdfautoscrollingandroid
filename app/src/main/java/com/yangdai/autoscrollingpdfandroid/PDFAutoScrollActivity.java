package com.yangdai.autoscrollingpdfandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yangdai.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PDFAutoScrollActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private PdfRenderer pdfRenderer;
    private LinearLayout pdfPageViews;

    private final int scrollSpeed = 1;
    private final int scrollDelay = 2000;
    private final int scrollInterval = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfauto_scroll);

        scrollView = findViewById(R.id.scrollView);

        // Copy PDF file from assets to cache directory
        String fileName = "math_removed.pdf"; // Replace with the actual file name
        File file = new File(getCacheDir(), fileName);
        if (!file.exists()) {
            try {
                InputStream inputStream = getAssets().open(fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfPageViews = new LinearLayout(this);
        pdfPageViews.setOrientation(LinearLayout.VERTICAL);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        for (int pageIndex = 0; pageIndex < pdfRenderer.getPageCount(); pageIndex++) {
            PdfRenderer.Page pdfPage = pdfRenderer.openPage(pageIndex);
            int pdfPageWidth = pdfPage.getWidth();
            int pdfPageHeight = pdfPage.getHeight();
            float scaleFactor = (float) screenWidth / pdfPageWidth;
            int scaledHeight = (int) (scaleFactor * pdfPageHeight);
            Bitmap pdfBitmap = Bitmap.createBitmap(screenWidth, scaledHeight, Bitmap.Config.ARGB_8888);
            pdfPage.render(pdfBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfPage.close();

            PdfPageView pdfPageView = new PdfPageView(this, pdfBitmap);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    scaledHeight
            );
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(pdfPageView, layoutParams);
            pdfPageViews.addView(linearLayout);
        }

        scrollView.addView(pdfPageViews);
        scrollView.setSmoothScrollingEnabled(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAutoScrolling();
            }
        }, scrollDelay);
    }

    private void startAutoScrolling() {
        final int totalHeight = pdfPageViews.getHeight();
        final int maxScrollY = totalHeight - scrollView.getHeight();
        final Handler handler = new Handler(Looper.getMainLooper());

        final Runnable scrollRunnable = new Runnable() {
            int scrollY = 0;

            @Override
            public void run() {
                scrollY += scrollSpeed;
                if (scrollY >= maxScrollY) {
                    scrollY = maxScrollY;
                }
                scrollView.scrollTo(0, scrollY);
                if (scrollY < maxScrollY) {
                    handler.postDelayed(this, scrollInterval);
                } else {
                    scrollView.scrollTo(0, 0);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Code to be executed after a delay
                            scrollY = 0;
                            handler.postDelayed(this, scrollInterval);
                        }
                    }, 2000); // Adjust the delay as needed
                }
            }
        };
        handler.postDelayed(scrollRunnable, scrollDelay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
    }

    static class PdfPageView extends View {
        private Bitmap pdfBitmap;

        public PdfPageView(Context context, Bitmap pdfBitmap) {
            super(context);
            this.pdfBitmap = pdfBitmap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(pdfBitmap, 0f, 0f, null);
        }
    }
}

