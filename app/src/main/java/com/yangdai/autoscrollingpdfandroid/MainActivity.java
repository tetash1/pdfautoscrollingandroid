package com.yangdai.autoscrollingpdfandroid;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.yangdai.myapplication.R;

public class MainActivity extends AppCompatActivity {

    private PDFView pdfView;
    private Button btnAutoScroll;
    private boolean isAutoScrolling = false;
    private Handler handler;
    private static final int AUTO_SCROLL_DELAY = 2000; // Adjust the delay as needed
    private static final int SCROLL_INCREMENT = 24; // Adjust the scroll increment as needed

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView = findViewById(R.id.pdfView);
        btnAutoScroll = findViewById(R.id.btnAutoScroll);
        handler = new Handler();

        // Load a PDF document from the assets folder
        pdfView.fromAsset("math_removed.pdf")
                .defaultPage(0)
                .scrollHandle(null)
                .spacing(8) // space between pages in dp
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();

        btnAutoScroll.setOnClickListener(v -> {
            if (!isAutoScrolling) {
                // Start auto-scrolling
                startAutoScroll();
                btnAutoScroll.setText("Stop Auto-Scroll");
            } else {
                // Stop auto-scrolling
                stopAutoScroll();
                btnAutoScroll.setText("Start Auto-Scroll");
            }
        });
    }

    private void startAutoScroll() {
        isAutoScrolling = true;
        handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        isAutoScrolling = false;
        handler.removeCallbacks(autoScrollRunnable);
    }

    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // Scroll the PDF view by a specified increment
            pdfView.scrollTo(0, pdfView.getScrollY() + SCROLL_INCREMENT);

            // Check if the end of the document is reached
            if (pdfView.getScrollY() >= pdfView.getHeight() * pdfView.getPageCount() - pdfView.getHeight()) {
                // End of the document, stop auto-scrolling
                stopAutoScroll();
                btnAutoScroll.setText("Start Auto-Scroll");
            } else {
                handler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        }
    };
}
