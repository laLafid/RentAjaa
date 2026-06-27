package com.lafid.rentaja;

import android.os.Bundle;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class indiMainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        // Setup WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // Prevent opening browser, stay in app
        webView.setWebViewClient(new WebViewClient());

        // Optional: inject Java bridge (if you want Java <-> JS communication later)
        // webView.addJavascriptInterface(new JavaBridge(this), "Android");

        // Load your HTML UI from assets folder
        webView.loadUrl("file:///android_asset/index.html");
    }

    // Handle back button inside WebView
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
