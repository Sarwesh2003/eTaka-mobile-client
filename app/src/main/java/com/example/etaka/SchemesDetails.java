package com.example.etaka;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class SchemesDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schemes_details);
        WebView browser = (WebView) findViewById(R.id.webview);
        browser.setInitialScale(100);
        browser.loadUrl("https://agricoop.nic.in/en/Major#gsc.tab=0");
    }
}